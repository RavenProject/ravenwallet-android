package com.ravenwallet.presenter.fragments;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.platform.assets.Asset;
import com.ravenwallet.R;


public class FragmentBurnAsset extends DialogFragment {

    private static final String TAG = FragmentBurnAsset.class.getSimpleName();
    private Asset mAsset;

    private Button mOkButton, mCancelButton;
    private BurnFragmentListener listener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_TITLE, 0);

    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return super.onCreateDialog(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_burn_asset, container, false);
        mOkButton = rootView.findViewById(R.id.ok_button);
        mCancelButton = rootView.findViewById(R.id.cancel_button);

        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        mOkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.performBurn();
                dismiss();
            }
        });

        updateUi();
        return rootView;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    private void updateUi() {
        Bundle b = getArguments();
        if (b == null || b.getParcelable("asset") == null)
            return;
        mAsset = b.getParcelable("asset");
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void setListner(BurnFragmentListener listener) {
        this.listener = listener;
    }
}
