package com.ravencoin.presenter.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.security.keystore.UserNotAuthenticatedException;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.platform.assets.Asset;
import com.ravencoin.R;
import com.ravencoin.core.BRCoreTransaction;
import com.ravencoin.core.BRCoreTransactionAsset;
import com.ravencoin.core.BRCoreWallet;
import com.ravencoin.presenter.customviews.BRDialogView;
import com.ravencoin.presenter.customviews.BRText;
import com.ravencoin.presenter.interfaces.BRAuthCompletion;
import com.ravencoin.tools.animation.BRDialog;
import com.ravencoin.tools.manager.BRClipboardManager;
import com.ravencoin.tools.security.BRKeyStore;
import com.ravencoin.tools.util.BRConstants;
import com.ravencoin.tools.util.Utils;
import com.ravencoin.wallet.wallets.raven.RvnWalletManager;

import static com.ravencoin.tools.util.BRConstants.SATOSHIS;


public class FragmentBurnAsset extends DialogFragment {

    private static final String TAG = FragmentBurnAsset.class.getSimpleName();
    private Asset mAsset;

    private Button mOkButton, mCancelButton;
    private BurnFragmentListener listener;

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

        View rootView = inflater.inflate(R.layout.fragment_burn_asset, container, false);
        mOkButton = rootView.findViewById(R.id.ok_button);
        mCancelButton = rootView.findViewById(R.id.cancel_button);

        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        mOkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.performBurn();
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
        mAsset = b.getParcelable("asset");
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void setListner(BurnFragmentListener listener) {
        this.listener = listener;
    }
}
