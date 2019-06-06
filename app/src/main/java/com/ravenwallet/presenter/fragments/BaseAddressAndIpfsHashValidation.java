package com.ravenwallet.presenter.fragments;

import android.app.Activity;
import android.util.Log;
import android.widget.EditText;

import com.ravenwallet.R;
import com.ravenwallet.presenter.customviews.BRDialogView;
import com.ravenwallet.tools.animation.BRAnimator;
import com.ravenwallet.tools.animation.BRDialog;
import com.ravenwallet.tools.manager.BRClipboardManager;
import com.ravenwallet.tools.manager.BRSharedPrefs;
import com.ravenwallet.tools.util.Utils;


/**
 * RavenWallet
 * <p>
 * Created by Mihail Gutan <mihail@breadwallet.com> on 6/29/15.
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

public class BaseAddressAndIpfsHashValidation extends BaseAddressValidation {
    private static final String TAG = BaseAddressAndIpfsHashValidation.class.getName();

    protected EditText ipfsHashEditText;
    private final static String IPFS_HASH_ADDRESS_START = "Qm";
    private final static int IPFS_HASH_ADDRESS_LENGTH = 46;

    public boolean isIPFSHashValid(String ipfsHash) {
        return ipfsHash.startsWith(IPFS_HASH_ADDRESS_START) && ipfsHash.length() == IPFS_HASH_ADDRESS_LENGTH;
    }

    protected void postIPFSHashPasted(final EditText ipfsHashInputField) {
        if (!BRAnimator.isClickAllowed()) return;
        String ipfsHash = BRClipboardManager.getClipboard(getActivity());
        if (Utils.isNullOrEmpty(ipfsHash)) {
            sayClipboardEmpty();
            return;
        }

        if (isIPFSHashValid(ipfsHash)) {
            final Activity app = getActivity();
            if (app == null) {
                Log.e(TAG, "paste onClick: app is null");
                return;
            }
            ipfsHashInputField.setText(ipfsHash);
        } else {
            sayInvalidIPFSHash();
        }
    }

    public void sayInvalidIPFSHash() {
        BRDialog.showCustomDialog(getActivity(), "", getResources().getString(R.string.ipfs_hash_invalid), getString(R.string.AccessibilityLabels_close), null, new BRDialogView.BROnClickListener() {
            @Override
            public void onClick(BRDialogView brDialogView) {
                brDialogView.dismiss();
            }
        }, null, null, 0);
        BRClipboardManager.putClipboard(getActivity(), "");
    }

    public void setIPFSHash(String ipfsHash) {
        ipfsHashEditText.setText(ipfsHash);
    }

    public boolean isShowAddressInput() {
        return BRSharedPrefs.getExpertMode(getActivity()) && BRSharedPrefs.getShowAddressInput(getActivity());
    }
}