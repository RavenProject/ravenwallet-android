package com.ravenwallet.tools.util;

import android.content.Context;
import android.content.res.AssetManager;

import com.ravenwallet.core.BRCoreKey;
import com.ravenwallet.core.BRCoreMasterPubKey;
import com.ravenwallet.tools.manager.BRReportsManager;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.text.Normalizer;
import java.util.Locale;

public class Bip39Wordlist {
    private static final String TAG = Bip39Wordlist.class.getName();

    public static final int WORD_LIST_SIZE = 2048;
    public static final int PHRASE_SZE = 12;

    String languageCode;
    String languageName;
    final String[] loadedWords = new String[WORD_LIST_SIZE];

    public static Bip39Wordlist[] LANGS = {
            new Bip39Wordlist("cs","Czech"),
            new Bip39Wordlist("en","English"),
            new Bip39Wordlist("es","Spanish"),
            new Bip39Wordlist("fr","French"),
            new Bip39Wordlist("it","Italian"),
            new Bip39Wordlist("ja","Japanese"),
            new Bip39Wordlist("ko","Korean"),
            new Bip39Wordlist("pt","Portuguese"),
            new Bip39Wordlist("zh-CN","Chinese (Simplified)"),
            new Bip39Wordlist("zh-TW","Chinese (Traditional)")
    };
    public static Bip39Wordlist DEFAULT_WORDLIST = LANGS[1]; //en

    public static Bip39Wordlist getWordlistForLocale() {
        return getWordlistForLocale(Locale.getDefault());
    }
    public static Bip39Wordlist getWordlistForLocale(Locale locale) {
        String languageCode = locale != null ? locale.getLanguage() : null;
        if (languageCode == null) return DEFAULT_WORDLIST;
        return getWordlistForLanguage(languageCode);
    }
    public static Bip39Wordlist getWordlistForLanguage(String languageCode) {
        if(languageCode == null) return null;
        for(Bip39Wordlist lang : LANGS) {
            if (lang.languageCode.equals(languageCode))
                return lang;
        }
        return null;
    }
    public static Bip39Wordlist identifyWordlist(Context app, byte[] paperKeyBytes) {
        return identifyWordlist(app, paperKeyString(paperKeyBytes));
    }
    public static Bip39Wordlist identifyWordlist(Context app, String phrase) {
        for(Bip39Wordlist list : LANGS)
            if (list.checkPhrase(app, phrase))
                return list;
        return null;
    }
    public static boolean isValidWord(Context app, String checkWord) {
        String cleanWord = cleanWord(checkWord);
        for(Bip39Wordlist list : LANGS)
            if(list.hasWord(app, cleanWord))
                return true;
        return false;
    }
    public static String cleanWord(String word) {
        String w = Normalizer.normalize(word.trim().replace("　", "")
                .replace(" ", ""), Normalizer.Form.NFKD);
        return w;
    }

    public static String paperKeyString(byte[] paperKeyBytes) {
        return new String(paperKeyBytes);
    }

    public static String[] splitPharse(byte[] paperKeyBytes) {
        return splitPharse(paperKeyString(paperKeyBytes));
    }

    public static String[] splitPharse(String phrase) {
        return phrase
                //Turn any non-breaking spaces into normal ones
                .replaceAll("[ \\t\\xA0\\u1680\\u180e\\u2000-\\u200a\\u202f\\u205f\\u3000]+", " ")
                .split("[\\s]+");
    }




    public Bip39Wordlist(String languageCode, String languageName) {
        this.languageCode = languageCode;
        this.languageName = languageName;
    }

    public String getLanguageCode() { return languageCode; }
    public String getLanguageName() { return languageName; }
    public String[] getWords(Context app) {
        loadWords(app);
        return loadedWords;
    }

