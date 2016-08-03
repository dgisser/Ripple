package com.facebook.peepingtom.UI;

import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.facebook.peepingtom.Activities.MainActivity;
import com.facebook.peepingtom.Activities.ProfileActivity;
import com.facebook.peepingtom.Database.DatabaseLayer;
import com.facebook.peepingtom.Fragments.AbstractFriendsFragment;
import com.facebook.peepingtom.Models.User;
import com.facebook.peepingtom.R;

import org.parceler.Parcels;

/**
 * Created by aespino on 7/26/16.
 */
public class FollowersView {
    ImageView ivProfile;
    TextView tvName;;
    Button btnFollow;
    User profileUser;
    User friend;
    User thisUser;
    int accentColor;
    int profileColor;

    public FollowersView(){}

    public void setUpFromView(View itemView){
        ivProfile = (ImageView) itemView.findViewById(R.id.ivProfile);
        tvName = (TextView) itemView.findViewById(R.id.tvName);
        btnFollow = (Button) itemView.findViewById(R.id.btnFollow);
    }


    public void configureFollowersView(final User friend, final AbstractFriendsFragment fragment){
        this.friend = friend;
        profileUser = fragment.profileUser;
        thisUser = fragment.thisUser;
        accentColor = profileUser.getColorHexAccent(fragment.getContext());
        profileColor = profileUser.getColorHexDark(fragment.getContext());
        tvName.setText(friend.getFirstName());
        if(thisUser.getUid().equals(friend.getUid())){
            btnFollow.setVisibility(View.GONE);
        }else{
            btnFollow.setVisibility(View.VISIBLE);
        }

        Glide.with(fragment.getContext()).load(friend.getProfileUrl()).centerCrop().into(ivProfile);
        ivProfile.setVisibility(View.VISIBLE);

        followSetUp();
        btnFollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickFollowToggle();
            }
        });
        ivProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(view.getContext(), ProfileActivity.class);
                i.putExtra("user", Parcels.wrap(friend));
                i.putExtra("activityTag", 1);
                fragment.getActivity().startActivityForResult(i, MainActivity.SENDMESSAGE);
            }
        });
    }

    public void onClickFollowToggle() {
        // if the user does not follow the profile user
        if (thisUser.getFollowing().get(friend.getUid()) == null) {
            btnFollow.setText("Following");
            ((GradientDrawable)btnFollow.getBackground()).setColor(accentColor);
            // add user to the profile user's followers
            friend.getFollowers().put(thisUser.getUid(), true);
            DatabaseLayer.updateUserFollowers(friend, thisUser.getUid(), true);
            // add profile user to user's following
            thisUser.getFollowing().put(friend.getUid(), true);
            DatabaseLayer.updateUserFollowing(thisUser, friend.getUid(), true);
        }
        // else if the user does follow the profile user
        else {
            btnFollow.setText("Follow");
            ((GradientDrawable)btnFollow.getBackground()).setColor(profileColor);
            // remove user from the profile user's followers
            friend.getFollowers().remove(thisUser.getUid());
            DatabaseLayer.updateUserFollowers(friend, thisUser.getUid(), false);
            // remove profile user from user's following
            thisUser.getFollowing().remove(friend.getUid());
            DatabaseLayer.updateUserFollowing(thisUser, friend.getUid(), false);
        }
    }

    // set up follow button
    public void followSetUp() {
        // if the user does not follow the profile user
        if (!thisUser.getFollowing().keySet().contains(friend.getUid())) {
            btnFollow.setText(R.string.follow);
            ((GradientDrawable)btnFollow.getBackground()).setColor(profileColor);
            // btnFollow.setBackgroundColor(Color.GRAY);
            //btnFollow.setTextColor(Color.BLACK);
        }
        // else if the user does follow the profile user
        else {
            btnFollow.setText("Following");
            ((GradientDrawable)btnFollow.getBackground()).setColor(accentColor);
            // btnFollow.setBackgroundColor(Color.BLUE);
            //btnFollow.setTextColor(Color.WHITE);
        }
    }


}
