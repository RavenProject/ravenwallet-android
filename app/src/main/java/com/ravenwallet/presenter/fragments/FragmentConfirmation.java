package com.ravenwallet.presenter.fragments;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.platform.assets.Asset;
import com.platform.assets.AssetType;
import com.ravenwallet.R;
import com.ravenwallet.presenter.customviews.BRButton;
import com.ravenwallet.presenter.customviews.BRText;
import com.ravenwallet.presenter.interfaces.ConfirmationListener;
import com.ravenwallet.tools.manager.BRSharedPrefs;
import com.ravenwallet.tools.util.CurrencyUtils;
import com.ravenwallet.wallet.WalletsMaster;
import com.ravenwallet.wallet.abstracts.BaseWalletManager;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;

import static com.ravenwallet.tools.util.BRConstants.CREATION_FEE;
import static com.ravenwallet.tools.util.BRConstants.REISSUE_FEE;
import static com.ravenwallet.tools.util.BRConstants.SATOSHIS;
import static com.ravenwallet.tools.util.BRConstants.SUB_FEE;
import static com.ravenwallet.tools.util.BRConstants.UNIQUE_FEE;


public class FragmentConfirmation extends DialogFragment {

    private static final String TAG = "FragmentData";
    private ConfirmationListener confirmationListener;

    private BRText mTvNamAmount, tvType;
    private BRText mTvAmount, tvAddress;
    private BRText lblAmount, tvNetFee, lblTxTypeFee, tvTxTypeFee, tvTotal;
    private BRButton mSendButton, mCancelButton;
    private LinearLayout layout_tx_type_fee, layoutAddress, layout_amount;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_TITLE, 0);
    }

    public void setConfirmationListener(ConfirmationListener confirmationListener) {
        this.confirmationListener = confirmationListener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return super.onCreateDialog(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_confirmation, container, false);

        tvType = rootView.findViewById(R.id.tv_type);
        mTvNamAmount = rootView.findViewById(R.id.tv_asset_name_amount);
        tvAddress = rootView.findViewById(R.id.tv_address);
        lblAmount = rootView.findViewById(R.id.lbl_amount);
        mTvAmount = rootView.findViewById(R.id.tv_amount);
        tvNetFee = rootView.findViewById(R.id.tv_net_fee);
        lblTxTypeFee = rootView.findViewById(R.id.lbl_tx_type_fee);
        tvTxTypeFee = rootView.findViewById(R.id.tv_tx_type_fee);
        tvTotal = rootView.findViewById(R.id.tv_total);
        mCancelButton = rootView.findViewById(R.id.cancel_button);
        mSendButton = rootView.findViewById(R.id.send_button);
        layout_tx_type_fee = rootView.findViewById(R.id.layout_tx_type_fee);
        layout_amount = rootView.findViewById(R.id.layout_amount);
        layoutAddress = rootView.findViewById(R.id.layout_address);
        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirmationListener.onCancel();
                dismiss();
            }
        });

        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirmationListener.onConfirm();
                dismiss();
            }
        });

        updateUi();
        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    private void updateUi() {
        Context context = getContext();
        Bundle b = getArguments();
        if (b == null || b.getParcelable("asset") == null)
            return;
        Asset asset = b.getParcelable("asset");
        String address = b.getString("address");
        AssetType assetType = (AssetType) b.getSerializable("type");
        String type = "Create";
        String lblTxFee = "Creation Fee";
        long assetTypeFee = CREATION_FEE;
        switch (assetType) {
            case NEW_ASSET:
                type = "Create";
                lblTxFee = "Creation Fee";
                assetTypeFee = CREATION_FEE;
                break;
            case REISSUE:
                type = "Manage";
                lblTxFee = "Managing Fee";
                assetTypeFee = REISSUE_FEE;
                break;
            case TRANSFER:
                type = "Transfer";
                lblTxFee = "Transfer Fee";
                layout_tx_type_fee.setVisibility(View.GONE);
                mSendButton.setText("Send");
                assetTypeFee = 0;
                break;
            case SUB:
                type = "Create";
                lblTxFee = "Sub Asset Fee";
                assetTypeFee = SUB_FEE;
                break;
            case UNIQUE:
                type = "Create";
                lblTxFee = "Unique Asset Fee";
                assetTypeFee = UNIQUE_FEE;
                break;
            case BURN:
                type = "Burn";
                lblTxFee = "Burn Fee";
                layout_tx_type_fee.setVisibility(View.GONE);
                layout_amount.setVisibility(View.GONE);
                mSendButton.setText("Burn");
                mSendButton.setType(6);
                assetTypeFee = 0;
                break;
        }
        BaseWalletManager wallet = WalletsMaster.getInstance(context).getCurrentWallet(context);
        boolean isIsoCrypto = WalletsMaster.getInstance(context).isIsoCrypto(getActivity(), wallet.getIso(context));

        //get the fee for iso (dollars, bits, BTC..)
        BigDecimal isoFee = isIsoCrypto ? wallet.getCryptoForSmallestCrypto(context,
                new BigDecimal(2300)) :
                wallet.getFiatForSmallestCrypto(context, new BigDecimal(2300), null);

        //format the fee to the selected ISO
        String formattedFee = CurrencyUtils.getFormattedAmount(context, wallet.getIso(context),
                isIsoCrypto ? wallet.getSmallestCryptoForCrypto(context, isoFee) : isoFee);
        //format the asset creation fee to the selected ISO
        BigDecimal isoAssetCreationFee = new BigDecimal(assetTypeFee);
        String formattedAssetFee = CurrencyUtils.getFormattedAmount(context, wallet.getIso(context),
                wallet.getSmallestCryptoForCrypto(context, isoAssetCreationFee));
        BigDecimal total = isoAssetCreationFee.add(isoFee);
        String formattedTotal = CurrencyUtils.getFormattedAmount(context, wallet.getIso(context),
                wallet.getSmallestCryptoForCrypto(context, total));

        tvTotal.setText(formattedTotal);
        tvTxTypeFee.setText(formattedAssetFee);
        tvNetFee.setText(formattedFee);

        if (TextUtils.isEmpty(address) || !BRSharedPrefs.getExpertMode(getContext())|| !BRSharedPrefs.getShowAddressInput(getContext()))
            layoutAddress.setVisibility(View.GONE);
        else tvAddress.setText(address);
        DecimalFormat currencyFormat = (DecimalFormat) DecimalFormat.getInstance();
        currencyFormat.setGroupingUsed(true);
        currencyFormat.setRoundingMode(RoundingMode.DOWN);
        BigDecimal amount = new BigDecimal(asset.getAmount() / SATOSHIS);
        Log.e(TAG, "setTexts: amount:" + amount);
        String formattedAmount = currencyFormat.format(amount.doubleValue());

        mTvNamAmount.setText(formattedAmount + " " + asset.getName());
        mTvAmount.setText(formattedAmount + " " + asset.getName());

        tvType.setText(type);
        lblTxTypeFee.setText(lblTxFee);
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}
