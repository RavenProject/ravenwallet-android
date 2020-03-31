package com.ravenwallet.tools.animation;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

import com.platform.addressBook.AddressBookItem;
import com.platform.assets.Asset;
import com.platform.assets.AssetType;
import com.ravenwallet.R;
import com.ravenwallet.presenter.activities.HomeActivity;
import com.ravenwallet.presenter.activities.LoginActivity;
import com.ravenwallet.presenter.activities.camera.ScanQRActivity;
import com.ravenwallet.presenter.activities.intro.IntroActivity;
import com.ravenwallet.presenter.customviews.BRDialogView;
import com.ravenwallet.presenter.entities.CryptoRequest;
import com.ravenwallet.presenter.entities.TxUiHolder;
import com.ravenwallet.presenter.fragments.BurnFragmentListener;
import com.ravenwallet.presenter.fragments.FragmentAddAddress;
import com.ravenwallet.presenter.fragments.FragmentAssetMenu;
import com.ravenwallet.presenter.fragments.FragmentBurnAsset;
import com.ravenwallet.presenter.fragments.FragmentConfirmation;
import com.ravenwallet.presenter.fragments.FragmentCreateAsset;
import com.ravenwallet.presenter.fragments.FragmentData;
import com.ravenwallet.presenter.fragments.FragmentIPFS;
import com.ravenwallet.presenter.fragments.FragmentIssueSubAsset;
import com.ravenwallet.presenter.fragments.FragmentIssueUniqueAsset;
import com.ravenwallet.presenter.fragments.FragmentManageAsset;
import com.ravenwallet.presenter.fragments.FragmentMenu;
import com.ravenwallet.presenter.fragments.FragmentReceive;
import com.ravenwallet.presenter.fragments.FragmentRequestAmount;
import com.ravenwallet.presenter.fragments.FragmentSend;
import com.ravenwallet.presenter.fragments.FragmentSignal;
import com.ravenwallet.presenter.fragments.FragmentSupport;
import com.ravenwallet.presenter.fragments.FragmentTransferAsset;
import com.ravenwallet.presenter.fragments.FragmentTxCreated;
import com.ravenwallet.presenter.fragments.FragmentTxDetails;
import com.ravenwallet.presenter.interfaces.BROnSignalCompletion;
import com.ravenwallet.presenter.interfaces.ConfirmationListener;
import com.ravenwallet.tools.threads.executor.BRExecutor;
import com.ravenwallet.tools.util.BRConstants;

import javax.annotation.Nullable;

public class BRAnimator {

    private static final String TAG = BRAnimator.class.getName();
    private static boolean clickAllowed = true;
    public static int SLIDE_ANIMATION_DURATION = 300;
    public static float t1Size;
    public static float t2Size;
    public static boolean supportIsShowing;

