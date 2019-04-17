package com.platform.assets;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import java.math.BigDecimal;

public class UnitTextWatcher implements TextWatcher {

        private EditText editText;
        private String oldValue;

        public UnitTextWatcher(EditText inputField) {
            this.editText = inputField;
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
                    if (new BigDecimal(newValue).intValue() <= 8) {
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
