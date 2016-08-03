package com.facebook.peepingtom.Fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.facebook.peepingtom.Activities.MainActivity;
import com.facebook.peepingtom.Activities.ProfileActivity;
import com.facebook.peepingtom.Adapters.StoryRecyclerAdapter;
import com.facebook.peepingtom.Database.CacheManager;
import com.facebook.peepingtom.Database.DatabaseLayer;
import com.facebook.peepingtom.GlobalVars;
import com.facebook.peepingtom.Models.Story;
import com.facebook.peepingtom.Models.User;
import com.facebook.peepingtom.R;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DatabaseReference;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

/**
 * Created by aespino on 7/8/16.
 */
public class ProfileStoriesFragment extends AbstractStoryFragment
        implements DatabaseLayer.UserStory, GlobalVars.UserListener, ComposeBioFragment.ComposeBioListener {
    ComposeBioFragment composeBioFragment = ComposeBioFragment.newInstance();
    TextView tvName;
    TextView tvEditBio;
    TextView tvBio;
    ImageView ivProfile;
    RecyclerView rvStories;
    ProfileTabsFragment tabsFragment;
    Button btnFollow;
    FloatingActionButton fab;
    public int storyItemPosition = 0;
    // the user the profile belongs to
    public User profileUser;
    // the user using the app
    public User thisUser;
    public StoryRecyclerAdapter storyAdapter;
    public ArrayList<Story> stories = new ArrayList<>();
    private ChildEventListener childEventListener;
    private DatabaseReference reference;
    int darkColor;
    int accentColor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        profileUser = Parcels.unwrap(getActivity().getIntent().getParcelableExtra("user"));
        thisUser = ((GlobalVars) getActivity().getApplication()).getUser();
        fragmentFlag = 2;
        ((GlobalVars) getActivity().getApplication()).abstractStoryFragment = this;
        if (getActivity().getIntent().hasExtra("activityTag") && getActivity().getIntent().getIntExtra("activityTag", 0) != 1) {
            //in main activty
            ((MainActivity) getActivity()).abstractStoryFragment = this;
            if (profileUser.getUid().equals(thisUser.getUid())) {
                profileUser = thisUser;
                stories = ((MainActivity) getActivity()).profileStories;
            }
        } else {//not in main activity
            stories = new ArrayList<>();
            if (profileUser.getUid().equals(thisUser.getUid())) profileUser = thisUser;
        }
        accentColor = profileUser.getColorHexAccent(this.getContext());
        darkColor= profileUser.getColorHexDark(this.getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_profile_stories, container, false);
        tvName = (TextView) v.findViewById(R.id.tvName);
        tvBio = (TextView) v.findViewById(R.id.tvBio);
        ivProfile = (ImageView) v.findViewById(R.id.ivProfile);
        btnFollow = (Button) v.findViewById(R.id.btnFollow);
        fab = (FloatingActionButton) v.findViewById(R.id.fab);
        tvEditBio = (TextView) v.findViewById(R.id.tvEnterBio);
        tvEditBio.setTextColor(thisUser.getColorHexAccent(this.getContext()));
        // if user is looking at someone else's profile
        if (!profileUser.getUid().equals(thisUser.getUid())) {
            tvEditBio.setVisibility(View.GONE);
            fab.setVisibility(View.VISIBLE);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // sends profileUser and backFlag to MainActivity
                    Intent resultData = new Intent();
                    resultData.putExtra("otherUser", Parcels.wrap(profileUser));
                    // flags to MainActivity that it should go to messages
                    resultData.putExtra("backFlag", 0);
                    getActivity().setResult(MainActivity.RESULT_OK, resultData);
                    getActivity().finish();
                }
            });
            followSetUp();
            btnFollow.setVisibility(View.VISIBLE);
            btnFollow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onClickFollowToggle();
                }
            });
            tvName.setText(profileUser.getFirstName());
        } else {//user is looking at their own profile
            fab.setVisibility(View.GONE);
            if (profileUser.bio.isEmpty()) tvEditBio.setText("Enter a bio");
            else tvEditBio.setText("Edit your bio");
            tvEditBio.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onAddBio();
                }
            });
            tvName.setText(String.format("%s %s", profileUser.getFirstName(), profileUser.getLastName()));
        }
        if (!getActivity().getIntent().hasExtra("activityTag")) ((MainActivity)getActivity()).getSupportActionBar().setTitle("Profile");
        else ((ProfileActivity)getActivity()).getSupportActionBar().setTitle(profileUser.getFirstName());
        Glide.with(getContext()).load(profileUser.getProfileUrl()).centerCrop().into(ivProfile);
        tvBio.setText(profileUser.bio);

        // setup the recyclerview
        rvStories = (RecyclerView) v.findViewById(R.id.rvStories);
        //TODO: pass User into constructor
        storyAdapter = new StoryRecyclerAdapter(stories, this);
        rvStories.setAdapter(storyAdapter);
        rvStories.setLayoutManager(new LinearLayoutManager(getActivity()));
        //loadStories();
        if (childEventListener != null) reference.removeEventListener(childEventListener);
        DatabaseLayer.getUserStories(this, profileUser);
        updating = 1;
        return v;
    }

    @Override
    public void storyAdded(Story story, ChildEventListener listener, DatabaseReference reference) {
        this.childEventListener = listener;
        this.reference = reference;
        if (updating == 1) {
            CacheManager.dontDeleteList.clear();
            stories.clear();
            updating = 0;
        }
        if (!stories.contains(story)) {
            stories.add(0, story);
            //dont change this or there will be index inconsistencies
            storyAdapter.notifyDataSetChanged();
            storyAdapter.notifyItemRangeChanged(1 + storyAdapter.promptCount, stories.size());
        }
    }

    @Override
    public void storyRemoved(Story story) {
        int storyIndex = -1;
        Log.d("profilestories", String.valueOf(stories.size()));
        for (int x = 0; x < stories.size(); x++) {
            if (Objects.equals(stories.get(x).getStoryId(), story.getStoryId())) storyIndex = x;
        }
        if (storyIndex < 0) return;
        else {
            stories.remove(storyIndex);
            storyAdapter.notifyItemRemoved(storyIndex + 1 + storyAdapter.promptCount);///dont change this
            storyAdapter.notifyItemRangeChanged(storyIndex + 1 + storyAdapter.promptCount, stories.size());//dont change this
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (childEventListener != null) reference.removeEventListener(childEventListener);
    }

    @Override
    public void onUserChanged(User thisUser) {
        if (Objects.equals(profileUser.getUid(), thisUser.getUid()))
            profileUser = thisUser;
        storyAdapter.notifyItemChanged(0);
        if (tabsFragment!= null) tabsFragment.changeTabLabels();
    }

    //called when the questions are downloaded
    @Override
    public void onQuestionsDownloaded() {
        storyAdapter.notifyDataSetChanged();
    }

    public void onSubmitText(String text, int theme) {
        Story story = new Story();
        story.description = text;
        story.theme = theme;
        story.creationDate = new Date();
        story.setPoster(((GlobalVars) getActivity().getApplication()).getUser());
        // submit new story to server
        DatabaseLayer.submitStory(story);
        composeTextFragment.dismiss();
    }

    @Override
    public void deleteStory(Story story) {
        DatabaseLayer.deleteStory(story);
    }

    // intercepts camera result from device camera going to main activity
    // creates new story with image, removes empty one, adds to array and notifies the adapter
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == MainActivity.RESULT_OK) {
            Story story = new Story(null, null, null, new Date(), currentTheme,
                    ((GlobalVars) getActivity().getApplication()).getUser(), null);
            if (requestCode == MainActivity.CAMERA_PIC_REQUEST) {
                Bitmap image;
                if (data != null) image = (Bitmap) data.getExtras().get("data");
                else image = grabImage();
                story.setMedia(image);
                // submits story to server and puts uri in story
                DatabaseLayer.uploadImageStory(story);
            } else if (requestCode == MainActivity.CAMERA_VIDEO_REQUEST) {
                story.setVideoUri(imagePath);
                // submits story to server and puts uri in story
                DatabaseLayer.uploadImageStory(story);
            }
        }
    }

    // toggle between follow/not following in the button view and firebase
    public void onClickFollowToggle() {
        // if the user does not follow the profile user
        if (thisUser.getFollowing().get(profileUser.getUid()) == null) {
            btnFollow.setText("Following");
            ((GradientDrawable)btnFollow.getBackground()).setColor(accentColor);
            // add user to the profile user's followers
            profileUser.getFollowers().put(thisUser.getUid(), true);
            DatabaseLayer.updateUserFollowers(profileUser, thisUser.getUid(), true);
            // add profile user to user's following
            thisUser.getFollowing().put(profileUser.getUid(), true);
            DatabaseLayer.updateUserFollowing(thisUser, profileUser.getUid(), true);
             if(tabsFragment!= null)tabsFragment.changeTabLabels();
        }
        // else if the user does follow the profile user
        else {
            btnFollow.setText(R.string.follow);
            ((GradientDrawable)btnFollow.getBackground()).setColor(darkColor);
            // remove user from the profile user's followers
            profileUser.getFollowers().remove(thisUser.getUid());
            DatabaseLayer.updateUserFollowers(profileUser, thisUser.getUid(), false);
            // remove profile user from user's following
            thisUser.getFollowing().remove(profileUser.getUid());
            DatabaseLayer.updateUserFollowing(thisUser, profileUser.getUid(), false);
             if(tabsFragment!= null)tabsFragment.changeTabLabels();
        }
        storyAdapter.notifyItemChanged(0);
    }

    // set up follow button
    public void followSetUp() {
        // if the user does not follow the profile user
        if (!thisUser.getFollowing().keySet().contains(profileUser.getUid())) {
            btnFollow.setText(R.string.follow);
            ((GradientDrawable)btnFollow.getBackground()).setColor(darkColor);
        }
        // else if the user does follow the profile user
        else {
            btnFollow.setText("Following");
            ((GradientDrawable)btnFollow.getBackground()).setColor(accentColor);
        }
    }

    public void onAddBio() {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        composeBioFragment.show(fm, "fragment_compose_text");
        Bundle bundle = new Bundle();
        bundle.putString("currentBio", profileUser.getBio());
        composeBioFragment.setArguments(bundle);
        composeBioFragment.setTargetFragment(this, 0);
    }

    public void onSubmitBio(String newBio) {
        if (!newBio.isEmpty()) {
            thisUser.bio = newBio;
            DatabaseLayer.submitUser(thisUser);
            tvBio.setText(newBio);
        }
        composeBioFragment.dismiss();
    }

    @Override
    public void onVideoDownloaded(Story story) {
        Handler handler = new Handler();
        final Runnable r = new Runnable() {
            public void run() {
                storyAdapter.notifyDataSetChanged();
            }
        };
        handler.post(r);
    }
}