    public boolean hasWord(Context app, String checkWord) {
        loadWords(app);
        if(checkWord == null) return false;
        for(String word : loadedWords) {
            if (checkWord.equals(word))
                return true;
        }
        return false;
    }
    public void loadWords() {
        loadWords(null);
    }
    public void loadWords(Context app) {
        if(loadedWords[0] != null) return;

        String fileName = "words/" + this.languageCode + "-BIP39Words.txt";
        BufferedReader reader = null;
        int lineIndex = 0;
        try {
            InputStream inputStream;
            if (app == null) { //Null when passed in for tests
                inputStream = getClass().getResourceAsStream(fileName);
            } else {
                AssetManager assetManager = app.getResources().getAssets();
                inputStream = assetManager.open(fileName);
            }
            reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            String line;
            while ((line = reader.readLine()) != null) {
                if (lineIndex < WORD_LIST_SIZE)
                    loadedWords[lineIndex] = cleanWord(line);
                lineIndex++;
            }
        } catch (FileNotFoundException fnfex) {
            BRReportsManager.reportBug(new IllegalArgumentException("Wordlist '" + fileName + "' does not exist."), true);
            fnfex.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (reader != null)
                    reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if(lineIndex != WORD_LIST_SIZE) {
            BRReportsManager.reportBug(new IllegalArgumentException("Wordlist '" + fileName + "' has unexpected size. Expected " + WORD_LIST_SIZE + " Got: " + lineIndex), true);
            throw new RuntimeException("Invalid wordlist");
        }
    }

    public boolean checkPhrase(Context app, String phrase) {
        loadWords(app);
        //This currently fails with UnsatisfiedLinkError, something with the JNI class. Implementing manually
        //return BRCoreMasterPubKey.validateRecoveryPhrase(loadedWords, phrase);
        String[] parts = splitPharse(phrase);
        for(String part : parts) {
            if (!this.hasWord(app, cleanWord(part)))
                return false;
        }
        return true;
    }

    public synchronized byte[] generatePaperKeyBytes(Context app, final byte[] seed) {
        loadWords(app);
        System.out.println("Seed: " + (seed == null ? 0 : seed.length));
        byte[] paperKeyBytes = BRCoreMasterPubKey.generatePaperKey(seed, loadedWords);
        if (paperKeyBytes == null || paperKeyBytes.length == 0) {
            BRReportsManager.reportBug(new NullPointerException("failed to encodeSeed"), true);
            return null;
        }
        return paperKeyBytes;
    }

    public byte[] generateRandomSeed() {
        final byte[] randomSeed = new SecureRandom().generateSeed(16);
        if (randomSeed.length != 16)
            throw new NullPointerException("failed to create the seed, seed length is not 128: " + randomSeed.length);
        return randomSeed;
    }

    public byte[] getDerivedPhraseKey(byte[] paperKeyBytes) {
        byte[] seed = BRCoreKey.getDerivedPhraseKey(paperKeyBytes);
        if (seed == null || seed.length == 0)
            throw new RuntimeException("seed is null");
        return seed;
    }

    public byte[] decodePaperKeyPhrase(Context app, String phrase) {
        byte[] seed = BRCoreMasterPubKey.decodePaperKey(phrase, getWords(app));
        if (seed == null || seed.length == 0)
            throw new RuntimeException("seed is null");
        return seed;
    }

    public byte[] getPrivateKeyForAPI(byte[] seed) {
        byte[] authKey = BRCoreKey.getAuthPrivKeyForAPI(seed);
        if (authKey == null || authKey.length == 0) {
            BRReportsManager.reportBug(new IllegalArgumentException("authKey is invalid"), true);
        }
        return authKey;
    }

    public BRCoreMasterPubKey getMasterPubKey(byte[] paperKeyBytes, boolean isPaperKey) {
        BRCoreMasterPubKey pubKey = new BRCoreMasterPubKey(paperKeyBytes, true);
        if(pubKey == null) {
            BRReportsManager.reportBug(new NullPointerException("Did not recieve a valid master public key object."), true);
            return null;
        }
        return pubKey;
    }

}