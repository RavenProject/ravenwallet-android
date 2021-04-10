import android.content.Context;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.ravenwallet.core.BRCoreMasterPubKey;
import com.ravenwallet.tools.security.SmartValidator;
import com.ravenwallet.tools.util.BRConstants;
import com.ravenwallet.tools.util.Bip39Reader;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
public class PaperKeyAndroidTests {
    public static final String PAPER_KEY_EN = "stick sword keen   afraid smile sting   huge relax nominee   arena area gift ";//english
    public static final String ADDRESS_EN   = "RUFUNXFYWeTzyAoQTb5rnPoQrmxr2MTVF5";

    public static final String PAPER_KEY_ZH = "怨 贪 旁 扎 吹 音 决 廷 十 助 畜 怒";//chinese (simplified)
    public static final String ADDRESS_ZH   = "RQn9TZgcCut1mYzEScJ7vmNA6SHEEWiC3m";

    static {
        System.loadLibrary(BRConstants.NATIVE_LIB_NAME);
    }

    public void testPaperKey(String paperKey, String address) {
        Context app = InstrumentationRegistry.getInstrumentation().getTargetContext();
        String cleanKey = SmartValidator.cleanPaperKey(app, paperKey);

        boolean isValid = SmartValidator.isPaperKeyValid(app, cleanKey);
        assertThat(isValid, is(true));

        // Don't use SmartValidator here, so we can avoid the dependency on the BRKeyStore.
        String addr = new BRCoreMasterPubKey(cleanKey.getBytes(), true).getPubKeyAsCoreKey().address();
        boolean isCorrect = addr.equals(address);
        assertThat(isCorrect, is(true));
    }

    @Test
    public void testEnglishPaperKey() {
        Locale.setDefault(Locale.ENGLISH);
        testPaperKey(PAPER_KEY_EN, ADDRESS_EN);
    }

    @Test
    public void testChinesePaperKey() {
        Locale.setDefault(Locale.CHINESE);
        testPaperKey(PAPER_KEY_ZH, ADDRESS_ZH);
    }

    @Test
    public void testEnglishChinesePaperKey() {
        Locale.setDefault(Locale.ENGLISH);
        testPaperKey(PAPER_KEY_ZH, ADDRESS_ZH);
    }

    @Test
    public void testChineseEnglishPaperKey() {
        Locale.setDefault(Locale.CHINESE);
        testPaperKey(PAPER_KEY_EN, ADDRESS_EN);
    }

    @Test
    public void testChineseLocale() {
        Context app = InstrumentationRegistry.getInstrumentation().getTargetContext();

        // Index 9 is the first character that is different between simplified and traditional.
        // TODO: actually detecting traditional may also require testing the country or other fields;
        //   for example, Locale.TRADITIONAL_CHINESE will evaluate to "zh" as the language.
        Locale.setDefault(new Locale("zh_tw"));
        List<String> trad = Bip39Reader.bip39List(app, Locale.getDefault().getLanguage());
        assertThat(trad.get(9), is("這"));

        Locale.setDefault(Locale.SIMPLIFIED_CHINESE);
        List<String> simp = Bip39Reader.bip39List(app, Locale.getDefault().getLanguage());
        assertThat(simp.get(9), is("这"));
    }
}
