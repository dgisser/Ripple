package com.facebook.peepingtom.Fragments;

import android.os.Bundle;

import com.facebook.peepingtom.Database.DatabaseLayer;
import com.facebook.peepingtom.GlobalVars;

import org.parceler.Parcels;

import java.util.ArrayList;

/**
 * Created by aespino on 7/25/16.
 */
public class FollowingFragment extends AbstractFriendsFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        profileUser = Parcels.unwrap(getActivity().getIntent().getParcelableExtra("user"));
        thisUser = ((GlobalVars) getActivity().getApplication()).getUser();
        if(profileUser.getUid().equals(thisUser.getUid())) {
            DatabaseLayer.getFollowUsers(this, new ArrayList<>(thisUser.getFollowing().keySet()));
        } else {
            DatabaseLayer.getFollowUsers(this, new ArrayList<>(profileUser.getFollowing().keySet()));
        }
    }

    @Override
    public void onFriendsChanged() {
        if(profileUser.getUid().equals(thisUser.getUid())) {
            DatabaseLayer.getFollowUsers(this, new ArrayList<>(thisUser.getFollowing().keySet()));
        } else {
            DatabaseLayer.getFollowUsers(this, new ArrayList<>(profileUser.getFollowing().keySet()));
        }
        followAdapter.notifyDataSetChanged();
    }
}
