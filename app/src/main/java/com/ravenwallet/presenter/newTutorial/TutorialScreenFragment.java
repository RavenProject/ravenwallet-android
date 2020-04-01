package com.ravenwallet.presenter.newTutorial;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ravenwallet.R;


public class TutorialScreenFragment extends Fragment {

    private static String SCREEN_PAGE_KEY_EXTRAS = "screen.page.key.extras";

    public static TutorialScreenFragment newInstance(int screenPage) {
        TutorialScreenFragment fragment = new TutorialScreenFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(SCREEN_PAGE_KEY_EXTRAS, screenPage);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.fragment_tutorial, container, false);

        TextView title = rootView.findViewById(R.id.tutorial_title);
        ImageView image = rootView.findViewById(R.id.tutorial_image);
        TextView text = rootView.findViewById(R.id.tutorial_text);

        if (getArguments() != null) {
            int screenPage = getArguments().getInt(SCREEN_PAGE_KEY_EXTRAS);
            switch (screenPage) {
                case 0:
                    title.setText(getString(R.string.tutorial_first_screen_title));
                    image.setImageResource(R.drawable.ic_tutorial_first_screen);
                    text.setText(getString(R.string.tutorial_first_screen_text));
                    break;
                case 1:
                    title.setText(getString(R.string.tutorial_second_screen_title));
                    image.setImageResource(R.drawable.ic_tutorial_second_screen);
                    text.setText(getString(R.string.tutorial_second_screen_text));
                    break;
                case 2:
                    title.setText(getString(R.string.tutorial_third_screen_title));
                    image.setImageResource(R.drawable.ic_tutorial_third_screen);
                    text.setText(getString(R.string.tutorial_third_screen_text));
                    break;
                case 3:
                    title.setText(getString(R.string.tutorial_fourth_screen_title));
                    image.setImageResource(R.drawable.ic_tutorial_fourth_screen);
                    text.setText(getString(R.string.tutorial_fourth_screen_text));
                    break;
                case 4:
                    title.setText(getString(R.string.tutorial_fifth_screen_title));
                    image.setImageResource(R.drawable.ic_tutorial_fifth_screen);
                    text.setText(getString(R.string.tutorial_fifth_screen_text));
                    break;
                default:
                    throw new IllegalArgumentException("Argument unexpected");
            }
        }

        return rootView;
    }
}
