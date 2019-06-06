import com.platform.assets.AssetsValidation;

import org.junit.Test;

import static org.hamcrest.Matchers.is;
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
public class AssetNameTests {

    @Test
    public void testRegularNames() {
        assertThat(AssetsValidation.isAssetNameValid("MIN"), is(true));
        assertThat(AssetsValidation.isAssetNameValid("MAX_ASSET_IS_30_CHARACTERS_LNG"), is(true));

        assertThat(AssetsValidation.isAssetNameValid("MAX_ASSET_IS_31_CHARACTERS_LONG"), is(false));
        assertThat(AssetsValidation.isAssetNameValid("A_BCDEFGHIJKLMNOPQRSTUVWXY.Z"), is(true));
        assertThat(AssetsValidation.isAssetNameValid("0_12345678.9"), is(true));

        assertThat(AssetsValidation.isAssetNameValid("NO"), is(false));
        assertThat(AssetsValidation.isAssetNameValid("nolower"), is(false));
        assertThat(AssetsValidation.isAssetNameValid("NO SPACE"), is(false));
        assertThat(AssetsValidation.isAssetNameValid("(#&$(&*^%$))"), is(false));

        assertThat(AssetsValidation.isAssetNameValid("_ABC"), is(false));
        assertThat(AssetsValidation.isAssetNameValid("ABC_"), is(false));
        assertThat(AssetsValidation.isAssetNameValid(".ABC"), is(false));
        assertThat(AssetsValidation.isAssetNameValid("ABC."), is(false));
        assertThat(AssetsValidation.isAssetNameValid("AB..C"), is(false));
        assertThat(AssetsValidation.isAssetNameValid("A__BC"), is(false));
        assertThat(AssetsValidation.isAssetNameValid("A._BC"), is(false));
        assertThat(AssetsValidation.isAssetNameValid("AB_.C"), is(false));
    }

    @Test
    public void testRavencoinNotAllowed() {
        assertThat(AssetsValidation.isAssetNameValid("RVN"), is(false));
        assertThat(AssetsValidation.isAssetNameValid("RAVEN"), is(false));
        assertThat(AssetsValidation.isAssetNameValid("RAVENCOIN"), is(false));
    }

    @Test
    public void testRavencoinAllowed() {
        assertThat(AssetsValidation.isAssetNameValid("RAVEN.COIN"), is(true));
        assertThat(AssetsValidation.isAssetNameValid("RAVEN_COIN"), is(true));
        assertThat(AssetsValidation.isAssetNameValid("RVNSPYDER"), is(true));
        assertThat(AssetsValidation.isAssetNameValid("SPYDERRVN"), is(true));
        assertThat(AssetsValidation.isAssetNameValid("RAVENSPYDER"), is(true));
        assertThat(AssetsValidation.isAssetNameValid("SPYDERAVEN"), is(true));
        assertThat(AssetsValidation.isAssetNameValid("BLACK_RAVENS"), is(true));
        assertThat(AssetsValidation.isAssetNameValid("SERVNOT"), is(true));
    }

    @Test
    public void testAssetNameValid() {
        assertThat(AssetsValidation.isAssetNameValid("Asset".toUpperCase()), is(true));
    }

    @Test
    public void testAssetNameEmpty() {
        assertThat(AssetsValidation.isAssetNameValid(""), is(false));
    }

    @Test
    public void testAssetNameInValid() {
        assertThat(AssetsValidation.isAssetNameValid("Asset"), is(false));
    }

    @Test
    public void testSubAssetNameValid() {
        assertThat(AssetsValidation.isAssetNameValid("Asset/sub".toUpperCase()), is(true));
    }

    @Test
    public void testSubAssetNameEmpty() {
        assertThat(AssetsValidation.isAssetNameValid("Asset/".toUpperCase()), is(false));
    }

    @Test
    public void testSubAssetNameInvalid() {
        assertThat(AssetsValidation.isAssetNameValid("ASSET/sub"), is(false));
    }

    @Test
    public void testUniqueAssetNameValid() {
        assertThat(AssetsValidation.isAssetNameValid("Asset#unique".toUpperCase()), is(true));
    }

    @Test
    public void testUniqueAssetNameEmpty() {
        assertThat(AssetsValidation.isAssetNameValid("Asset#".toUpperCase()), is(false));
    }

}
