package com.ravenwallet.wallet.abstracts;

import android.content.Context;
import androidx.annotation.WorkerThread;

import com.ravenwallet.core.BRCoreAddress;
import com.ravenwallet.core.BRCoreMerkleBlock;
import com.ravenwallet.core.BRCorePeer;
import com.ravenwallet.core.BRCorePeerManager;
import com.ravenwallet.core.BRCoreTransaction;
import com.ravenwallet.core.BRCoreWallet;
import com.ravenwallet.presenter.entities.CurrencyEntity;
import com.ravenwallet.presenter.entities.TxUiHolder;
import com.ravenwallet.wallet.configs.WalletUiConfiguration;

import java.math.BigDecimal;
import java.util.List;


public interface BaseWalletManager {

    //get the core wallet
    BRCoreWallet getWallet();

    //get the core wallet
    int getForkId();

    //get the core peerManager
    BRCorePeerManager getPeerManager();

    //sign and publish the tx using the seed
    byte[] signAndPublishTransaction(BRCoreTransaction tx, byte[] seed);

    void addBalanceChangedListener(OnBalanceChangedListener list);

    void addTxStatusUpdatedListener(OnTxStatusUpdatedListener list);

    void addSyncListeners(SyncListener list);

    void removeSyncListener(SyncListener listener);

    void addTxListModifiedListener(OnTxListModified list);

    //get a list of all the transactions sorted by timestamp
    BRCoreTransaction[] getTransactions();

    void updateFee(Context app);

    //get the core address and store it locally
    void refreshAddress(Context app);

    //get a list of all the transactions UI holders sorted by timestamp
    List<TxUiHolder> getTxUiHolders();

    //generate the wallet if needed
    boolean generateWallet(Context app);

    //init the current wallet
    boolean connectWallet(Context app);

    //get the currency symbol e.g. Bitcoin - ₿, Ether - Ξ
    String getSymbol(Context app);

    //get the currency denomination e.g. Bitcoin - BTC, Ether - ETH
    String getIso(Context app);

    //get the currency scheme (bitcoin or bitcoincash)
    String getScheme(Context app);

    //get the currency name e.g. Bitcoin
    String getName(Context app);

    //get the currency denomination e.g. BCH, mBCH, Bits
    String getDenomination(Context app);

    //get the wallet's receive address
    BRCoreAddress getReceiveAddress(Context app);

    //decorate an address to a particular currency, if needed (like BCH address format)
    String decorateAddress(Context app, String addr);

    //convert to raw address to a particular currency, if needed (like BCH address format)
    String undecorateAddress(Context app, String addr);

    //get the number of decimal places to use for this currency
    int getMaxDecimalPlaces(Context app);

    //get the cached balance in the smallest unit:  satoshis.
    long getCachedBalance(Context app);

    //get the total amount sent in the smallest crypto unit:  satoshis.
    long getTotalSent(Context app);

    //wipe all wallet data
    void wipeData(Context app);

    //load the txs from DB
    BRCoreTransaction[] loadTransactions();

    //load the blocks from DB
    BRCoreMerkleBlock[] loadBlocks();

    //load the peers from DB
    BRCorePeer[] loadPeers();

    void syncStarted();

    void syncStopped(String error);

    void onTxAdded(BRCoreTransaction transaction);

    void onTxDeleted(String hash, int notifyUser, int recommendRescan);

    void onTxUpdated(String hash, int blockHeight, int timeStamp);

    void txPublished(final String error);

    void balanceChanged(long balance);

    void txStatusUpdate();

    void saveBlocks(boolean replace, BRCoreMerkleBlock[] blocks);

    void savePeers(boolean replace, BRCorePeer[] peers);

    boolean networkIsReachable();

    /**
     * @param balance - the balance to be saved in the smallest unit.(e.g. satoshis)
     */
    void setCashedBalance(Context app, long balance);

    //return the maximum amount for this currency
    BigDecimal getMaxAmount(Context app);

    /**
     * @return - the wallet's Ui configuration
     */
    WalletUiConfiguration getUiConfiguration();

    /**
     * @return - the wallet's currency exchange rate in the user's favorite fiat currency (e.g. dollars)
     */
    BigDecimal getFiatExchangeRate(Context app);

    /**
     * @return - the total balance amount in the user's favorite fiat currency (e.g. dollars)
     */
    BigDecimal getFiatBalance(Context app);

    /**
     * @param amount - the smallest denomination amount in current wallet's crypto (e.g. Satoshis)
     * @param ent - provide a currency entity if needed
     * @return - the fiat value of the amount in crypto (e.g. dollars)
     * or null if there is no fiat exchange data from the API yet
     */
    BigDecimal getFiatForSmallestCrypto(Context app, BigDecimal amount, CurrencyEntity ent);

    /**
     * @param amount - the amount in the user's favorite fiat currency (e.g. dollars)
     * @return - the crypto value of the amount in the current favorite denomination (e.g. BTC, mBTC, Bits..)
     * or null if there is no fiat exchange data from the API yet
     */
    BigDecimal getCryptoForFiat(Context app, BigDecimal amount);

    /**
     * @param amount - the smallest denomination amount in crypto (e.g. satoshis)
     * @return - the crypto value of the amount in the current favorite denomination (e.g. BTC, mBTC, Bits..)
     */
    BigDecimal getCryptoForSmallestCrypto(Context app, BigDecimal amount);

    /**
     * @param amount - the crypto value of the amount in the current favorite denomination (e.g. BTC, mBTC, Bits..)
     * @return - the smallest denomination amount in crypto (e.g. satoshis)
     */
    BigDecimal getSmallestCryptoForCrypto(Context app, BigDecimal amount);

    /**
     * @param amount - the fiat amount (e.g. dollars)
     * @return - the crypto value of the amount in the smallest denomination (e.g. satothis)
     * or null if there is no fiat exchange data from the API yet
     */
    BigDecimal getSmallestCryptoForFiat(Context app, BigDecimal amount);

    //get confirmation number
    @WorkerThread
    long getRelayCount(byte[] txHash);

    //get the syncing progress
    @WorkerThread
    double getSyncProgress(long startHeight);

    //get the connection status 0 - Disconnected, 1 - Connecting, 2 - Connected, 3 - Unknown
    @WorkerThread
    double getConnectStatus();

    //Rescan the wallet (PeerManager for Bitcoin)
    @WorkerThread
    void rescan(Context app);
}
