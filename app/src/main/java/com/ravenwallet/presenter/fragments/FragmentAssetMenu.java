package com.ravenwallet.presenter.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.platform.assets.Asset;
import com.ravenwallet.R;
import com.ravenwallet.core.BRCoreTransactionAsset;
import com.ravenwallet.core.BRCoreWallet;
import com.ravenwallet.presenter.customviews.BRButton;
import com.ravenwallet.presenter.customviews.BRDialogView;
import com.ravenwallet.presenter.interfaces.WalletManagerListener;
import com.ravenwallet.tools.animation.BRAnimator;
import com.ravenwallet.tools.animation.BRDialog;
import com.ravenwallet.tools.manager.BRSharedPrefs;
import com.ravenwallet.tools.util.Utils;
import com.ravenwallet.wallet.WalletsMaster;
import com.ravenwallet.wallet.abstracts.BaseWalletManager;
import com.ravenwallet.wallet.RvnWalletManager;

import java.math.BigDecimal;

import static com.platform.assets.AssetType.BURN;
import static com.ravenwallet.tools.animation.BRAnimator.animateBackgroundDim;
import static com.ravenwallet.tools.animation.BRAnimator.animateSignalSlide;

public class FragmentAssetMenu extends Fragment implements BurnFragmentListener, WalletManagerListener {

    private ScrollView backgroundLayout;
    private LinearLayout signalLayout;
    private BRButton reissueAssetButton;
    private BRButton transferAssetButton;
    private BRButton browseIPFSButton;
    private BRButton issueSubAssetButton;
    private BRButton issueUniqueAssetButton;
    private BRButton burnAssetButton;
    private BRButton getDataButton;
    private Asset mAsset;
    private final static String EXTRAS_ASSET_KEY = "extras.asset.key";

    public static FragmentAssetMenu newInstance(Asset asset) {
        FragmentAssetMenu fragment = new FragmentAssetMenu();
        Bundle args = new Bundle();
        args.putParcelable(EXTRAS_ASSET_KEY, asset);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_asset_menu, container, false);

        backgroundLayout = rootView.findViewById(R.id.background_layout);
        signalLayout = rootView.findViewById(R.id.signal_layout);
        reissueAssetButton = rootView.findViewById(R.id.reissue_asset_button);
        transferAssetButton = rootView.findViewById(R.id.transfer_asset_button);
        browseIPFSButton = rootView.findViewById(R.id.browse_ipfs_button);
        issueSubAssetButton = rootView.findViewById(R.id.issue_sub_asset_button);
        issueUniqueAssetButton = rootView.findViewById(R.id.issue_unique_asset_button);
        burnAssetButton = rootView.findViewById(R.id.burn_asset_button);
        getDataButton = rootView.findViewById(R.id.get_data_button);

        setListeners(rootView);

        // Set title
        mAsset = getArguments().getParcelable(EXTRAS_ASSET_KEY);
        TextView title = rootView.findViewById(R.id.title);
        title.setText(mAsset.getName());

        // Set Buttons enabling
        setMenuButtonsEnabling();

