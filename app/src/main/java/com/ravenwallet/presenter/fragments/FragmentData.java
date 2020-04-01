package com.ravenwallet.presenter.fragments;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.platform.assets.Asset;
import com.platform.assets.Utils;
import com.ravenwallet.R;
import com.ravenwallet.presenter.customviews.BRText;

import static com.ravenwallet.tools.util.BRConstants.SATOSHIS;


public class FragmentData extends DialogFragment {

    private static final String TAG = "FragmentData";


    private BRText mTxName;
    private BRText mTxAmount;
    private BRText mTxUnits;
    private BRText mTxreissuable;
    private BRText mTxHasIpfs;
    private BRText mTxIpfsHash;

    private Button mOkButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_TITLE, 0);

    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return super.onCreateDialog(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_data, container, false);

        mTxName = rootView.findViewById(R.id.tx_name);
        mTxAmount = rootView.findViewById(R.id.tx_amount);
        mTxUnits = rootView.findViewById(R.id.tx_units);
        mTxreissuable = rootView.findViewById(R.id.tx_reissuable);
        mTxHasIpfs = rootView.findViewById(R.id.tx_hasIpfs);
        mTxIpfsHash = rootView.findViewById(R.id.tx_ipfsHash);
        mOkButton = rootView.findViewById(R.id.ok_button);

        mOkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
        Bundle b = getArguments();
        if (b == null || b.getParcelable("asset") == null)
            return;
        Asset asset = b.getParcelable("asset");

        if (asset != null) {
            mTxName.setText(asset.getName());

            mTxAmount.setText(String.valueOf(Utils.formatAssetAmount(asset.getAmount()/SATOSHIS, asset.getUnits())));
            mTxUnits.setText(String.valueOf(asset.getUnits()));
            mTxreissuable.setText(String.valueOf(asset.getReissuable()));
            mTxHasIpfs.setText(String.valueOf(asset.getHasIpfs()));
            mTxIpfsHash.setText(!TextUtils.isEmpty(asset.getIpfsHash()) ? asset.getIpfsHash() : "");
        } else {
            Toast.makeText(getContext(), "Error getting transaction data", Toast.LENGTH_SHORT).show();
        }


    }

    @Override
    public void onResume() {
        super.onResume();
    }
}
