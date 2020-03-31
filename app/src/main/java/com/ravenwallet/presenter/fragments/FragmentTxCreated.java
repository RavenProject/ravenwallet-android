package com.ravenwallet.presenter.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.ravenwallet.BuildConfig;
import com.ravenwallet.R;
import com.ravenwallet.presenter.customviews.BRLinearLayoutWithCaret;
import com.ravenwallet.presenter.customviews.BRText;
import com.ravenwallet.tools.manager.BRClipboardManager;
import com.ravenwallet.tools.qrcode.QRUtils;
import com.ravenwallet.tools.threads.executor.BRExecutor;
import com.ravenwallet.tools.util.Utils;
import com.ravenwallet.wallet.WalletsMaster;
import com.ravenwallet.wallet.abstracts.BaseWalletManager;
import com.ravenwallet.wallet.util.CryptoUriParser;


public class FragmentTxCreated extends DialogFragment {

    private static final String TAG = FragmentTxCreated.class.getSimpleName();

    private BRText txHash, lblTxHash;
    private Button mOkButton, mCopyButton;
    private ImageView qrImage;
    private String mTxHash;
    public RelativeLayout signalLayout;
    private BRLinearLayoutWithCaret copiedLayout;
//    private Handler copyCloseHandler = new Handler();

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

        View rootView = inflater.inflate(R.layout.fragment_tx_created, container, false);

        qrImage = rootView.findViewById(R.id.qr_image);
        txHash = rootView.findViewById(R.id.tx_hash);
        lblTxHash = rootView.findViewById(R.id.lbl_tx_hash);
        mOkButton = rootView.findViewById(R.id.ok_button);
        mCopyButton = rootView.findViewById(R.id.copy_button);
        signalLayout = rootView.findViewById(R.id.signal_layout);
        copiedLayout = rootView.findViewById(R.id.copied_layout);

        mOkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        mCopyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                copyText();
            }
        });

        updateUi();
        return rootView;
    }

    private void copyText() {
        if (TextUtils.isEmpty(mTxHash)) return;
        Activity app = getActivity();
        BRClipboardManager.putClipboard(app, mTxHash);
        //copy the legacy for testing purposes (testnet faucet money receiving)
        if (Utils.isEmulatorOrDebug(app) && BuildConfig.TESTNET)
            BRClipboardManager.putClipboard(app, WalletsMaster.getInstance(app).getCurrentWallet(app).undecorateAddress(app, mTxHash));
        showCopiedLayout(true);
    }

    private void showCopiedLayout(boolean b) {
        if (!b) {
            copiedLayout.setVisibility(View.GONE);
        } else {
            if (copiedLayout.getVisibility() == View.GONE) {
                copiedLayout.setVisibility(View.VISIBLE);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        copiedLayout.setVisibility(View.GONE);
                    }
                }, 2000);
            } else {
                copiedLayout.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    private void updateUi() {
        final Context ctx = getContext();
        Bundle b = getArguments();
        if (b == null || TextUtils.isEmpty(b.getString("tx_hash")))
            return;
        mTxHash = b.getString("tx_hash");
        txHash.setText(mTxHash);
        WalletsMaster.getInstance(ctx).getCurrentWallet(ctx).refreshAddress(ctx);
        final BaseWalletManager wallet = WalletsMaster.getInstance(ctx).getCurrentWallet(ctx);
        BRExecutor.getInstance().forMainThreadTasks().execute(new Runnable() {
            @Override
            public void run() {
                Uri uri = CryptoUriParser.createCryptoUrl(ctx, wallet, mTxHash, 0, null, null, null);
                QRUtils.generateQR(ctx, uri.toString(), qrImage);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}
