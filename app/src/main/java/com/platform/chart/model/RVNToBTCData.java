package com.platform.chart.model;

import android.util.Log;

import com.google.gson.annotations.SerializedName;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RVNToBTCData {

    public List<SubData> getResult() {
        return result;
    }

    public void setResult(List<SubData> result) {
        this.result = result;
    }

    private List<SubData> result;

    private class SubData {
        public double getData() {
            return data;
        }

        public String getTime() {
            try {
                SimpleDateFormat inputFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                SimpleDateFormat outputFormatter = new SimpleDateFormat("MM-dd-yyyy", Locale.getDefault());
                Date date = inputFormatter.parse(time);
                return outputFormatter.format(date);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return time;
        }

        public void setData(double data) {
            this.data = data;
        }

        @SerializedName("C")
        private double data;
        @SerializedName("T")
        private String time;
    }

    public Object[] toObjectArray() {
        Collections.reverse(result);
        int arraySize = 190;//result.size();
        Object[] finalData = new Object[arraySize];

        for (int i = arraySize - 1; i >= 0; i--) {
            int j = 190 - i - 1;
            Object[] dataTime = new Object[2];
            dataTime[1] = (float) result.get(i).getData();
            dataTime[0] = result.get(i).getTime();
            finalData[j] = dataTime;
        }
        Log.d("TAG",finalData.toString());
        return finalData;
    }
}
