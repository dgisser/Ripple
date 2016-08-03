package com.facebook.peepingtom.Fragments;

import android.os.Bundle;

import com.facebook.peepingtom.Database.DatabaseLayer;
import com.facebook.peepingtom.GlobalVars;

import org.parceler.Parcels;

import java.util.ArrayList;

/**
 * Created by aespino on 7/25/16.
 */
public class FollowersFragment extends AbstractFriendsFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        profileUser = Parcels.unwrap(getActivity().getIntent().getParcelableExtra("user"));
        thisUser = ((GlobalVars) getActivity().getApplication()).getUser();
        if(profileUser.getUid().equals(thisUser.getUid())) {
            DatabaseLayer.getFollowUsers(this, new ArrayList<>(thisUser.getFollowers().keySet()));
        } else {
            DatabaseLayer.getFollowUsers(this, new ArrayList<>(profileUser.getFollowers().keySet()));
        }
    }

    @Override
    public void onFriendsChanged() {
        if(profileUser.getUid().equals(thisUser.getUid())) {
            DatabaseLayer.getFollowUsers(this, new ArrayList<>(thisUser.getFollowers().keySet()));
        } else {
            DatabaseLayer.getFollowUsers(this, new ArrayList<>(profileUser.getFollowers().keySet()));
        }
        followAdapter.notifyDataSetChanged();
    }
}
