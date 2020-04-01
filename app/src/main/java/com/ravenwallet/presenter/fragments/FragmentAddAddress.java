package com.ravenwallet.presenter.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.platform.addressBook.AddressBookItem;
import com.platform.addressBook.AddressBookRepository;
import com.platform.addressBook.event.OnAddressSavedListener;
import com.platform.assets.filters.AddressLabelFilter;
import com.ravenwallet.R;
import com.ravenwallet.presenter.customviews.PasteButton;
import com.ravenwallet.presenter.customviews.ScanButton;
import com.ravenwallet.tools.animation.BRAnimator;
import com.ravenwallet.tools.util.BRConstants;
import com.ravenwallet.tools.util.Utils;

import static com.ravenwallet.tools.animation.BRAnimator.animateBackgroundDim;
import static com.ravenwallet.tools.animation.BRAnimator.animateSignalSlide;
import static com.ravenwallet.tools.util.BRConstants.MAX_ADDRESS_NAME_LENGTH;


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

public class FragmentAddAddress extends BaseAddressValidation {

    private ScrollView backgroundLayout;
    private LinearLayout signalLayout;

    private OnAddressSavedListener listener;

    public static FragmentAddAddress newInstance() {
        return new FragmentAddAddress();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_add_address, container, false);

        backgroundLayout = rootView.findViewById(R.id.background_layout);
        signalLayout = rootView.findViewById(R.id.signal_layout);
        addressEditText = rootView.findViewById(R.id.address_edit);
        addressNameEditText = rootView.findViewById(R.id.address_name);
        addressNameEditText.setFilters(new InputFilter[]{new AddressLabelFilter(), new InputFilter.LengthFilter(MAX_ADDRESS_NAME_LENGTH)});

        setListeners(rootView);

        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        listener = (OnAddressSavedListener) getActivity();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @Override
    public void onStop() {
        super.onStop();
        if (isRemoving()) {
            animateBackgroundDim(backgroundLayout, true);
            animateSignalSlide(signalLayout, true, new BRAnimator.OnSlideAnimationEnd() {
                @Override
                public void onAnimationEnd() {
                    Activity app = getActivity();
                    if (app != null && !app.isDestroyed())
                        app.getFragmentManager().popBackStack();
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

    private void setListeners(final View rootView) {
        // Handling the paste button click
        PasteButton paste = rootView.findViewById(R.id.paste_button);
        paste.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postAddressPasted(addressEditText);
            }
        });

        // Handling the scan button click
        ScanButton scan = rootView.findViewById(R.id.scan_button);
        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!BRAnimator.isClickAllowed()) return;
                BRAnimator.openAddressScanner(getActivity(), BRConstants.ADDRESS_SCANNER_REQUEST);

            }
        });

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

        // Handling the add button click
        Button addButton = rootView.findViewById(R.id.add_button);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateAllInputFields(rootView);
            }
        });

    }

    private void validateAllInputFields(View rootView) {
        // Checking the Address validity
        String addressValue = addressEditText.getText().toString().trim();
        if (!isAddressValid(addressValue)) {
            sayInvalidClipboardData();
            return;
        }

        // Check the address name validity
        String addressName = addressNameEditText.getText().toString().trim();
        if (addressName.isEmpty()) {
            sayCustomMessage(getString(R.string.invalid_address_name));
            return;
        }

        // Otherwise, save the address to the AddressBook
        saveAddress(addressName, addressValue);

        // Close the current fragment
        getActivity().getFragmentManager().popBackStack();
    }

    private void saveAddress(String addressName, String addressValue) {
        AddressBookRepository repository = AddressBookRepository.getInstance(getActivity());
        boolean result = repository.insertAddress(new AddressBookItem(addressName, addressValue));
        if (result && listener != null) {
            // Notify the activity to update the address book list
            listener.onAddressSaved();

            // Hide the keyboard
            Utils.hideKeyboard(getActivity());
        }
    }

    public void setAddress(String address) {
        addressEditText.setText(address);
    }
}