        return rootView;
    }

    private void setMenuButtonsEnabling() {
        // Manage owned asset button
        if (mAsset.getOwnership() == 1 && mAsset.getReissuable() == 1) {
            reissueAssetButton.setEnabled(true);
            reissueAssetButton.setType(4);
        }

        // Transfer asset button
        if (mAsset.getAmount() > 0) {
            transferAssetButton.setEnabled(true);
            transferAssetButton.setType(4);
        }

        // Browse IPFS button
        if (mAsset.getHasIpfs() == 1 && !TextUtils.isEmpty(mAsset.getIpfsHash())) {
            browseIPFSButton.setEnabled(true);
            browseIPFSButton.setType(4);
        }

        if (mAsset.getOwnership() == 1) {
            issueSubAssetButton.setEnabled(true);
            issueUniqueAssetButton.setEnabled(true);
            issueSubAssetButton.setType(4);
            issueUniqueAssetButton.setType(4);
        }
        if (mAsset.getAmount() > 0) {
            burnAssetButton.setEnabled(true);
            burnAssetButton.setType(6);
        } else burnAssetButton.setEnabled(false);

        boolean isExpertMode = BRSharedPrefs.getExpertMode(getContext());
        if (isExpertMode) {
            getDataButton.setEnabled(true);
            getDataButton.setType(4);
        } else {
            getDataButton.setVisibility(View.GONE);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (isRemoving()) {
            animateBackgroundDim(backgroundLayout, true);
            animateSignalSlide(signalLayout, true, new BRAnimator.OnSlideAnimationEnd() {
                @Override
                public void onAnimationEnd() {
                    closeMe();
                }
            });
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final ViewTreeObserver observer = signalLayout.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (observer.isAlive())
                    observer.removeOnGlobalLayoutListener(this);
                BRAnimator.animateBackgroundDim(backgroundLayout, false);
                BRAnimator.animateSignalSlide(signalLayout, false, new BRAnimator.OnSlideAnimationEnd() {
                    @Override
                    public void onAnimationEnd() {
                    }
                });
            }
        });
    }

    private void closeMe() {
        Activity app = getActivity();
        if (app != null && !app.isDestroyed())
            app.getFragmentManager().popBackStack();
    }

    private void setListeners(final View rootView) {
        // Handling the close button click
        ImageButton close = rootView.findViewById(R.id.close_button);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Activity app = getActivity();
                if (app != null)
                    app.getFragmentManager().popBackStack();
            }
        });

        // Handling the manage owned asset click
        reissueAssetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeMe();
                BRAnimator.showAssetManagingFragment(getActivity(), mAsset);
            }
        });

        // Handling the transfer asset click
        transferAssetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeMe();
                BRAnimator.showAssetTransferFragment(getActivity(), mAsset);
            }
        });

        // Handling the browse IPFS click
        browseIPFSButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String IPFSHash = mAsset.getIpfsHash();
                if (!TextUtils.isEmpty(IPFSHash)) {
                    closeMe();
                    String URL = Utils.getIpfsUrlFromHash(getContext(), IPFSHash);
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(URL)));
                    //BRAnimator.showIPFSFragment(getActivity(), IPFSHash);
                }
            }
        });

        issueSubAssetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeMe();
                BRAnimator.showIssueSubAssetFragment(getActivity(), mAsset);
            }
        });
        issueUniqueAssetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeMe();
                BRAnimator.showIssueUniqueAssetFragment(getActivity(), mAsset);
            }
        });

        burnAssetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Activity app = getActivity();
                BaseWalletManager wallet = WalletsMaster.getInstance(app).getCurrentWallet(app);
                long curBalance = wallet.getCachedBalance(app);

                //is the chosen ISO a crypto (could be also a fiat currency)
                boolean isIsoCrypto = WalletsMaster.getInstance(app).isIsoCrypto(app, wallet.getIso(app));

                BigDecimal isoBalance = isIsoCrypto ? wallet.getCryptoForSmallestCrypto(app, new BigDecimal(curBalance)) : wallet.getFiatForSmallestCrypto(app, new BigDecimal(curBalance), null);
                if (isoBalance == null) isoBalance = new BigDecimal(0);

                BigDecimal isoFee = isIsoCrypto ? wallet.getCryptoForSmallestCrypto(app, new BigDecimal(2300)) : wallet.getFiatForSmallestCrypto(app, new BigDecimal(2300), null);

                boolean hasBurnFee = isoBalance.compareTo(isoFee) > 0;
                if (hasBurnFee) {
                    BRAnimator.showBurnAssetFragment(getActivity(), mAsset, FragmentAssetMenu.this);
                } else {
                    BRDialog.showCustomDialog(getActivity(), "", "insufficient fund",
                            getString(R.string.AccessibilityLabels_close), null, new BRDialogView.BROnClickListener() {
                        @Override
                        public void onClick(BRDialogView brDialogView) {
                            brDialogView.dismiss();
                        }
                    }, null, null, 0);
                }
            }
        });
        getDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDataButton.setText("Loading data...");
                getDataButton.setEnabled(false);
                RvnWalletManager walletManager = RvnWalletManager.getInstance(getActivity());
                BRCoreWallet wallet = walletManager.getWallet();
                String name = mAsset.getName();
                wallet.getAssetData(walletManager.getPeerManager(), mAsset.getName(), name.length(), FragmentAssetMenu.this);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        getDataButton.setEnabled(true);
                        getDataButton.setText("Get data");
                    }
                }, 5000);
            }
        });

    }

    // callback of wallet.getAssetData from JNI
    public void onGetAssetData(BRCoreTransactionAsset asset) {
        Activity activity = getActivity();
        if (asset != null && activity != null) {
            BRAnimator.showDataFragment(activity, new Asset(asset));
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (!getDataButton.isEnabled()) {
                        getDataButton.setEnabled(true);
                        getDataButton.setText("Get data");
                    }
                }
            });
        }

    }

    @Override
    public void performBurn() {
        final Activity app = getActivity();
        if (app == null) return;
        final RvnWalletManager walletManager = RvnWalletManager.getInstance(app);
        walletManager.requestConfirmation(app, BURN,mAsset.getCoreAsset(),null,null,false,FragmentAssetMenu.this);
    }

    @Override
    public void close() {
        closeMe();
    }

    @Override
    public void error(String error) {
        BRDialog.showCustomDialog(getActivity(), "", error,
                getString(R.string.AccessibilityLabels_close), null, new BRDialogView.BROnClickListener() {
                    @Override
                    public void onClick(BRDialogView brDialogView) {
                        brDialogView.dismiss();
                    }
                }, null, null, 0);
    }
}