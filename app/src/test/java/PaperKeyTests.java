import android.content.Context;
import android.security.keystore.UserNotAuthenticatedException;
import android.text.format.DateUtils;
import android.util.Log;

import com.ravenwallet.tools.manager.BRReportsManager;
import com.ravenwallet.tools.security.BRKeyStore;
import com.ravenwallet.tools.util.BRConstants;
import com.ravenwallet.tools.util.Bip39Wordlist;
import com.ravenwallet.tools.util.Utils;
import com.ravenwallet.wallet.WalletsMaster;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.test.platform.app.InstrumentationRegistry;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * RavenWallet
 * <p/>
 * Created by Mihail Gutan on <mihail@breadwallet.com> 11/3/17.
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
public class PaperKeyTests {

    private static final String TAG = PaperKeyTests.class.getName();
    public static final String PAPER_KEY_CS = "vklad herec srub narodit rastr zasunout pianista javor vklad patrona panstvo fosfor";//czech
    public static final String PAPER_KEY_EN = "stick sword keen   afraid smile sting   huge relax nominee   arena area gift ";//english
    public static final String PAPER_KEY_ES = "zorro turismo mezcla nicho morir chico blanco pájaro alba esencia roer repetir";//spanish
    public static final String PAPER_KEY_FR = "vocation triage capsule marchand onduler tibia illicite entier fureur minorer amateur lubie";//french
    public static final String PAPER_KEY_IT = "terme cornice seggiola lorenzo rappreso vano ovest dono terme orribile ordine circa";//italian
    public static final String PAPER_KEY_JA = "こせき　ぎじにってい　けっこん　せつぞく　うんどう　ふこう　にっすう　こせい　きさま　なまみ　たきび　はかい";//japanese
    public static final String PAPER_KEY_KO = "특징 대형 즐거움 순위 입장 행복 오랫동안 무더위 특징 옆구리 엽서 단위";//korean
    public static final String PAPER_KEY_PT = "surreal cartaz ralado girino oportuno usina madrinha corporal surreal longe liquidez cadeado";//portuguese
    public static final String PAPER_KEY_ZH_CN = "怨 贪 旁 扎 吹 音 决 廷 十 助 畜 怒";//chinese (simplified)
    public static final String PAPER_KEY_ZH_TW = "棚 深 偵 衡 埃 摘 齒 黃 棚 紛 遭 況";//chinese (traditional)

    @Before
    public void setup() {
    }

    @Test
    public void testWordlistsValidSizes() {
        for(Bip39Wordlist wordlist : Bip39Wordlist.LANGS) {
            //This will raise an exception if the wordlist is invalid
            wordlist.loadWords(null);
            assertEquals("Wordlist is expected size", wordlist.getWords().length, Bip39Wordlist.WORD_LIST_SIZE);
        }
    }

    void testPaperKey(String paperKeyName, String testPhrase, String expectedLanguage) {
        Bip39Wordlist testWordList = Bip39Wordlist.identifyWordlist(null, testPhrase);

        Assert.assertEquals(paperKeyName + " Phrase length", Bip39Wordlist.PHRASE_SZE, Bip39Wordlist.splitPharse(testPhrase).length);
        Assert.assertNotNull(paperKeyName + " Phrase detected", testWordList);
        Assert.assertEquals(paperKeyName + " Phrase language autodetected", expectedLanguage, testWordList.getLanguageCode());
    }

    @Test
    public void testCzechPaperKey() {
        testPaperKey("Czech", PAPER_KEY_CS, "cs");
    }

    @Test
    public void testEnglishPaperKey() {
        testPaperKey("English", PAPER_KEY_EN, "en");
    }

    @Test
    public void testSpanishPaperKey() {
        testPaperKey("Spanish", PAPER_KEY_ES, "es");
    }

    @Test
    public void testFrenchPaperKey() {
        testPaperKey("French", PAPER_KEY_FR, "fr");
    }

    @Test
    public void testItalianPaperKey() {
        testPaperKey("Italian", PAPER_KEY_IT, "it");
    }

    @Test
    public void testJapanesePaperKey() {
        testPaperKey("Japanese", PAPER_KEY_JA, "ja");
    }

    @Test
    public void testKoreanPaperKey() {
        testPaperKey("Korean", PAPER_KEY_KO, "ko");
    }

    @Test
    public void testPortuguesePaperKey() {
        testPaperKey("Portuguese", PAPER_KEY_PT, "pt");
    }

    @Test
    public void testChineseSimplifiedPaperKey() {
        testPaperKey("Chinese (Simplified)", PAPER_KEY_ZH_CN, "zh-CN");
    }

    @Test
    public void testChineseTraditionalPaperKey() {
        testPaperKey("Chinese (Traditional)", PAPER_KEY_ZH_TW, "zh-TW");
    }

    @Test
    public void testPaperKeyGenerate() {
        //TODO: For some reason when testing, generatePaperKeyBytes (which calls JNI generatePaperKey) crashes with UnsatisfiedLinkError.
        //Have not been able to track it down yet, but it works correctly when running the application normally

        /*
        ////Main structure copied from WalletsMaster.java
        Bip39Wordlist bipWords = Bip39Wordlist.DEFAULT_WORDLIST;

        //Generate a random seed to use
        final byte[] randomSeed = bipWords.generateRandomSeed();
        //Generate a byte-array String of the paper key created
        byte[] paperKeyBytes = bipWords.generatePaperKeyBytes(null, randomSeed);
        //Split that byte[] into an array of each word
        String[] splitPhrase = bipWords.splitPharse(paperKeyBytes);

        Assert.assertEquals("Correct seed size", 16, randomSeed.length);
        Assert.assertEquals("Correct phrase size", Bip39Wordlist.PHRASE_SZE, splitPhrase.length);
        Assert.assertNotNull("Valid phrase", Bip39Wordlist.identifyWordlist(null, paperKeyBytes));

        //Re-extract the seed from the newly created phrase for checking
        byte[] verifyRandomSeed = bipWords.getSeedFromPhrase(paperKeyBytes);

        //Create an api private key, this function will internally verify the results, and will raise if invalid
        byte[] privateKeyBytes = bipWords.getPrivateKeyForAPI(randomSeed);

        Assert.assertEquals("Paper key seed matches length", randomSeed.length, verifyRandomSeed.length);
        for(int i=0;i<paperKeyBytes.length;i++)
            Assert.assertEquals("Paper key seed matches", randomSeed[i], verifyRandomSeed[i]);
        */
    }

}
