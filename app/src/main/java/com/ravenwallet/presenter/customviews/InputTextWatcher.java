package com.ravenwallet.presenter.customviews;

import com.google.android.material.textfield.TextInputLayout;
import android.text.Editable;
import android.text.TextWatcher;

public class InputTextWatcher implements TextWatcher {

    private TextInputLayout mInputLaout;

    public InputTextWatcher(TextInputLayout mInputLaout) {
        this.mInputLaout = mInputLaout;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        mInputLaout.setError(null);
        mInputLaout.setErrorEnabled(false);
    }
}
