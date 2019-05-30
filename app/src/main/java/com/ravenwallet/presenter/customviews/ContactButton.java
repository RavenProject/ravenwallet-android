package com.ravenwallet.presenter.customviews;

import android.content.Context;
import android.util.AttributeSet;

import com.ravenwallet.R;

public class ContactButton extends IconButton {


    public ContactButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected int getIconId() {
        return R.drawable.ic_contact_24px;
    }
}