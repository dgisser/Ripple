package com.facebook.peepingtom.Fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.peepingtom.Activities.MainActivity;
import com.facebook.peepingtom.Adapters.FeedRecyclerAdapter;
import com.facebook.peepingtom.Database.CacheManager;
import com.facebook.peepingtom.Database.DatabaseLayer;
import com.facebook.peepingtom.GlobalVars;
import com.facebook.peepingtom.Models.BasicStory;
import com.facebook.peepingtom.Models.Story;
import com.facebook.peepingtom.Models.User;
import com.facebook.peepingtom.R;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class FeedFragment extends AbstractStoryFragment
                    implements GlobalVars.UserListener, DatabaseLayer.FeedStory {
    RecyclerView rvQuestions;
    HashMap<Integer, ArrayList<Story>> questionDict;
    FeedRecyclerAdapter questionsAdapter;
    ArrayList<Integer> keysList;
    //usercounter exists because some users don't have any stories
    int storylessUsers = 0;
    //if listKind is 0 it's full of random stories, if 1 then followingstories
    int listKind = 0;
    ArrayList<String> usersToFetch;
    HashMap<String, User> userDict;
    ArrayList<BasicStory> storiesToInstantiate;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        int darkColor = ((GlobalVars)getActivity().getApplication()).getUser().getColorHexDark(getContext());
      //  ((GradientDrawable)getActivity().getResources().getDrawable(R.drawable.custom_button))
        //        .setColor(darkColor);

        super.onCreate(savedInstanceState);
        fragmentFlag = 1;
        ((GlobalVars)getActivity().getApplication()).abstractStoryFragment = this;
        ((MainActivity)getActivity()).abstractStoryFragment = this;
        if (((MainActivity) getActivity()).feedDict != null)
            questionDict = ((MainActivity) getActivity()).feedDict;
        else {
            questionDict = new HashMap<>();
            ((MainActivity) getActivity()).feedDict = questionDict;
        }
        userDict = new HashMap<>();
        usersToFetch = new ArrayList<>();
        storiesToInstantiate = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                                    Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_feed, container, false);
        // Setting RecyclerView
        rvQuestions = (RecyclerView) v.findViewById(R.id.rvQuestions);
        rvQuestions.setHasFixedSize(true); //be careful about this line if lines can be deleted
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        rvQuestions.setLayoutManager(llm);
        if (questionDict.isEmpty()) keysList = new ArrayList<>();
        else keysList = new ArrayList<>(questionDict.keySet());
        questionsAdapter = new FeedRecyclerAdapter(getActivity(), questionDict, this, keysList);
        rvQuestions.setAdapter(questionsAdapter);
        storylessUsers = 0;
        updating = 1;
        DatabaseLayer.getFeedStories(this, ((GlobalVars)getActivity().getApplication()).getUser());
        ((MainActivity)getActivity()).getSupportActionBar().setTitle("Feed");
        return v;
    }

    @Override
    public void followingStoryQuestionAdded(ArrayList<BasicStory> stories) {
        if (listKind != 1) {
            storylessUsers = 0;
            listKind = 1;
            updating = 2;
            storiesToInstantiate.clear();
            usersToFetch.clear();
        }
        storyQuestionAdded(stories, listKind);
    }

    @Override
    public void generalStoryQuestionAdded(ArrayList<BasicStory> stories) {
        listKind = 0;
        storiesToInstantiate.clear();
        usersToFetch.clear();
        updating = 1;
        storyQuestionAdded(stories, listKind);
    }

    public void storyQuestionAdded(ArrayList<BasicStory> storyList, int localListKind) {
        if (listKind == 1 && localListKind == 0) return;
        if (storyList.isEmpty()) storylessUsers ++;
        else {
            for (BasicStory story : storyList) {
                if (!usersToFetch.contains(story.getUserId())) usersToFetch.add(story.getUserId());
            }
        }
        storiesToInstantiate.addAll(storyList);
        if (storylessUsers + usersToFetch.size() ==
                ((GlobalVars)getActivity().getApplication()).getUser().getFollowing().size() || listKind == 0)
            DatabaseLayer.getFeedUsers(this, usersToFetch, localListKind); //clears userstofetch in method
    }

    public void userFetched(User user, int localListKind) {
        if (listKind == 1 && localListKind == 0) return;
        if (updating == 1 || updating == 2) {
            CacheManager.dontDeleteList.clear();
            questionDict.clear();
            userDict.clear();
            keysList.clear();
            updating = 0;
        }
        usersToFetch.remove(user.getUid());
        userDict.put(user.getUid(), user);
        if (userDict.size() + storylessUsers ==
                ((GlobalVars)getActivity().getApplication()).getUser().getFollowing().size() || (listKind == 0 && usersToFetch.size() == 0))
            fillQuestionDict();
    }

    public void fillQuestionDict() {
        for (BasicStory bsStory : storiesToInstantiate) {
            Story story = new Story(bsStory, userDict.get(bsStory.getUserId()), null);
            Integer theme = story.getTheme();
            ArrayList<Story> internalStoryList = questionDict.get(theme);
            if (internalStoryList == null) internalStoryList = new ArrayList<>();
            internalStoryList.add(story);
            if (!keysList.contains(theme)) keysList.add(theme);
            questionDict.put(theme, internalStoryList);
        }
        questionsAdapter.notifyDataSetChanged();
        storiesToInstantiate.clear();
        usersToFetch.clear();
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
        ((MainActivity)getActivity()).changeTab(1);
    }

    @Override
    public void onSubmitText(String text, int theme) {
        Story story = new Story();
        story.description = text;
        story.theme = theme;
        story.creationDate = new Date();
        story.setPoster(((GlobalVars)getActivity().getApplication()).getUser());
        // submit new story to server
        DatabaseLayer.submitStory(story);
        //questionsAdapter.notifyItemChanged(keysList.indexOf(story.getTheme()));
        ((MainActivity)getActivity()).changeTab(1); //see new post
        composeTextFragment.dismiss();
    }

    @Override
    public void deleteStory(Story story) {
        //TODO this if we decide you can see yourself in feed
    }

    //called when the questions are downloaded
    @Override
    public void onQuestionsDownloaded() {
        if (questionsAdapter != null) questionsAdapter.notifyDataSetChanged();
    }

    @Override
    public void onUserChanged(User user) {
        if (updating != 2) {
            updating = 2;
            DatabaseLayer.getFeedStories(this, user);
        }
    }

    @Override
    public void onVideoDownloaded(final Story story) {
        Handler handler = new Handler();
        final Runnable r = new Runnable() {
            public void run() {
                questionsAdapter.notifyItemChanged(keysList.indexOf(story.getTheme()));
            }
        };
        handler.post(r);
    }
}
