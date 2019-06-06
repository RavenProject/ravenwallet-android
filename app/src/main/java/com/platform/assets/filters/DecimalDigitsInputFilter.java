package com.platform.assets.filters;

import android.text.InputFilter;
import android.text.Spanned;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DecimalDigitsInputFilter implements InputFilter {

        Pattern mPattern;

        public DecimalDigitsInputFilter(int digitsAfterZero) {
            if (digitsAfterZero > 0)
                mPattern = Pattern.compile("[0-9]{0,}+((\\.[0-9]{0," + (Math.max(0, digitsAfterZero - 1)) + "})?)||(\\.)?");
            else {
                mPattern = Pattern.compile("[0-9]{0,}+");
            }
        }

        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {

            Matcher matcher = mPattern.matcher(dest);
            if (!matcher.matches())
                return "";
            return null;
        }
    }

