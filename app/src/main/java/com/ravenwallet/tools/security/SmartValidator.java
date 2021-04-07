package com.ravenwallet.tools.security;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.ravenwallet.core.BRCoreMasterPubKey;
import com.ravenwallet.tools.manager.BRReportsManager;
import com.ravenwallet.tools.manager.BRSharedPrefs;
import com.ravenwallet.tools.util.Bip39Wordlist;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * RavenWallet
 * <p/>
 * Created by Mihail Gutan on <mihail@breadwallet.com> 10/11/17.
 * Copyright (c) 2017 breadwallet LLC
 * <p/>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
public class SmartValidator {

    private static final String TAG = SmartValidator.class.getName();
    private static List<String> list;

    public static boolean isPhraseWordValid(Context ctx, String phraseWord) {
        return Bip39Wordlist.isValidWord(ctx, phraseWord);
    }

    public static boolean isPaperKeyValid(Context ctx, String paperKey) {
        return isPaperKeyValid(ctx, paperKey, Locale.getDefault().getLanguage());
    }

    private static boolean isPaperKeyValid(Context ctx, String paperKey, String lang) {
        Bip39Wordlist wordList = Bip39Wordlist.getWordlistForLanguage(lang);
        if(wordList == null) {
            Log.e(TAG, "No wordlist found for language: '" + lang + "'");
            BRReportsManager.reportBug(new IllegalArgumentException("No wordlist found for language: '" + lang + "'"), true);
        }
        return wordList.checkPhrase(ctx, paperKey);
    }

    public static boolean isPaperKeyCorrect(String insertedPhrase, Context activity) {
        String normalizedPhrase = Normalizer.normalize(insertedPhrase.trim(), Normalizer.Form.NFKD);
        if (!SmartValidator.isPaperKeyValid(activity, normalizedPhrase))
            return false;
        byte[] rawPhrase = normalizedPhrase.getBytes();
        byte[] pubKey = new BRCoreMasterPubKey(rawPhrase, true).serialize();
        byte[] pubKeyFromKeyStore = new byte[0];
        try {
            pubKeyFromKeyStore = BRKeyStore.getMasterPublicKey(activity);
        } catch (Exception e) {
            e.printStackTrace();
            BRReportsManager.reportBug(e);
        }
        Arrays.fill(rawPhrase, (byte) 0);
        return Arrays.equals(pubKey, pubKeyFromKeyStore);
    }

    public static boolean checkFirstAddress(Activity app, byte[] mpk) {
        String addressFromPrefs = BRSharedPrefs.getFirstAddress(app);

        String generatedAddress = new BRCoreMasterPubKey(mpk, false).getPubKeyAsCoreKey().address();
        if (!addressFromPrefs.equalsIgnoreCase(generatedAddress) && addressFromPrefs.length() != 0 && generatedAddress.length() != 0) {
            Log.e(TAG, "checkFirstAddress: WARNING, addresses don't match: Prefs:" + addressFromPrefs + ", gen:" + generatedAddress);
        }
        return addressFromPrefs.equals(generatedAddress);
    }

    public static String cleanPaperKey(Context activity, String phraseToCheck) {
        return Normalizer.normalize(phraseToCheck.replace("　", " ").replace("\n", " ").trim().replaceAll(" +", " "), Normalizer.Form.NFKD);
    }
}
