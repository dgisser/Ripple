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

import com.facebook.peepingtom.Activities.MainActivity;
import com.facebook.peepingtom.GlobalVars;
import com.facebook.peepingtom.Models.User;
import com.facebook.peepingtom.R;

/**
 * Created by sophiehouser on 7/11/16.
 */
public class ComposeTextFragment extends DialogFragment {

    EditText etText;
    Button btnSubmit;
    TextView tvComposeQuestion;
    User thisUser;

    // implemented by AbstractStoryFragment
    public interface ComposeTextListener {
        void onSubmitText(String string, int theme);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        thisUser = ((GlobalVars)getActivity().getApplication()).getUser();
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
            final int theme = getArguments().getInt("theme");
            tvComposeQuestion.setText(MainActivity.questionsList.get(theme));
        ((GradientDrawable)tvComposeQuestion.getBackground()).setColorFilter(
                thisUser.getColorHexDark(this.getContext()), PorterDuff.Mode.SRC_OVER);
            // done button listener
            btnSubmit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //TODO must have text to submit
                    // listener is the fragment the dialog is opening in (Feed or Profile)
                    InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    ComposeTextListener listener = (ComposeTextListener) getTargetFragment();
                    if (!etText.getText().toString().trim().isEmpty()) {
                        listener.onSubmitText(etText.getText().toString(), theme);
                        etText.setText("");
                    } else{
                        dismiss();
                    }
                }
            });
        return view;
    }

    // returns new instance of fragment ComposeTextFragment
    public static ComposeTextFragment newInstance() {
        ComposeTextFragment composeTextFragment = new ComposeTextFragment();
        return composeTextFragment;
    }
    public void onResume() {
        super.onResume();
        Window window = getDialog().getWindow();
        window.setLayout(1300, 1300);//change dialog size here
        window.setGravity(Gravity.CENTER);
    }


    public ComposeTextFragment() {
        // Required empty public constructor
    }
}
