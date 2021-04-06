package com.ravenwallet.tools.manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;


import com.ravenwallet.tools.util.BRConstants;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
import java.util.UUID;


/**
 * RavenWallet
 * <p>
 * Created by Mihail Gutan <mihail@breadwallet.com> on 6/13/16.
 * Copyright (c) 2016 breadwallet LLC
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

public class BRSharedPrefs {
    public static final String TAG = BRSharedPrefs.class.getName();

    public static List<OnIsoChangedListener> isoChangedListeners = new ArrayList<>();
    public static final String PREFS_NAME = "MyPrefsFile";
    private static final String CURRENT_WALLET_CURRENCY_CODE = "currentWalletIso";
    private static final String LAST_RESCAN_MODE_USED = "lastRescanModeUsed_RVN";
    private static final String LAST_SEND_TRANSACTION_BLOCK_HEIGHT = "lastSendTransactionBlockheight_RVN";
    private static final String RESCAN_TIME = "rescanTime_RVN";

    public interface OnIsoChangedListener {
        void onIsoChanged(String iso);
    }

    public static void addIsoChangedListener(OnIsoChangedListener listener) {
        if (isoChangedListeners == null) {
            isoChangedListeners = new ArrayList<>();
        }
        if (!isoChangedListeners.contains(listener))
            isoChangedListeners.add(listener);
    }

    public static void removeListener(OnIsoChangedListener listener) {
        if (isoChangedListeners == null) {
            isoChangedListeners = new ArrayList<>();
        }
        isoChangedListeners.remove(listener);

    }

    public static String getPreferredFiatIso(Context context) {
        SharedPreferences settingsToGet = context.getSharedPreferences(PREFS_NAME, 0);
        String defIso;
        try {
            defIso = Currency.getInstance(Locale.getDefault()).getCurrencyCode();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            defIso = Currency.getInstance(Locale.US).getCurrencyCode();
        }
        return settingsToGet.getString("currentCurrency", defIso);
    }

    public static void putPreferredFiatIso(Context context, String iso) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("currentCurrency", iso.equalsIgnoreCase(Locale.getDefault().getISO3Language()) ? null : iso);
        editor.apply();

        for (OnIsoChangedListener listener : isoChangedListeners) {
            if (listener != null) listener.onIsoChanged(iso);
        }

    }

    public static String getPreferredIPFSGateway(Context context) {
        SharedPreferences settingsToGet = context.getSharedPreferences(PREFS_NAME, 0);
        return settingsToGet.getString("ipfsGateway", BRConstants.IPFS_DEFAULT_HOST);
    }

    public static void putPreferredIPFSGateway(Context context, String gateway) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("ipfsGateway", gateway);
        editor.apply();
    }

    public static boolean getExpertMode(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean("expertMode", false);
    }

    public static void putExpertMode(Context context, boolean check) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("expertMode", check);
        editor.apply();
    }

    public static boolean getShowAddressInput(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean("isShowAddressInput", true);
    }

    public static void putShowAddressInput(Context context, boolean check) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("isShowAddressInput", check);
        editor.apply();
    }

    public static boolean getPhraseWroteDown(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean("phraseWritten", false);
    }

    public static void putPhraseWroteDown(Context context, boolean check) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("phraseWritten", check);
        editor.apply();
    }

    public static boolean getGreetingsShown(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean("greetingsShown", false);

    }

    public static void putGreetingsShown(Context context, boolean shown) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("greetingsShown", shown);
        editor.apply();
    }

    public static boolean getFavorStandardFee(Context context, String iso) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean("favorStandardFee" + iso.toUpperCase(), true);

    }

    public static void putFavorStandardFee(Context context, String iso, boolean favorStandardFee) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("favorStandardFee" + iso.toUpperCase(), favorStandardFee);
        editor.apply();
    }

    public static String getReceiveAddress(Context context, String iso) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString("receive_address" + iso.toUpperCase(), "");
    }

    public static void putReceiveAddress(Context ctx, String tmpAddr, String iso) {
        SharedPreferences.Editor editor = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
        editor.putString("receive_address" + iso.toUpperCase(), tmpAddr);
        editor.apply();
    }

    public static String getFirstAddress(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString("firstAddress", "");
    }

    public static void putFirstAddress(Context context, String firstAddress) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("firstAddress", firstAddress);
        editor.apply();
    }

    public static long getFeePerKb(Context context, String iso) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getLong("feeKb" + iso.toUpperCase(), 0);
    }

    public static void putFeePerKb(Context context, String iso, long fee) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong("feeKb" + iso.toUpperCase(), fee);
        editor.apply();
    }

    public static long getEconomyFeePerKb(Context context, String iso) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getLong("economyFeeKb" + iso.toUpperCase(), 0);
    }

    public static void putEconomyFeePerKb(Context context, String iso, long fee) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong("economyFeeKb" + iso.toUpperCase(), fee);
        editor.apply();
    }

    public static long getCachedBalance(Context context, String iso) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getLong("balance_" + iso.toUpperCase(), 0);
    }

    public static void putCachedBalance(Context context, String iso, long balance) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong("balance_" + iso.toUpperCase(), balance);
        editor.apply();
    }

    public static long getSecureTime(Context activity) {
        SharedPreferences prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getLong("secureTime", System.currentTimeMillis() / 1000);
    }

    //secure time from the server
    public static void putSecureTime(Context activity, long date) {
        SharedPreferences prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong("secureTime", date);
        editor.apply();
    }

    public static long getLastSyncTime(Context activity, String iso) {
        SharedPreferences prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getLong("lastSyncTime_" + iso.toUpperCase(), 0);
    }

    public static void putLastSyncTime(Context activity, String iso, long time) {
        SharedPreferences prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong("lastSyncTime_" + iso.toUpperCase(), time);
        editor.apply();
    }


    public static String getLastRescanModeUsed(Context activity) {
        SharedPreferences prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(LAST_RESCAN_MODE_USED, null);
    }

    public static void putLastRescanModeUsed(Context activity, String mode) {
        SharedPreferences prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(LAST_RESCAN_MODE_USED, mode);
        editor.apply();
    }

    public static long getLastSendTransactionBlockheight(Context activity) {
        SharedPreferences prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getLong(LAST_SEND_TRANSACTION_BLOCK_HEIGHT, 0);
    }

    public static void putLastSendTransactionBlockheight(Context activity, long blockHeight) {
        SharedPreferences prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(LAST_SEND_TRANSACTION_BLOCK_HEIGHT, blockHeight);
        editor.apply();
    }

    public static long getFeeTime(Context activity, String iso) {
        SharedPreferences prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getLong("feeTime_" + iso.toUpperCase(), 0);
    }

    public static void putFeeTime(Context activity, String iso, long feeTime) {
        SharedPreferences prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong("feeTime_" + iso.toUpperCase(), feeTime);
        editor.apply();
    }

    public static List<Integer> getBitIdNonces(Context activity, String key) {
        SharedPreferences prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String result = prefs.getString(key, null);
        List<Integer> list = new ArrayList<>();
        try {
            JSONArray arr = new JSONArray(result);
            for (int i = 0; i < arr.length(); i++) {
                int a = arr.getInt(i);
                Log.d("found a nonce: ", a + "");
                list.add(a);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public static void putBitIdNonces(Context activity, List<Integer> nonces, String key) {
        JSONArray arr = new JSONArray();
        arr.put(nonces);
        SharedPreferences prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key, arr.toString());
        editor.apply();
    }

    public static boolean getAllowSpend(Context activity, String iso) {
        SharedPreferences prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean("allowSpend_" + iso.toUpperCase(), true);
    }

    public static void putAllowSpend(Context activity, String iso, boolean allow) {
        SharedPreferences prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("allowSpend_" + iso.toUpperCase(), allow);
        editor.apply();
    }

    //if the user prefers all in crypto units, not fiat currencies
    public static boolean isCryptoPreferred(Context activity) {
        SharedPreferences prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean("priceInCrypto", true);
    }

    //if the user prefers all in crypto units, not fiat currencies
    public static void setIsCryptoPreferred(Context activity, boolean b) {
        SharedPreferences prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("priceInCrypto", b);
        editor.apply();
    }

    public static boolean getUseFingerprint(Context activity) {
        SharedPreferences prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean("useFingerprint", false);
    }

    public static void putUseFingerprint(Context activity, boolean use) {
        SharedPreferences prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("useFingerprint", use);
        editor.apply();
    }

    public static boolean getFeatureEnabled(Context activity, String feature) {
        SharedPreferences prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(feature, false);
    }

    public static void putFeatureEnabled(Context activity, boolean enabled, String feature) {
        SharedPreferences prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(feature, enabled);
        editor.apply();
    }

    public static String getCurrentWalletIso(Context activity) {
//        Log.d(TAG, "getCurrentWalletIso() Activity -> " + activity.getClass().getSimpleName());
        SharedPreferences prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
//        Log.d(TAG, "Getting current wallet ISO -> " + prefs.getString("currentWalletIso", "RVN"));
        return prefs.getString(CURRENT_WALLET_CURRENCY_CODE, "RVN");
    }

    public static void putCurrentWalletIso(Context activity, String iso) {
        Log.d(TAG, "putCurrentWalletIso(), ISO -> " + iso);
        if (iso == null) throw new NullPointerException("cannot be null");
        SharedPreferences prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(CURRENT_WALLET_CURRENCY_CODE, iso);
        editor.apply();
    }

    public static boolean getGeoPermissionsRequested(Context activity) {
        SharedPreferences prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean("geoPermissionsRequested", false);
    }

    public static void putGeoPermissionsRequested(Context activity, boolean requested) {
        SharedPreferences prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("geoPermissionsRequested", requested);
        editor.apply();
    }

    public static long getStartHeight(Context context, String iso) {
        SharedPreferences settingsToGet = context.getSharedPreferences(PREFS_NAME, 0);
        return settingsToGet.getLong("startHeight_" + iso.toUpperCase(), 0);
    }

    public static void putStartHeight(Context context, String iso, long startHeight) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putLong("startHeight_" + iso.toUpperCase(), startHeight);
        editor.apply();
    }

    public static long getLastRescanTime(Context context) {
        SharedPreferences settingsToGet = context.getSharedPreferences(PREFS_NAME, 0);
        return settingsToGet.getLong(RESCAN_TIME, 0);
    }

    public static void putLastRescanTime(Context context, long time) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putLong(RESCAN_TIME, time);
        editor.apply();
    }

    public static int getLastBlockHeight(Context context, String iso) {
        SharedPreferences settingsToGet = context.getSharedPreferences(PREFS_NAME, 0);
        return settingsToGet.getInt("lastBlockHeight" + iso.toUpperCase(), 0);
    }

    public static void putLastBlockHeight(Context context, String iso, int lastHeight) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("lastBlockHeight" + iso.toUpperCase(), lastHeight);
        editor.apply();
    }

    public static boolean getScanRecommended(Context context, String iso) {
        SharedPreferences settingsToGet = context.getSharedPreferences(PREFS_NAME, 0);
        return settingsToGet.getBoolean("scanRecommended_" + iso.toUpperCase(), false);
    }

    public static void putScanRecommended(Context context, String iso, boolean recommended) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("scanRecommended_" + iso.toUpperCase(), recommended);
        editor.apply();
    }

    public static int getCryptoDenomination(Context context, String iso) {
        SharedPreferences settingsToGet = context.getSharedPreferences(PREFS_NAME, 0);
        return settingsToGet.getInt("currencyUnit", BRConstants.CURRENT_UNIT_RAVENS);
    }

    public static void putCryptoDenomination(Context context, String iso, int unit) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("currencyUnit", unit);
        editor.apply();
    }

    public static String getDeviceId(Context context) {
        SharedPreferences settingsToGet = context.getSharedPreferences(PREFS_NAME, 0);
        String deviceId = settingsToGet.getString("userId", "");
        if (deviceId.isEmpty()) setDeviceId(context, UUID.randomUUID().toString());
        return (settingsToGet.getString("userId", ""));
    }

    private static void setDeviceId(Context context, String uuid) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("userId", uuid);
        editor.apply();
    }

    public static void clearAllPrefs(Context activity) {
        SharedPreferences.Editor editor = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
        editor.clear();
        editor.apply();
    }

    public static boolean getShowNotification(Context context) {
        SharedPreferences settingsToGet = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return settingsToGet.getBoolean("showNotification", false);
    }

    public static void putShowNotification(Context context, boolean show) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("showNotification", show);
        editor.apply();
    }

    public static boolean getShareData(Context context) {
        SharedPreferences settingsToGet = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return settingsToGet.getBoolean("shareData", false);
    }

    public static void putShareData(Context context, boolean show) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("shareData", show);
        editor.apply();
    }

    public static boolean getShareDataDismissed(Context context) {
        SharedPreferences settingsToGet = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return settingsToGet.getBoolean("shareDataDismissed", false);
    }

    public static void putShareDataDismissed(Context context, boolean dismissed) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("shareDataDismissed", dismissed);
        editor.apply();
    }

    public static String getTrustNode(Context context, String iso) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString("trustNode_" + iso.toUpperCase(), "");
    }

    public static void putTrustNode(Context context, String iso, String trustNode) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("trustNode_" + iso.toUpperCase(), trustNode);
        editor.apply();
    }
}