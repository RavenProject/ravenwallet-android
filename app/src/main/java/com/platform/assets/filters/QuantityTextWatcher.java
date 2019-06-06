package com.platform.assets.filters;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.EditText;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import static com.ravenwallet.tools.util.BRConstants.MAX_ASSET_QUANTITY;

public class QuantityTextWatcher implements TextWatcher {

    private EditText editText;
    private double oldValue;
    DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(Locale.US);

    public QuantityTextWatcher(EditText inputField) {
        this.editText = inputField;
        formatter.applyPattern("#,###,###");
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        oldValue = getQuantityFromString(s);
    }

    private double getQuantityFromString(CharSequence s) {
        double value = 0;
        try {
            value = formatter.parse(s.toString()).doubleValue();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

    public double getValue() {
        double value = 0;
        try {
            if (!TextUtils.isEmpty(editText.getText().toString()))
                value = formatter.parse(editText.getText().toString()).doubleValue();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        if (s.toString().isEmpty()) {
            editText.setTextSize(16);
        } else {
            editText.setTextSize(24);
        }
//        editText.removeTextChangedListener(this);
//
//        try {
//            String originalString = s.toString();
//            double newValue = getQuantityFromString(s);
//            String formattedString;
//            if (new BigDecimal(newValue).doubleValue() <= MAX_ASSET_QUANTITY) {
//                formattedString = formatter.format(newValue);
//            } else formattedString = formatter.format(oldValue);
//
//            //setting text after format to EditText
//            editText.setText(formattedString);
//            editText.setSelection(editText.getText().length());
//        } catch (NumberFormatException nfe) {
//            nfe.printStackTrace();
//        }
//
//        editText.addTextChangedListener(this);
    }
}
