package com.facebook.peepingtom;

import android.app.Application;
import android.util.Log;

import com.facebook.peepingtom.Fragments.AbstractStoryFragment;
import com.facebook.peepingtom.Fragments.FeedFragment;
import com.facebook.peepingtom.Fragments.ProfileStoriesFragment;
import com.facebook.peepingtom.Models.User;

/**
 * Created by dgisser on 7/13/16.
 */
public class GlobalVars extends Application {
    public interface UserListener {
        void onUserChanged(User user);
    }

    private User user;
    public AbstractStoryFragment abstractStoryFragment;

    public User getUser() { return user; }

    public void setUser(User user) {
        this.user = user;
        if (abstractStoryFragment != null) {
            if (abstractStoryFragment.fragmentFlag == 2)
                ((ProfileStoriesFragment) abstractStoryFragment).onUserChanged(user);
            else if (abstractStoryFragment.fragmentFlag == 1)
                ((FeedFragment) abstractStoryFragment).onUserChanged(user);
            else Log.d("globalvars", "no user flag -- ruh roh");
        }
    }
}