    public static void showRvnSignal(Activity activity, String title, String iconDescription, int drawableId, BROnSignalCompletion completion) {
        FragmentSignal fragmentSignal = new FragmentSignal();
        Bundle bundle = new Bundle();
        bundle.putString(FragmentSignal.TITLE, title);
        bundle.putString(FragmentSignal.ICON_DESCRIPTION, iconDescription);
        fragmentSignal.setCompletion(completion);
        bundle.putInt(FragmentSignal.RES_ID, drawableId);
        fragmentSignal.setArguments(bundle);
        FragmentTransaction transaction = activity.getFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.animator.from_bottom, R.animator.to_bottom, R.animator.from_bottom, R.animator.to_bottom);
        transaction.add(android.R.id.content, fragmentSignal, fragmentSignal.getClass().getName());
        transaction.addToBackStack(null);
        if (!activity.isDestroyed())
            transaction.commit();
    }

    public static void init(Activity app) {
        if (app == null) return;
//        t1Size = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 30, app.getResources().getDisplayMetrics());
//        t2Size = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 16, app.getResources().getDisplayMetrics());
        t1Size = 30;
        t2Size = 16;
    }

    public static void showFragmentByTag(Activity app, String tag) {
        Log.e(TAG, "showFragmentByTag: " + tag);
        if (tag == null) return;
        //catch animation duration, make it 0 for no animation, then restore it.
        final int slideAnimation = SLIDE_ANIMATION_DURATION;
        try {
            SLIDE_ANIMATION_DURATION = 0;
            if (tag.equalsIgnoreCase(FragmentSend.class.getName())) {
                showSendFragment(app, null, false, null);
            } else if (tag.equalsIgnoreCase(FragmentReceive.class.getName())) {
                showReceiveFragment(app, true,null);
            } else if (tag.equalsIgnoreCase(FragmentRequestAmount.class.getName())) {
                showRequestFragment(app);
            } else if (tag.equalsIgnoreCase(FragmentMenu.class.getName())) {
                showMenuFragment(app);
            } else {
                Log.e(TAG, "showFragmentByTag: error, no such tag: " + tag);
            }
        } finally {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    SLIDE_ANIMATION_DURATION = slideAnimation;
                }
            }, 800);

        }
    }

    public static void showSendFragment(Activity app, final CryptoRequest request, boolean isFromAddressBook, @Nullable AddressBookItem address) {
        if (app == null) {
            Log.e(TAG, "showSendFragment: app is null");
            return;
        }
        FragmentSend fragmentSend = (FragmentSend) app.getFragmentManager().findFragmentByTag(FragmentSend.class.getName());
        if (fragmentSend != null && fragmentSend.isAdded()) {
            fragmentSend.setCryptoObject(request);
            return;
        }
        try {
            fragmentSend = FragmentSend.newInstance(isFromAddressBook, address);
            if (request != null && !request.address.isEmpty()) {
                fragmentSend.setCryptoObject(request);
            }
            app.getFragmentManager().beginTransaction()
                    .setCustomAnimations(0, 0, 0, R.animator.plain_300)
                    .add(android.R.id.content, fragmentSend, FragmentSend.class.getName())
                    .addToBackStack(FragmentSend.class.getName()).commit();
        } finally {

        }

    }

    public static void showAddressAddingFragment(Activity app) {
        if (app == null) {
            Log.e(TAG, "showAddressAddingFragment: app is null");
            return;
        }
        FragmentAddAddress fragmentAddAddress = (FragmentAddAddress) app.getFragmentManager().findFragmentByTag(FragmentAddAddress.class.getName());
        if (fragmentAddAddress != null && fragmentAddAddress.isAdded()) return;

        fragmentAddAddress = FragmentAddAddress.newInstance();

        app.getFragmentManager().beginTransaction()
                .setCustomAnimations(0, 0, 0, R.animator.plain_300)
                .add(android.R.id.content, fragmentAddAddress, FragmentAddAddress.class.getName())
                .addToBackStack(FragmentAddAddress.class.getName()).commit();
    }

    public static void showAssetMenuFragment(Activity app, Asset asset) {
        if (app == null) {
            Log.e(TAG, "showAddressAddingFragment: app is null");
            return;
        }
        FragmentAssetMenu fragmentAssetMenu = (FragmentAssetMenu) app.getFragmentManager().findFragmentByTag(FragmentAssetMenu.class.getName());
        if (fragmentAssetMenu != null && fragmentAssetMenu.isAdded()) return;

        fragmentAssetMenu = FragmentAssetMenu.newInstance(asset);

        app.getFragmentManager().beginTransaction()
                .setCustomAnimations(0, 0, 0, R.animator.plain_300)
                .add(android.R.id.content, fragmentAssetMenu, FragmentAssetMenu.class.getName())
                .addToBackStack(FragmentAssetMenu.class.getName()).commit();
    }

    public static void showAssetTransferFragment(Activity app, Asset asset) {
        if (app == null) {
            Log.e(TAG, "showAssetTransferFragment: app is null");
            return;
        }
        FragmentTransferAsset fragmentTransferAsset = (FragmentTransferAsset) app.getFragmentManager().findFragmentByTag(FragmentTransferAsset.class.getName());
        if (fragmentTransferAsset != null && fragmentTransferAsset.isAdded()) return;

        fragmentTransferAsset = FragmentTransferAsset.newInstance(asset);

        app.getFragmentManager().beginTransaction()
                .setCustomAnimations(0, 0, 0, R.animator.plain_300)
                .add(android.R.id.content, fragmentTransferAsset, FragmentTransferAsset.class.getName())
                .addToBackStack(FragmentTransferAsset.class.getName()).commit();
    }

    public static void showAssetCreationFragment(Activity app) {
        if (app == null) {
            Log.e(TAG, "showAssetCreationFragment: app is null");
            return;
        }
        FragmentCreateAsset fragmentCreateAsset = (FragmentCreateAsset) app.getFragmentManager().findFragmentByTag(FragmentCreateAsset.class.getName());
        if (fragmentCreateAsset != null && fragmentCreateAsset.isAdded()) return;

        fragmentCreateAsset = FragmentCreateAsset.newInstance();

        app.getFragmentManager().beginTransaction()
                .setCustomAnimations(0, 0, 0, R.animator.plain_300)
                .add(android.R.id.content, fragmentCreateAsset, FragmentCreateAsset.class.getName())
                .addToBackStack(FragmentCreateAsset.class.getName()).commit();
    }

    public static void showIssueSubAssetFragment(Activity app, Asset asset) {
        if (app == null) {
            Log.e(TAG, "showIssueSubAssetFragment: app is null");
            return;
        }
        FragmentIssueSubAsset fragmentIssueSubAsset = (FragmentIssueSubAsset) app.getFragmentManager().findFragmentByTag(FragmentIssueSubAsset.class.getName());
        if (fragmentIssueSubAsset != null && fragmentIssueSubAsset.isAdded()) return;

        fragmentIssueSubAsset = FragmentIssueSubAsset.newInstance(asset);

        app.getFragmentManager().beginTransaction()
                .setCustomAnimations(0, 0, 0, R.animator.plain_300)
                .add(android.R.id.content, fragmentIssueSubAsset, FragmentIssueSubAsset.class.getName())
                .addToBackStack(FragmentIssueSubAsset.class.getName()).commit();
    }

    public static void showIssueUniqueAssetFragment(Activity app, Asset asset) {
        if (app == null) {
            Log.e(TAG, "showIssueUniqueAssetFragment: app is null");
            return;
        }
        FragmentIssueUniqueAsset fragmentIssueUniqueAsset = (FragmentIssueUniqueAsset) app.getFragmentManager().findFragmentByTag(FragmentIssueUniqueAsset.class.getName());
        if (fragmentIssueUniqueAsset != null && fragmentIssueUniqueAsset.isAdded()) return;

        fragmentIssueUniqueAsset = FragmentIssueUniqueAsset.newInstance(asset);

        app.getFragmentManager().beginTransaction()
                .setCustomAnimations(0, 0, 0, R.animator.plain_300)
                .add(android.R.id.content, fragmentIssueUniqueAsset, FragmentIssueUniqueAsset.class.getName())
                .addToBackStack(FragmentIssueUniqueAsset.class.getName()).commit();
    }

    public static void showAssetManagingFragment(Activity app, Asset asset) {
        if (app == null) {
            Log.e(TAG, "showAssetManagingFragment: app is null");
            return;
        }
        FragmentManageAsset fragmentManageAsset = (FragmentManageAsset) app.getFragmentManager().findFragmentByTag(FragmentManageAsset.class.getName());
        if (fragmentManageAsset != null && fragmentManageAsset.isAdded()) return;

        fragmentManageAsset = FragmentManageAsset.newInstance(asset);

        app.getFragmentManager().beginTransaction()
                .setCustomAnimations(0, 0, 0, R.animator.plain_300)
                .add(android.R.id.content, fragmentManageAsset, FragmentManageAsset.class.getName())
                .addToBackStack(FragmentManageAsset.class.getName()).commit();
    }

    public static void showSupportFragment(Activity app, String articleId) {
        if (supportIsShowing) return;
        supportIsShowing = true;
        if (app == null) {
            Log.e(TAG, "showSupportFragment: app is null");
            return;
        }
        FragmentSupport fragmentSupport = (FragmentSupport) app.getFragmentManager().findFragmentByTag(FragmentSupport.class.getName());
        if (fragmentSupport != null && fragmentSupport.isAdded()) {
            app.getFragmentManager().popBackStack();
            return;
        }
        try {
            fragmentSupport = new FragmentSupport();
            if (articleId != null && !articleId.isEmpty()) {
                Bundle bundle = new Bundle();
                bundle.putString("articleId", articleId);
                fragmentSupport.setArguments(bundle);
            }
            app.getFragmentManager().beginTransaction()
                    .setCustomAnimations(0, 0, 0, R.animator.plain_300)
                    .add(android.R.id.content, fragmentSupport, FragmentSend.class.getName())
                    .addToBackStack(FragmentSend.class.getName()).commit();
        } finally {

        }

    }

    public static void showIPFSFragment(Activity app, String IPFSHash) {
        if (app == null) {
            Log.e(TAG, "showSupportFragment: app is null");
            return;
        }
        FragmentIPFS fragmentIPFS = (FragmentIPFS) app.getFragmentManager().findFragmentByTag(FragmentIPFS.class.getName());
        if (fragmentIPFS != null && fragmentIPFS.isAdded()) {
            app.getFragmentManager().popBackStack();
            return;
        }
        fragmentIPFS = FragmentIPFS.newInstance(IPFSHash);

        app.getFragmentManager().beginTransaction()
                .setCustomAnimations(0, 0, 0, R.animator.plain_300)
                .add(android.R.id.content, fragmentIPFS, FragmentIPFS.class.getName())
                .addToBackStack(FragmentSend.class.getName()).commit();
    }

    public static void popBackStackTillEntry(Activity app, int entryIndex) {

        if (app.getFragmentManager() == null) {
            return;
        }
        if (app.getFragmentManager().getBackStackEntryCount() <= entryIndex) {
            return;
        }
        FragmentManager.BackStackEntry entry = app.getFragmentManager().getBackStackEntryAt(
                entryIndex);
        if (entry != null) {
            app.getFragmentManager().popBackStackImmediate(entry.getId(),
                    FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }


    }

//    public static void showTransactionPager(Activity app, List<TxUiHolder> items, int position) {
//        if (app == null) {
//            Log.e(TAG, "showSendFragment: app is null");
//            return;
//        }
//        FragmentTransactionDetails fragmentTransactionDetails = (FragmentTransactionDetails) app.getFragmentManager().findFragmentByTag(FragmentTransactionDetails.class.getName());
//        if (fragmentTransactionDetails != null && fragmentTransactionDetails.isAdded()) {
//            fragmentTransactionDetails.setItems(items);
//            Log.e(TAG, "showTransactionPager: Already showing");
//            return;
//        }
//        fragmentTransactionDetails = new FragmentTransactionDetails();
//        fragmentTransactionDetails.setItems(items);
//        Bundle bundle = new Bundle();
//        bundle.putInt("pos", position);
//        fragmentTransactionDetails.setArguments(bundle);
//
//        app.getFragmentManager().beginTransaction()
//                .setCustomAnimations(0, 0, 0, R.animator.plain_300)
//                .add(android.R.id.content, fragmentTransactionDetails, FragmentTransactionDetails.class.getName())
//                .addToBackStack(FragmentTransactionDetails.class.getName()).commit();
//
//    }

    public static void showTransactionDetails(Activity app, TxUiHolder item, int position) {

        FragmentTxDetails txDetails = (FragmentTxDetails) app.getFragmentManager().findFragmentByTag(FragmentTxDetails.class.getName());

        if (txDetails != null && txDetails.isAdded()) {
            Log.e(TAG, "showTransactionDetails: Already showing");

            return;
        }

        txDetails = new FragmentTxDetails();
        txDetails.setTransaction(item);
        txDetails.show(app.getFragmentManager(), "txDetails");

    }

    public static void openAddressScanner(Activity app, int requestID) {
        try {
            if (app == null) return;

            // Check if the camera permission is granted
            if (ContextCompat.checkSelfPermission(app,
                    Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(app,
                        Manifest.permission.CAMERA)) {
                    BRDialog.showCustomDialog(app, app.getString(R.string.Send_cameraUnavailabeTitle_android), app.getString(R.string.Send_cameraUnavailabeMessage_android), app.getString(R.string.AccessibilityLabels_close), null, new BRDialogView.BROnClickListener() {
                        @Override
                        public void onClick(BRDialogView brDialogView) {
                            brDialogView.dismiss();
                        }
                    }, null, null, 0);
                } else {
                    // No explanation needed, we can request the permission.
                    ActivityCompat.requestPermissions(app,
                            new String[]{Manifest.permission.CAMERA},
                            BRConstants.CAMERA_REQUEST_ID);
                }
            } else {
                // Permission is granted, open camera
                Intent intent = new Intent(app, ScanQRActivity.class);
                app.startActivityForResult(intent, requestID);
                app.overridePendingTransition(R.anim.fade_up, R.anim.fade_down);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void openIPFSHashScanner(Activity app, int requestID) {
        try {
            if (app == null) return;

            // Check if the camera permission is granted
            if (ContextCompat.checkSelfPermission(app,
                    Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(app,
                        Manifest.permission.CAMERA)) {
                    BRDialog.showCustomDialog(app, app.getString(R.string.Send_cameraUnavailabeTitle_android), app.getString(R.string.Send_cameraUnavailabeMessage_android), app.getString(R.string.AccessibilityLabels_close), null, new BRDialogView.BROnClickListener() {
                        @Override
                        public void onClick(BRDialogView brDialogView) {
                            brDialogView.dismiss();
                        }
                    }, null, null, 0);
                } else {
                    // No explanation needed, we can request the permission.
                    ActivityCompat.requestPermissions(app,
                            new String[]{Manifest.permission.CAMERA},
                            BRConstants.CAMERA_REQUEST_ID);
                }
            } else {
                // Permission is granted, open camera
                Intent intent = new Intent(app, ScanQRActivity.class);
                intent.putExtra(ScanQRActivity.SCANNING_IPFS_HASH_EXTRA_KEY, true);
                app.startActivityForResult(intent, requestID);
                app.overridePendingTransition(R.anim.fade_up, R.anim.fade_down);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static LayoutTransition getDefaultTransition() {
        LayoutTransition itemLayoutTransition = new LayoutTransition();
        itemLayoutTransition.setStartDelay(LayoutTransition.APPEARING, 0);
        itemLayoutTransition.setStartDelay(LayoutTransition.DISAPPEARING, 0);
        itemLayoutTransition.setStartDelay(LayoutTransition.CHANGE_APPEARING, 0);
        itemLayoutTransition.setStartDelay(LayoutTransition.CHANGE_DISAPPEARING, 0);
        itemLayoutTransition.setStartDelay(LayoutTransition.CHANGING, 0);
        itemLayoutTransition.setDuration(100);
        itemLayoutTransition.setInterpolator(LayoutTransition.CHANGING, new OvershootInterpolator(2f));
        Animator scaleUp = ObjectAnimator.ofPropertyValuesHolder((Object) null, PropertyValuesHolder.ofFloat(View.SCALE_X, 1, 1), PropertyValuesHolder.ofFloat(View.SCALE_Y, 0, 1));
        scaleUp.setDuration(50);
        scaleUp.setStartDelay(50);
        Animator scaleDown = ObjectAnimator.ofPropertyValuesHolder((Object) null, PropertyValuesHolder.ofFloat(View.SCALE_X, 1, 1), PropertyValuesHolder.ofFloat(View.SCALE_Y, 1, 0));
        scaleDown.setDuration(2);
        itemLayoutTransition.setAnimator(LayoutTransition.APPEARING, scaleUp);
        itemLayoutTransition.setAnimator(LayoutTransition.DISAPPEARING, null);
        itemLayoutTransition.enableTransitionType(LayoutTransition.CHANGING);
        return itemLayoutTransition;
    }

    public static void showRequestFragment(Activity app) {
        if (app == null) {
            Log.e(TAG, "showRequestFragment: app is null");
            return;
        }

        FragmentRequestAmount fragmentRequestAmount = (FragmentRequestAmount) app.getFragmentManager().findFragmentByTag(FragmentRequestAmount.class.getName());
        if (fragmentRequestAmount != null && fragmentRequestAmount.isAdded())
            return;

        fragmentRequestAmount = new FragmentRequestAmount();
        Bundle bundle = new Bundle();
        fragmentRequestAmount.setArguments(bundle);
        app.getFragmentManager().beginTransaction()
                .setCustomAnimations(0, 0, 0, R.animator.plain_300)
                .add(android.R.id.content, fragmentRequestAmount, FragmentRequestAmount.class.getName())
                .addToBackStack(FragmentRequestAmount.class.getName()).commit();

    }

    //isReceive tells the Animator that the Receive fragment is requested, not My Address
    public static void showReceiveFragment(Activity app, boolean isReceive, String address) {
        if (app == null) {
            Log.e(TAG, "showReceiveFragment: app is null");
            return;
        }
        FragmentReceive fragmentReceive = (FragmentReceive) app.getFragmentManager().findFragmentByTag(FragmentReceive.class.getName());
        if (fragmentReceive != null && fragmentReceive.isAdded())
            return;
        fragmentReceive = new FragmentReceive();
        Bundle args = new Bundle();
        args.putBoolean("receive", isReceive);
        if (!TextUtils.isEmpty(address))
            args.putString("address", address);
        fragmentReceive.setArguments(args);

        app.getFragmentManager().beginTransaction()
                .setCustomAnimations(0, 0, 0, R.animator.plain_300)
                .add(android.R.id.content, fragmentReceive, FragmentReceive.class.getName())
                .addToBackStack(FragmentReceive.class.getName()).commit();

    }

    public static void showMenuFragment(Activity app) {
        if (app == null) {
            Log.e(TAG, "showReceiveFragment: app is null");
            return;
        }
        FragmentTransaction transaction = app.getFragmentManager().beginTransaction();
        transaction.setCustomAnimations(0, 0, 0, R.animator.plain_300);
        transaction.add(android.R.id.content, new FragmentMenu(), FragmentMenu.class.getName());
        transaction.addToBackStack(FragmentMenu.class.getName());
        transaction.commit();

    }

    public static boolean isClickAllowed() {
        if (clickAllowed) {
            clickAllowed = false;
            BRExecutor.getInstance().forLightWeightBackgroundTasks().execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    clickAllowed = true;
                }
            });
            return true;
        } else return false;
    }

    public static void killAllFragments(Activity app) {
        if (app != null && !app.isDestroyed())
            app.getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }

    public static void startRvnIfNotStarted(Activity app) {
        if (!(app instanceof HomeActivity))
            startRvnActivity(app, false);
    }

    public static void startRvnActivity(Activity from, boolean auth) {
        if (from == null) return;
        Log.e(TAG, "startRvnActivity: " + from.getClass().getName());
        Class toStart = auth ? LoginActivity.class : HomeActivity.class;
        Intent intent = new Intent(from, toStart);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        from.startActivity(intent);
        from.overridePendingTransition(R.anim.fade_up, R.anim.fade_down);
        if (!from.isDestroyed()) {
            from.finish();
        }
    }

    public static void animateSignalSlide(final ViewGroup signalLayout, final boolean reverse, final OnSlideAnimationEnd listener) {
        float translationY = signalLayout.getTranslationY();
        float signalHeight = signalLayout.getHeight();
        signalLayout.setTranslationY(reverse ? translationY : translationY + signalHeight);

        signalLayout.animate().translationY(reverse ? IntroActivity.screenParametersPoint.y : translationY).setDuration(SLIDE_ANIMATION_DURATION)
                .setInterpolator(reverse ? new DecelerateInterpolator() : new OvershootInterpolator(0.7f))
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        if (listener != null)
                            listener.onAnimationEnd();
                    }
                });


    }

    public static void animateBackgroundDim(final ViewGroup backgroundLayout, boolean reverse) {
        int transColor = reverse ? R.color.black_trans : android.R.color.transparent;
        int blackTransColor = reverse ? android.R.color.transparent : R.color.black_trans;

        ValueAnimator anim = new ValueAnimator();
        anim.setIntValues(transColor, blackTransColor);
        anim.setEvaluator(new ArgbEvaluator());
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                backgroundLayout.setBackgroundColor((Integer) valueAnimator.getAnimatedValue());
            }
        });

        anim.setDuration(SLIDE_ANIMATION_DURATION);
        anim.start();
    }

    public static void showDataFragment(Activity app, Asset asset) {
        if (app == null || asset == null) {
            Log.e(TAG, "showDataFragment: app or asset is null");
            return;
        }
        FragmentData fragmentData = (FragmentData) app.getFragmentManager().findFragmentByTag(FragmentData.class.getName());
        if (fragmentData != null && fragmentData.isAdded())
            return;
        fragmentData = new FragmentData();
        Bundle args = new Bundle();
        args.putParcelable("asset", asset);
        fragmentData.setArguments(args);
        fragmentData.show(app.getFragmentManager(), FragmentData.class.getName());
    }

    public static void showConfirmationFragment(Activity app, Asset asset, AssetType assetType, String address, ConfirmationListener listener) {
        if (app == null || asset == null) {
            Log.e(TAG, "showDataFragment: app or asset is null");
            return;
        }
        FragmentConfirmation fragment = (FragmentConfirmation) app.getFragmentManager()
                .findFragmentByTag(FragmentConfirmation.class.getName());
        if (fragment != null && fragment.isAdded())
            return;
        fragment = new FragmentConfirmation();
        Bundle args = new Bundle();
        args.putParcelable("asset", asset);
        if (!TextUtils.isEmpty(address))
            args.putString("address", address);
        if (assetType != null)
            args.putSerializable("type", assetType);
        fragment.setArguments(args);
        fragment.setConfirmationListener(listener);
        fragment.show(app.getFragmentManager(), FragmentConfirmation.class.getName());
    }

    public static void showTxCreatedFragment(Activity app, String txHash) {
        if (app == null || txHash == null) {
            Log.e(TAG, "showDataFragment: app or asset is null");
            return;
        }
        FragmentTxCreated fragment = (FragmentTxCreated) app.getFragmentManager()
                .findFragmentByTag(FragmentTxCreated.class.getName());
        if (fragment != null && fragment.isAdded())
            return;
        fragment = new FragmentTxCreated();
        Bundle args = new Bundle();
        args.putString("tx_hash", txHash);
        fragment.setArguments(args);
        fragment.show(app.getFragmentManager(), FragmentTxCreated.class.getName());
    }

    public static void showBurnAssetFragment(Activity app, Asset asset, BurnFragmentListener listener) {
        if (app == null || asset == null) {
            Log.e(TAG, "showBurnAssetFragment: app or asset is null");
            return;
        }
        FragmentBurnAsset fragmentBurnAsset = (FragmentBurnAsset) app.getFragmentManager().findFragmentByTag(FragmentBurnAsset.class.getName());
        if (fragmentBurnAsset != null && fragmentBurnAsset.isAdded())
            return;

        fragmentBurnAsset = new FragmentBurnAsset();
        fragmentBurnAsset.setListner(listener);
        Bundle args = new Bundle();
        args.putParcelable("asset", asset);
        fragmentBurnAsset.setArguments(args);
        fragmentBurnAsset.show(app.getFragmentManager(), FragmentBurnAsset.class.getName());

    }

    public interface OnSlideAnimationEnd {
        void onAnimationEnd();
    }

}