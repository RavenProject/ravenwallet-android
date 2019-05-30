package com.platform.assets.filters;

import android.text.InputFilter;
import android.text.Spanned;

import java.util.regex.Pattern;

public class AddressLabelFilter implements InputFilter {

    Pattern mPattern;

    public AddressLabelFilter() {
        mPattern = Pattern.compile("^[a-zA-Z0-9._ ]+");
    }


    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        try {
            String input = source.toString();
            if (isNameValid(input.toUpperCase()))
                return null;
        } catch (NumberFormatException nfe) {
            nfe.printStackTrace();
        }
        return "";
    }

    private boolean isNameValid(String name) {
        return mPattern.matcher(name).matches();
    }
}