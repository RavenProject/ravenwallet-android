package com.ravenwallet.tools.threads;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import androidx.annotation.Nullable;


import com.ravenwallet.R;
import com.ravenwallet.core.BRCoreAddress;
import com.ravenwallet.core.BRCoreKey;
import com.ravenwallet.core.BRCoreTransaction;
import com.ravenwallet.core.BRCoreTransactionInput;
import com.ravenwallet.core.BRCoreTransactionOutput;
import com.ravenwallet.presenter.activities.settings.ImportActivity;
import com.ravenwallet.presenter.customviews.BRDialogView;
import com.ravenwallet.tools.animation.BRDialog;
import com.ravenwallet.tools.api.ApiUTxo;
import com.ravenwallet.tools.manager.BRApiManager;
import com.ravenwallet.tools.manager.BRReportsManager;
import com.ravenwallet.tools.threads.executor.BRExecutor;
import com.ravenwallet.tools.util.CurrencyUtils;
import com.ravenwallet.tools.util.TypesConverter;
import com.ravenwallet.wallet.RvnWalletManager;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static com.ravenwallet.tools.util.BRConstants.SEQUENCE_EXTERNAL_CHAIN;
import static com.ravenwallet.tools.util.BRConstants.SEQUENCE_INTERNAL_CHAIN;


public class Import12WordsTask extends AsyncTask<Void, Void, Void> {

    private Context context;
    private List<ApiUTxo> mListUtxos;
    private List<BRCoreKey> mKeys;
    private byte[] phrase;
    //    private BRCoreWallet mWallet;
    private int countFails;
    private int chain;
    private Import12WordsListener listener;
    private RvnWalletManager rvnWalletManager;
    private ProgressDialog progressBar;
    private String bipToUse;

    public Import12WordsTask(Activity context, byte[] phraseStr, String bip, Import12WordsListener listener) {
        this.context = context;
        this.listener = listener;
        this.mListUtxos = new ArrayList<>();
        this.mKeys = new ArrayList<>();
        this.phrase = phraseStr;
        this.bipToUse = bip;
        this.rvnWalletManager = RvnWalletManager.getInstance(context);
//        this.mWallet = rvnWalletManager.getWallet();
        countFails = 0;
        chain = SEQUENCE_EXTERNAL_CHAIN;
        progressBar = ProgressDialog.show(context, null, null,
                true, false);
        progressBar.setContentView(R.layout.progress_layout);
        progressBar.setCancelable(false);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (!progressBar.isShowing())
            progressBar.show();
    }


    @Override
    protected void onCancelled() {
        super.onCancelled();
        if (progressBar != null && progressBar.isShowing())
            progressBar.hide();
    }

    @Override
    protected Void doInBackground(Void... voids) {
        if (ImportActivity.BIP32.equals(bipToUse))
            fetchUtxosBip32(phrase, 0);
        else
            fetchUtxosBip44(phrase, 0);
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if (progressBar.isShowing())
            progressBar.hide();
        if (mListUtxos == null || mListUtxos.isEmpty() || mKeys == null || mKeys.isEmpty()) {
            if (listener != null)
                listener.error(R.string.Import_Error_empty);
            return;
        }
        final BRCoreTransaction transaction = getBrCoreTransaction();
        if (transaction == null) {
            listener.error(R.string.Import_Error_empty);
            return;
        }

        BigDecimal bigAmount = new BigDecimal(rvnWalletManager.getWallet().getTransactionAmount(transaction));
        BigDecimal bigFee = new BigDecimal(0);

        for (BRCoreTransactionInput in : transaction.getInputs())
            bigFee = bigFee.add(new BigDecimal(in.getAmount()));
        for (BRCoreTransactionOutput out : transaction.getOutputs())
            bigFee = bigFee.subtract(new BigDecimal(out.getAmount()));

        String amount = CurrencyUtils.getFormattedAmount(context, rvnWalletManager.getIso(context), bigAmount);
        String fee = CurrencyUtils.getFormattedAmount(context, rvnWalletManager.getIso(context), bigFee.abs());
        String message = String.format(context.getString(R.string.Import_confirm), amount, fee);
        BRDialog.showCustomDialog(context, "", message, "Import", context.getString(R.string.Button_cancel), new BRDialogView.BROnClickListener() {
            @Override
            public void onClick(BRDialogView brDialogView) {
                BRExecutor.getInstance().forLightWeightBackgroundTasks().execute(new Runnable() {
                    @Override
                    public void run() {
                        transaction.sign(mKeys.toArray(new BRCoreKey[mKeys.size()]), rvnWalletManager.getForkId());

                        if (!transaction.isSigned()) {
                            if (listener != null)
                                listener.error("error signing transaction");
                            String err = "transaction is not signed";
                            BRReportsManager.reportBug(new IllegalArgumentException(err));
                            return;
                        }
                        rvnWalletManager.getPeerManager().publishTransaction(transaction);
                    }
                });

                brDialogView.dismissWithAnimation();

            }
        }, new BRDialogView.BROnClickListener() {
            @Override
            public void onClick(BRDialogView brDialogView) {
                brDialogView.dismissWithAnimation();
            }
        }, null, 0);

    }

