package com.facebook.peepingtom.Adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.peepingtom.Fragments.AbstractFriendsFragment;
import com.facebook.peepingtom.Models.User;
import com.facebook.peepingtom.R;
import com.facebook.peepingtom.UI.FollowersView;

import java.util.ArrayList;

/**
 * Created by aespino on 7/26/16.
 */
public class FollowersRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    User profileUser;
    ArrayList<User> followersList;
    AbstractFriendsFragment abstractFriendsFragment;


    public FollowersRecyclerAdapter(User user, ArrayList<User> followers, AbstractFriendsFragment abstractFriendsFragment) {
        this.abstractFriendsFragment = abstractFriendsFragment;
        profileUser = user;
        followersList = followers;
    }

    // VIEWHOLDER CLASSES
    public static class FollowersViewHolder extends RecyclerView.ViewHolder {
        FollowersView followersView = new FollowersView();
        public FollowersViewHolder(View itemView) {
            super(itemView);
            followersView.setUpFromView(itemView);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View FollowerView = inflater.inflate(R.layout.item_profile_follower, parent, false);
        FollowersViewHolder followersViewHolder = new FollowersViewHolder(FollowerView);
        return followersViewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        User friend = followersList.get(position);
        FollowersViewHolder followersViewHolder = (FollowersViewHolder) holder;
        //configure the view in myAttributes view
        followersViewHolder.followersView.configureFollowersView(friend, abstractFriendsFragment);

    }

    @Override
    public int getItemCount() {
        if(followersList!= null)
            return followersList.size();
        return 0;
       }
}
