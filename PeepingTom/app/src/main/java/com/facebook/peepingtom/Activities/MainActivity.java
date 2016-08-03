package com.facebook.peepingtom.Activities;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentTabHost;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;

import com.facebook.peepingtom.Database.DatabaseLayer;
import com.facebook.peepingtom.Fragments.AbstractStoryFragment;
import com.facebook.peepingtom.Fragments.FeedFragment;
import com.facebook.peepingtom.Fragments.InboxFragment;
import com.facebook.peepingtom.Fragments.ProfileTabsFragment;
import com.facebook.peepingtom.Fragments.SearchFragment;
import com.facebook.peepingtom.GlobalVars;
import com.facebook.peepingtom.Models.ChatDescription;
import com.facebook.peepingtom.Models.Story;
import com.facebook.peepingtom.Models.User;
import com.facebook.peepingtom.R;
import com.google.firebase.auth.FirebaseAuth;

import org.parceler.Parcels;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity  implements DatabaseLayer.QuestionsDownloaded{
    public static final int CAMERA_PIC_REQUEST = 55;
    public static final int CAMERA_VIDEO_REQUEST = 65;
    private FirebaseAuth.AuthStateListener mAuthListener;

    public HashMap<Integer, ArrayList<Story>> feedDict;
    public ArrayList<Story> profileStories = new ArrayList<>();
    public static ArrayList<String> questionsList = new ArrayList<>();
    public ArrayList<ChatDescription> descriptionList; //inbox
    public HashMap<String, User> userMap; //inbox
    public User newMessageUser; //try to message this person
    public AbstractStoryFragment abstractStoryFragment;
    public SearchFragment searchFragment;
    public FragmentTabHost mTabHost;
    public static int SENDMESSAGE = 220;
    public User thisUser;

    public interface QuestionsDownloaded {
        void onQuestionsDownloaded();
    }

    public interface SetUser {
        void setUser(User user);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        thisUser = ((GlobalVars) getApplication()).getUser();
        setTheme(thisUser.getColorTheme());
        setContentView(R.layout.activity_main);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#DAD4D4D4")));
        getSupportActionBar().setStackedBackgroundDrawable(new ColorDrawable(Color.parseColor("#D97E7E7E")));


        mTabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
        mTabHost.setup(this, getSupportFragmentManager(), android.R.id.tabcontent);

        if (Build.VERSION.SDK_INT >= 21) { //loads images into tabs for devices with sdk >= 21
            mTabHost.addTab(
                    mTabHost.newTabSpec("feed").setIndicator("", getDrawable(R.drawable.ic_home)),
                    FeedFragment.class, null);
            mTabHost.addTab(
                    mTabHost.newTabSpec("profile").setIndicator("", getDrawable(R.drawable.ic_profile)),
                    ProfileTabsFragment.class, null);
            mTabHost.addTab(
                    mTabHost.newTabSpec("search").setIndicator("", getDrawable(R.drawable.ic_search)),
                    SearchFragment.class, null);
            mTabHost.addTab(
                    mTabHost.newTabSpec("inbox").setIndicator("", getDrawable(R.drawable.ic_inbox)),
                    InboxFragment.class, null);
        } else {//loads images into tabs for devices with sdk <21
            mTabHost.addTab(
                    mTabHost.newTabSpec("feed").setIndicator("", getResources().getDrawable(R.drawable.ic_home)),
                    FeedFragment.class, null);
            mTabHost.addTab(
                    mTabHost.newTabSpec("profile").setIndicator("", getResources().getDrawable(R.drawable.ic_profile)),
                    ProfileTabsFragment.class, null);
            mTabHost.addTab(
                    mTabHost.newTabSpec("search").setIndicator("", getResources().getDrawable(R.drawable.ic_search)),
                    SearchFragment.class, null);
            mTabHost.addTab(
                    mTabHost.newTabSpec("inbox").setIndicator("", getResources().getDrawable(R.drawable.ic_inbox)),
                    InboxFragment.class, null);
        }

        File file = new File(getBaseContext().getFilesDir(), "questionslist.csv");
        if (file.exists()) questionsList = DatabaseLayer.readQuestions(file);
        try {
            DatabaseLayer.downloadQuestions(this, getApplicationContext());
        } catch (IOException e) {
            Log.d("accountactivity", e.toString());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_actionbar, menu);
        return true;
    }

    public void onSettingsClick(MenuItem mi){
        Intent i = new Intent(getApplicationContext(), SettingsActivity.class);
        startActivityForResult(i, AccountActivity.LOGOUT_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // REQUEST_CODE is defined above
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == AccountActivity.LOGOUT_REQUEST_CODE) finish();
            else if (requestCode == MainActivity.SENDMESSAGE) {
                // backFlag = 0 means going to message, = 1 means going back to search
                int backFlag = data.getIntExtra("backFlag", 1);
                // opens up messages tab
                if (backFlag == 0) {
                    //send to inboxfrag
                    newMessageUser = Parcels.unwrap(data.getParcelableExtra("otherUser"));
                    changeTab(3);
                } else searchFragment.setUser((User)Parcels.unwrap(data.getParcelableExtra("profileUser")));
            }
        }
        // calls method in search fragment

    }

    //callback for when questions have been downloaded
    @Override
    public void onQuestionsDownloaded(ArrayList<String> questions) {
        questionsList = questions;
        abstractStoryFragment.onQuestionsDownloaded();
    }

    public void setCustomColors(){

    }

    public void changeTab(int changeTo){
        mTabHost.setCurrentTab(changeTo);
    }

    @Override
    public void onBackPressed() {

    }
}
