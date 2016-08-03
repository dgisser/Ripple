package com.facebook.peepingtom.Fragments;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.facebook.peepingtom.GlobalVars;
import com.facebook.peepingtom.R;

/**
 * Created by aespino on 7/25/16.
 */
public class ComposeBioFragment extends DialogFragment {

    EditText etText;
    Button btnSubmit;
    TextView tvComposeQuestion;
    int darkColor;
    int accentColor;

    // implemented by AbstractStoryFragment
    public interface ComposeBioListener {
        void onSubmitBio(String string);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        darkColor = ((GlobalVars)getActivity().getApplication()).getUser().getColorHexDark(getContext());
        accentColor = ((GlobalVars)getActivity().getApplication()).getUser().getColorHexAccent(getContext());

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getDialog().setCanceledOnTouchOutside(true);

        View view = inflater.inflate(R.layout.fragment_compose_text, container, false);
        etText = (EditText) view.findViewById(R.id.etText);
        btnSubmit = (Button) view.findViewById(R.id.btnSubmit);
        tvComposeQuestion = (TextView) view.findViewById(R.id.tvComposeQuestion);
            final String currentBio = getArguments().getString("currentBio");
            tvComposeQuestion.setText("Edit your bio");
         ((GradientDrawable)tvComposeQuestion.getBackground()).setColorFilter(
                darkColor, PorterDuff.Mode.SRC_OVER);
            etText.setText(currentBio);
            btnSubmit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //TODO must have text to submit
                    // listener is the fragment the dialog is opening in (Feed or Profile)
                    InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    ComposeBioListener listener = (ComposeBioListener) getTargetFragment();
                    listener.onSubmitBio(etText.getText().toString());
                }
            });
        return view;
    }

    // returns new instance of fragment ComposeTextFragment
    public static ComposeBioFragment newInstance() {
        ComposeBioFragment composeBioFragment = new ComposeBioFragment();
        return composeBioFragment;
    }

    public void onResume() {
        super.onResume();
        Window window = getDialog().getWindow();
        window.setLayout(1300, 1300);//change dialog size here
        window.setGravity(Gravity.CENTER);
    }


    public ComposeBioFragment() {
        // Required empty public constructor
    }






}
