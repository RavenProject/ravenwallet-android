package com.ravenwallet.tools.util;

import androidx.test.espresso.core.internal.deps.guava.collect.Lists;


import com.ravenwallet.BuildConfig;

import java.math.RoundingMode;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * RavenWallet
 * <p/>
 * Created by Mihail Gutan <mihail@breadwallet.com> on 2/16/16.
 * Copyright (c) 2016 breadwallet LLC
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

public class BRConstants {

    /**
     * Native library name
     */
    public static final String NATIVE_LIB_NAME = "core";

    /**
     * Permissions
     */
    public static final int CAMERA_REQUEST_ID = 34;
    public static final int GEO_REQUEST_ID = 35;
    public static final int CAMERA_REQUEST_GLIDERA_ID = 36;

    /**
     * Request codes for auth
     */
    public static final int SHOW_PHRASE_REQUEST_CODE = 111;
    public static final int PAY_REQUEST_CODE = 112;
    public static final int CANARY_REQUEST_CODE = 113;
    public static final int PUT_PHRASE_NEW_WALLET_REQUEST_CODE = 114;
    public static final int PUT_PHRASE_RECOVERY_WALLET_REQUEST_CODE = 115;
    public static final int PAYMENT_PROTOCOL_REQUEST_CODE = 116;
    public static final int REQUEST_PHRASE_BITID = 117;
    public static final int PROVE_PHRASE_REQUEST = 119;

    /**
     * Request codes for take picture
     */
    public static final int SCANNER_REQUEST = 201;
    public static final int REQUEST_IMAGE_CAPTURE = 203;
    public static final int ADDRESS_SCANNER_REQUEST = 204;
    public static final int IPFS_HASH_SCANNER_REQUEST = 205;
    public static final int SELECT_FROM_ADDRESS_BOOK_REQUEST = 206;

    public static final String CANARY_STRING = "canary";
    public static final String LITTLE_CIRCLE = "\u2022";
    public static final String OWNER_SUFFIX = "!";
    public static String SUPPORT_EMAIL = "feedback@ravencoin.org";

    /**
     * Currency units
     */
    public static final int DEFAULT_FRACTION_DIGITS = 8;

    public static final int CURRENT_UNIT_URVN = 0;
    public static final int CURRENT_UNIT_MRVN = 1;
    public static final int CURRENT_UNIT_RAVENS = 2;

    public static final String symbolRotunda = "\uA75A";
    public static final String symbolRavenSecondary = "\uA75B";
    public static final String symbolRavenPrimary = "\uA75A";

    public static final long PASS_CODE_TIME_LIMIT = TimeUnit.MILLISECONDS.convert(6, TimeUnit.DAYS);

    public static boolean PLATFORM_ON = true;
    public static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_EVEN;
    public static final boolean WAL = true;

    public static final long MAX_ASSET_QUANTITY = 21000000 * 1000L;
    public static final int MAX_ASSET_NAME_LENGTH = 30;
    public static final int MAX_ADDRESS_NAME_LENGTH = 30;

    public static final double SATOSHIS = 100000000;

    public static final long CREATION_FEE = 500L;
    public static final long REISSUE_FEE = 100L;
    public static final long SUB_FEE = 100L;
    public static final long UNIQUE_FEE = 5L;
    public static final long CONFIRMS_COUNT = 5L;

    /**
     * Support Center article ids.
     */

    public static final String loopBug = "authenticated";
    public static final String displayCurrency = "app-settings/raven-currency.html";
    public static final String recoverWallet = "app-settings/previous-wallet.html";
    public static final String reScan = "app-settings/sync.html";
    public static final String securityCenter = "security";
    public static final String paperKey = "security/paper-key.html";
    public static final String enableFingerprint = "security/fingerprint.html";
    public static final String fingerprintSpendingLimit = "fingerprint-spending-limit";
    public static final String transactionDetails = "send-receive/rvn-transaction.html";
    public static final String manageWallet = "app-settings";
    public static final String receive = "send-receive";
    public static final String send = "send-receive";
    public static final String requestAmount = "send-receive/request-amount.html";
    public static final String walletDisabled = "wallet-disabled";
    public static final String resetPinWithPaperKey = "reset-pin-paper-key";

    public static final String setPin = "security/pin.html";
    public static final String importWallet = "Miscellaneous/import-paper.html";
    public static final String writePhrase = "security/down-paper-key.html";
    public static final String confirmPhrase = "security/confirm-key.html";
    public static final String startView = "start-view";
    public static final String wipeWallet = "app-settings/wipe-wallet.html";
    public static final int SEQUENCE_EXTERNAL_CHAIN = 0;
    public static final int SEQUENCE_INTERNAL_CHAIN = 1;
    public static final String IP_REGEX = "^\\d{1,3}(\\.(\\d{1,3}(\\.(\\d{1,3}(\\.(\\d{1,3})?)?)?)?)?)?";
    public static final String PRIVACY_URL = "https://ravencoin.org/mobilewallet/support/privacy.html";

    public static final String IPFS_URL_FORMAT = "https://%s/ipfs/%s";
    //NOTE: Additional hosts are defined in IPFSGatewayActivity.java
    public static final String IPFS_DEFAULT_HOST = "ipfs.io";

    public static final List<String> STR_ISSUE_ASSET_BURN_ADDESSES =
            Lists.newArrayList("RXissueAssetXXXXXXXXXXXXXXXXXhhZGt",
                    "n1issueAssetXXXXXXXXXXXXXXXXWdnemQ");
    public static final List<String> STR_REISSUE_ASSET_BURN_ADDESSES =
            Lists.newArrayList("RXReissueAssetXXXXXXXXXXXXXXVEFAWu",
                    "n1ReissueAssetXXXXXXXXXXXXXXWG9NLd");
    public static final List<String> STR_REISSUE_SUB_ASSET_BURN_ADDESSES =
            Lists.newArrayList("RXissueSubAssetXXXXXXXXXXXXXWcwhwL",
                    "n1issueSubAssetXXXXXXXXXXXXXbNiH6v");
    public static final List<String> STR_UNIQUE_ASSET_BURN_ADDESSES =
            Lists.newArrayList("RXissueUniqueAssetXXXXXXXXXXWEAe58",
                    "n1issueUniqueAssetXXXXXXXXXXS4695i");
    public static final List<String> STR_BURN_ASSET_ADDESSES =
            Lists.newArrayList("RXBurnXXXXXXXXXXXXXXXXXXXXXXWUo9FV",
                    "n1BurnXXXXXXXXXXXXXXXXXXXXXXU1qejP");

    //fetch Utxos urls
    public static String fetchRvnUtxosPath() {
        return BuildConfig.TESTNET ? "https://api.testnet.ravencoin.org/api/addr/%s/utxo" : "https://api.ravencoin.com/api/addr/%s/utxo";
    }

    public static String fetchAssetUtxosPath() {
        return BuildConfig.TESTNET ? "https://api.testnet.ravencoin.org/api/addr/%s/asset/*/utxo" : "https://api.ravencoin.com/api/addr/%s/asset/*/utxo";
    }

    public static String fetchTxsPath() {
        return BuildConfig.TESTNET ? "https://api.testnet.ravencoin.org/api/addr/%s" : "https://api.ravencoin.com/api/addr/%s";
    }

    public static String networkUrl() {
        return BuildConfig.TESTNET ? "https://api.testnet.ravencoin.org/api/addr/%s/utxo" : "https://api.ravencoin.com/api/addr/%s/utxo";
    }

    public static int getNodePort() {
        return BuildConfig.TESTNET ? 18770 : 8767;
    }

    private BRConstants() {
    }


}
