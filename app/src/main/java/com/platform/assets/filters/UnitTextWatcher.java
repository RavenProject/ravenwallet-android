package com.platform.assets.filters;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import java.math.BigDecimal;

public class UnitTextWatcher implements TextWatcher {

    private EditText editText;
    private String oldValue;
    private int assetUnit;

    public UnitTextWatcher(EditText inputField, int assetUnit) {
        this.editText = inputField;
        this.assetUnit = assetUnit;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        oldValue = s.toString();
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    public int getValue() {
        int value = 0;
        try {
            value = Integer.parseInt(editText.getText().toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

    @Override
    public void afterTextChanged(Editable s) {
        if (s.toString().isEmpty()) {
            editText.setText("0");
            return;
        } else {
            editText.setTextSize(24);
            editText.removeTextChangedListener(this);
            try {
                String originalString = s.toString();
                String newValue = originalString.substring(s.length() - 1, s.length());
                int newUnit = new BigDecimal(newValue).intValue();
                if (newUnit <= 8 && newUnit >= assetUnit) {
                    editText.setText(newValue);
                } else
                    editText.setText(oldValue);
                editText.setSelection(editText.getText().length());
            } catch (NumberFormatException nfe) {
                nfe.printStackTrace();
            }

            editText.addTextChangedListener(this);
        }
    }
}
