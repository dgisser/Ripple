package com.facebook.peepingtom.Activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.view.View;
import android.view.Window;

import com.facebook.peepingtom.Fragments.ProfileTabsFragment;
import com.facebook.peepingtom.Models.User;
import com.facebook.peepingtom.R;

import org.parceler.Parcels;

/**
 * Created by sophiehouser on 7/21/16.
 */
public class ProfileActivity extends AppCompatActivity {
    private ProfileTabsFragment profileTabsFragment;
    public User profileUser;

    @Override
    public View onCreateView(String name, Context context, AttributeSet attrs) {
        return super.onCreateView(name, context, attrs);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        setContentView(R.layout.activity_profile);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#DAD4D4D4")));
        getSupportActionBar().setStackedBackgroundDrawable(new ColorDrawable(Color.parseColor("#D97E7E7E")));
        profileUser = Parcels.unwrap(getIntent().getParcelableExtra("user"));
        setTheme(profileUser.getColorTheme());
        profileTabsFragment = new ProfileTabsFragment();
        profileTabsFragment.profileUser = profileUser;
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.flFragmentHolder, profileTabsFragment, null);
        ft.commit();
    }

    // passes updated user back to adapter list
    @Override
    public void onBackPressed()
    {
        Intent resultData = new Intent();
        resultData.putExtra("profileUser", Parcels.wrap(profileUser));
        // tells MainActivity that it should go back to SearchFragment and update profileUser
        resultData.putExtra("backFlag", 1);
        setResult(MainActivity.RESULT_OK, resultData);
        finish();
    }
}