    @Nullable
    private BRCoreTransaction getBrCoreTransaction() {
        BRCoreTransaction transaction = new BRCoreTransaction();
        long totalAmount = 0;

        try {
            for (int i = 0; i < mListUtxos.size(); i++) {
                ApiUTxo utxo = mListUtxos.get(i);
                byte[] txid = TypesConverter.hexToBytesReverse(utxo.getTxid());
                int vout = utxo.getVout();
                byte[] scriptPubKey = TypesConverter.hexToBytes(utxo.getScriptPubKey());
                long amount = utxo.getSatoshis();
                totalAmount += amount;
                BRCoreTransactionInput in = new BRCoreTransactionInput(txid, vout, amount, scriptPubKey, new byte[]{}, -1);
                transaction.addInput(in);
            }

            if (totalAmount <= 0) return null;
//
            BRCoreAddress address = rvnWalletManager.getWallet().getReceiveAddress();
            long fee = rvnWalletManager.getWallet().getFeeForTransactionAmount(totalAmount);
            transaction.addOutput(new BRCoreTransactionOutput(totalAmount - fee, address.getPubKeyScript()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return transaction;
    }

    private void fetchUtxosBip44(byte[] phrase, int index) {
        if (countFails > 20) {
            countFails = 0;
            return;
        }

        BRCoreKey privKey = rvnWalletManager.getWallet().generatePrivateKeyBip44(phrase, index, chain);
        if (privKey != null) {
            String address = privKey.address();
            boolean isUsed = BRApiManager.isAddressUsed(context, address);
            if (isUsed) {
                this.countFails = 0;
                List<ApiUTxo> listUtxo = BRApiManager.fetchUTXOS(context, address, false);
                if (listUtxo != null) {
                    mKeys.add(privKey);
                    mListUtxos.addAll(listUtxo);
                }
                chain = chain == SEQUENCE_EXTERNAL_CHAIN ? SEQUENCE_INTERNAL_CHAIN : SEQUENCE_EXTERNAL_CHAIN;
                int nextIndex = (chain == SEQUENCE_EXTERNAL_CHAIN) ? (index + 1) : index;
                fetchUtxosBip44(phrase, nextIndex);
            } else {
                countFails = countFails + 1;
                chain = chain == SEQUENCE_EXTERNAL_CHAIN ? SEQUENCE_INTERNAL_CHAIN : SEQUENCE_EXTERNAL_CHAIN;
                int nextIndex = (chain == SEQUENCE_EXTERNAL_CHAIN) ? (index + 1) : index;
                fetchUtxosBip44(phrase, nextIndex);
            }
        }
    }

    private void fetchUtxosBip32(byte[] phrase, int index) {
        if (countFails > 20) {
            countFails = 0;
            return;
        }

        BRCoreKey privKey = rvnWalletManager.getWallet().generatePrivateKeyBip32(phrase, index, chain);
        if (privKey != null) {
            String address = privKey.address();
            boolean isUsed = BRApiManager.isAddressUsed(context, address);
            if (isUsed) {
                this.countFails = 0;
                List<ApiUTxo> listUtxo = BRApiManager.fetchUTXOS(context, address, false);
                if (listUtxo != null) {
                    mKeys.add(privKey);
                    mListUtxos.addAll(listUtxo);
                }
                chain = chain == SEQUENCE_EXTERNAL_CHAIN ? SEQUENCE_INTERNAL_CHAIN : SEQUENCE_EXTERNAL_CHAIN;
                int nextIndex = (chain == SEQUENCE_EXTERNAL_CHAIN) ? (index + 1) : index;
                fetchUtxosBip32(phrase, nextIndex);
            } else {
                countFails = countFails + 1;
                chain = chain == SEQUENCE_EXTERNAL_CHAIN ? SEQUENCE_INTERNAL_CHAIN : SEQUENCE_EXTERNAL_CHAIN;
                int nextIndex = (chain == SEQUENCE_EXTERNAL_CHAIN) ? (index + 1) : index;
                fetchUtxosBip32(phrase, nextIndex);
            }
        }
    }
}
