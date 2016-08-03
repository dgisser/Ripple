package com.facebook.peepingtom.Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.peepingtom.Adapters.FollowersRecyclerAdapter;
import com.facebook.peepingtom.Database.DatabaseLayer;
import com.facebook.peepingtom.Models.User;
import com.facebook.peepingtom.R;

import java.util.ArrayList;

/**
 * Created by aespino on 7/26/16.
 */
public abstract class AbstractFriendsFragment extends Fragment implements DatabaseLayer.FollowUsers{
    public User profileUser;
    // the user using the app
    public User thisUser;
    FollowersRecyclerAdapter followAdapter; //can be followers or following
    ArrayList<User> followList = new ArrayList<>();
    public RecyclerView rvFollow; //can be followers or following


    public abstract void onFriendsChanged();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_followers, container, false);
        rvFollow = (RecyclerView) v.findViewById(R.id.rvFollowers);
        followAdapter = new FollowersRecyclerAdapter(profileUser, followList, this);
        rvFollow.setAdapter(followAdapter);
        rvFollow.setLayoutManager(new LinearLayoutManager(getActivity()));
        return v;
    }

    @Override
    public void onFollowUsersFetched(User user) {
        followList.add(user);
        followAdapter.notifyItemInserted(followList.size() - 1);
    }
}
