package com.ravenwallet.tools.util;

import android.content.Context;
import android.content.res.AssetManager;

import com.ravenwallet.core.BRCoreKey;
import com.ravenwallet.core.BRCoreMasterPubKey;
import com.ravenwallet.tools.manager.BRReportsManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.SecureRandom;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Bip39Wordlist {
    private static final String TAG = Bip39Wordlist.class.getName();

    public static final int WORD_LIST_SIZE = 2048;

    String languageCode;
    String languageName;
    String[] loadedWords;

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
        for(Bip39Wordlist lang : LANGS) {
            if (lang.languageCode.equals(languageCode))
                return lang;
        }
        return getWordlistForLocale();
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




    public Bip39Wordlist(String languageCode, String languageName) {
        this.languageCode = languageCode;
        this.languageName = languageName;
    }

    public boolean hasWord(Context app, String checkWord) {
        loadWords(app);
        for(String word : loadedWords) {
            if (word.equals(checkWord))
                return true;
        }
        return false;
    }
    public void loadWords(Context app) {
        if(loadedWords != null) return;

        loadedWords = new String[WORD_LIST_SIZE];
        String fileName = "words/" + this.languageCode + "-BIP39Words.txt";
        BufferedReader reader = null;
        int lineIndex = 0;
        try {
            AssetManager assetManager = app.getResources().getAssets();
            InputStream inputStream = assetManager.open(fileName);
            reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                if(lineIndex < WORD_LIST_SIZE)
                    loadedWords[lineIndex] = cleanWord(line);
                lineIndex++;
            }
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
            BRReportsManager.reportBug(new IllegalArgumentException("Wordlis '" + fileName + "' has unexpected size."), true);
        }
    }

    public boolean checkPhrase(Context app, String phrase) {
        loadWords(app);
        return BRCoreMasterPubKey.validateRecoveryPhrase(loadedWords, phrase);
    }

    public byte[] generatePaperKeyBytes(Context app, byte[] seed) {
        loadWords(app);
        byte[] paperKeyBytes = BRCoreMasterPubKey.generatePaperKey(seed, loadedWords);
        if (paperKeyBytes == null || paperKeyBytes.length == 0) {
            BRReportsManager.reportBug(new NullPointerException("failed to encodeSeed"), true);
            return null;
        }
        return paperKeyBytes;
    }

    public String paperKeyString(byte[] paperKeyBytes) {
        return new String(paperKeyBytes);
    }

    public byte[] generateRandomSeed() {
        final byte[] randomSeed = new SecureRandom().generateSeed(16);
        if (randomSeed.length != 16)
            throw new NullPointerException("failed to create the seed, seed length is not 128: " + randomSeed.length);
        return randomSeed;
    }

    public String[] splitPharse(byte[] paperKeyBytes) {
        return splitPharse(paperKeyString(paperKeyBytes));
    }

    public String[] splitPharse(String phrase) {
        String[] parts = phrase.split(" ");
        if (parts.length != 12) {
            BRReportsManager.reportBug(new NullPointerException("phrase does not have 12 words:" + parts.length + ", lang: " + languageCode), true);
            return null;
        }
        return parts;
    }

    public byte[] getSeedFromPhrase(byte[] paperKeyBytes) {
        byte[] seed = BRCoreKey.getSeedFromPhrase(paperKeyBytes);
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