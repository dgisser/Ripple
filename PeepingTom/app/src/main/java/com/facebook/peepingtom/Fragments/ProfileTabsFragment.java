package com.facebook.peepingtom.Fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTabHost;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.peepingtom.GlobalVars;
import com.facebook.peepingtom.Models.User;
import com.facebook.peepingtom.R;

/**
 * Created by aespino on 7/25/16.
 */
public class ProfileTabsFragment extends android.support.v4.app.Fragment{
    private FragmentTabHost mTabHost;
    public ProfileStoriesFragment profileStoriesFragment;
    public User profileUser;
    public FollowersFragment followersFragment;
    public FollowingFragment followingFragment;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                                Bundle savedInstanceState) {
        int followersCount = 0;
        int followingCount = 0;
        if (profileUser == null){
            profileUser = ((GlobalVars) getActivity().getApplication()).getUser();
        }
        followersCount = profileUser.getFollowers().size();
        followingCount = profileUser.getFollowing().size();
        mTabHost = new FragmentTabHost(getActivity());
        mTabHost.setup(getActivity(), getChildFragmentManager(), R.id.mTabhost);

        mTabHost.addTab(mTabHost.newTabSpec("Stories").setIndicator("Stories"),
                ProfileStoriesFragment.class, null);
        mTabHost.addTab(mTabHost.newTabSpec("Followers").setIndicator("Followers: " + followersCount),
                FollowersFragment.class, null);
        mTabHost.addTab(mTabHost.newTabSpec("Following").setIndicator("Following: " + followingCount ),
                FollowingFragment.class, null);
        TextView x1 = (TextView) mTabHost.getTabWidget().getChildAt(0).findViewById(android.R.id.title);
        x1.setTextSize(11);
        TextView x2 = (TextView) mTabHost.getTabWidget().getChildAt(1).findViewById(android.R.id.title);
        x2.setTextSize(11);
        TextView x3 = (TextView) mTabHost.getTabWidget().getChildAt(2).findViewById(android.R.id.title);
        x3.setTextSize(11);
        mTabHost.getTabWidget().getChildAt(0).getLayoutParams().height = 120;
        mTabHost.getTabWidget().getChildAt(1).getLayoutParams().height = 120;
        mTabHost.getTabWidget().getChildAt(2).getLayoutParams().height = 120;
        mTabHost.setCurrentTab(0);
        profileStoriesFragment = (ProfileStoriesFragment) getChildFragmentManager().findFragmentByTag("Stories");
        followersFragment = (FollowersFragment) getChildFragmentManager().findFragmentByTag("Followers");
        followingFragment = (FollowingFragment) getChildFragmentManager().findFragmentByTag("Following");
        if(profileStoriesFragment != null )profileStoriesFragment.tabsFragment = this;
        mTabHost.setPadding(0,200,0,0);
        return mTabHost;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mTabHost = null;
    }


    public void changeTabLabels(){
        int followersCount = profileUser.getFollowers().size();
        int followingCount = profileUser.getFollowing().size();
        ((TextView) mTabHost.getTabWidget().getChildAt(1)
                .findViewById(android.R.id.title)).setText("Followers: " + followersCount);
        ((TextView) mTabHost.getTabWidget().getChildAt(2)
                .findViewById(android.R.id.title)).setText("Following: " + followingCount);
        if ( followersFragment != null) {
            followersFragment.onFriendsChanged();
        }
        if ( followingFragment != null) {
            followingFragment.onFriendsChanged();
        }
    }

}