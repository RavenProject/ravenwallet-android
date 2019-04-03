package com.ravencoin.presenter.customviews;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.ImageButton;

import com.ravencoin.R;

public class ContactButton extends IconButton {


    public ContactButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected int getIconId() {
        return R.drawable.ic_contact_24px;
    }
}