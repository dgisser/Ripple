package com.facebook.peepingtom.Activities;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;

import com.facebook.login.LoginManager;
import com.facebook.login.widget.LoginButton;
import com.facebook.peepingtom.R;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;

public class SettingsActivity extends AppCompatActivity {
    private FirebaseAnalytics mTracker;
    LoginButton btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        setContentView(R.layout.activity_settings);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#DAD4D4D4")));
        getSupportActionBar().setStackedBackgroundDrawable(new ColorDrawable(Color.parseColor("#D97E7E7E")));
        mTracker = FirebaseAnalytics.getInstance(this);

        btnLogout = (LoginButton) findViewById(R.id.login_button);

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LoginManager.getInstance().logOut();//logsout of facebook
                FirebaseAuth.getInstance().signOut();//logs out of firebase
                Intent data = new Intent();
                data.putExtra("code", AccountActivity.LOGOUT_REQUEST_CODE);
                setResult(RESULT_OK, data);
                finish();
            }
        });
    }
}
