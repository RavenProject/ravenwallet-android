package com.platform.assets;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public class Utils {

    public static String formatAssetAmount(double amount, int units) {
        if (units > 0) {
            NumberFormat formatter = new DecimalFormat("#0.00000000");
            String formattedNumber = formatter.format(amount);

            String[] amountSplit = formattedNumber.split("\\.");
            String decimalBasePart = amountSplit[0];
            String decimalPointPart = amountSplit[1];

            String formattedDecimalPointPart = decimalPointPart.substring(0, units);

            return decimalBasePart.concat(".").concat(formattedDecimalPointPart);
        } else {
            NumberFormat formatter = new DecimalFormat("#0");
            return formatter.format(amount);
        }
    }
}
