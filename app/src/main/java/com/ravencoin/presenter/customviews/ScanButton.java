package com.ravencoin.presenter.customviews;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.ravencoin.R;

public class ScanButton extends IconButton {


    public ScanButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected int getIconId() {
        return R.drawable.ic_scan;
    }
}