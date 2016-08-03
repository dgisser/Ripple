package com.facebook.peepingtom.Adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.peepingtom.Activities.MainActivity;
import com.facebook.peepingtom.Activities.ProfileActivity;
import com.facebook.peepingtom.Fragments.AbstractStoryFragment;
import com.facebook.peepingtom.Fragments.ProfileStoriesFragment;
import com.facebook.peepingtom.Models.Story;
import com.facebook.peepingtom.R;
import com.facebook.peepingtom.UI.AttributesView;
import com.facebook.peepingtom.UI.MyAttributesView;
import com.facebook.peepingtom.UI.StoryPromptView;
import com.facebook.peepingtom.UI.StoryView;

import org.parceler.Parcels;

import java.util.ArrayList;

/**
 * Created by sophiehouser on 7/7/16.
 */
public class StoryRecyclerAdapter extends
        RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int MYATTRIBUTES = 0,  STORY = 1, ATTRIBUTES = 2, STORYPROMPT = 3, QUESTIONPROMPT = 4;

    public ArrayList<Story> stories;
    private AbstractStoryFragment abstractStoryFragment;
    public boolean inEditMode = false;
    public int promptCount = 0;
    public String question = "";
    public int position = 0;

    public StoryRecyclerAdapter(ArrayList<Story> storyList,
                                  AbstractStoryFragment abstractStoryFragment) {
        this.abstractStoryFragment = abstractStoryFragment;
        stories = storyList;
    }

    public void invalidate() {
        invalidate();
    }

    // VIEWHOLDER CLASSES
    public static class StoryViewHolder extends RecyclerView.ViewHolder {
        StoryView storyView = new StoryView();

        public StoryViewHolder(View itemView) {
            super(itemView);
            storyView.setUpFromView(itemView);
        }
    }
    public static class StoryPromptViewHolder extends RecyclerView.ViewHolder {
        StoryPromptView storyPromptView = new StoryPromptView();

        public StoryPromptViewHolder(View itemView) {
            super(itemView);
            storyPromptView.setUpFromView(itemView);
        }
    }

    public static class MyAttributesViewHolder extends RecyclerView.ViewHolder {
        MyAttributesView myAttributesView = new MyAttributesView(true);

        public MyAttributesViewHolder(View itemView){
            super(itemView);
            myAttributesView.setUpFromView(itemView);
        }
    }

    public static class AttributesViewHolder extends RecyclerView.ViewHolder{
        AttributesView attributesView = new AttributesView();

        public AttributesViewHolder(View itemView){
            super(itemView);
            attributesView.setUpFromView(itemView);
        }
    }

    public static class QuestionViewHolder extends RecyclerView.ViewHolder {

        public QuestionViewHolder(View itemView) {
            super(itemView);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0 && abstractStoryFragment.fragmentFlag == 2 && inEditMode) return MYATTRIBUTES;
        else if (position == 0 && abstractStoryFragment.fragmentFlag == 2) return ATTRIBUTES;
        else if (abstractStoryFragment.fragmentFlag == 2 && ((ProfileStoriesFragment) abstractStoryFragment).profileUser.getUid().
                equals(((ProfileStoriesFragment) abstractStoryFragment).thisUser.getUid()) && position == 1) {
            return STORYPROMPT;
        } else if (abstractStoryFragment.fragmentFlag == 1 && position == stories.size() + 1) return STORYPROMPT;
        else if (abstractStoryFragment.fragmentFlag == 1 && position == 0) return QUESTIONPROMPT;
        else return STORY;
    }

    //Inflates layout from xml and returns viewholder
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        RecyclerView.ViewHolder viewHolder;
        // Inflate the custom layout - IF PROFILE AND FEED ARE SEPARATE THAT NEED TO CUSTOMIZE HERE AND IN VIEW HOLDER!!
        if (viewType == STORY) {
            View storyView = inflater.inflate(R.layout.item_story, parent, false);
            viewHolder = new StoryViewHolder(storyView);
        } else if (viewType == MYATTRIBUTES) {
            View myAttributesView = inflater.inflate(R.layout.item_my_attributes, parent, false);
            viewHolder = new MyAttributesViewHolder(myAttributesView);
        } else if (viewType == ATTRIBUTES) {
            View attributesView = inflater.inflate(R.layout.item_attributes, parent, false);
            viewHolder = new AttributesViewHolder(attributesView);
        } else if (viewType == QUESTIONPROMPT) {
            View questionView = inflater.inflate(R.layout.item_question_card, parent, false);
            viewHolder = new QuestionViewHolder(questionView);
        } else {//view type is story prompt
            View storyPromptView = inflater.inflate(R.layout.item_story_prompt, parent, false);
            viewHolder = new StoryPromptViewHolder(storyPromptView);
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        //mHolder.itemView.setTag(position);
        //this.position = position;
        //if (abstractStoryFragment.fragmentFlag == 1) {
            //if (position > 0) {
               // ((FeedFragment) abstractStoryFragment).getView().findViewById(R.id.rvStories).setBackgroundColor(Color.WHITE);
            //} else {
               // ((FeedFragment) abstractStoryFragment).getView().findViewById(R.id.rvStories).setBackgroundColor(Color.TRANSPARENT);
            //}
        //}
        if (getItemViewType(position) == STORY) {
            // Get the data model based on position
            StoryViewHolder storyViewHolder = (StoryViewHolder) holder;
            final Story story;
            storyViewHolder.storyView.viewPosition = position;
            if (abstractStoryFragment.fragmentFlag == 2) {
                story = stories.get(position - 1 - promptCount);
                storyViewHolder.storyView.configureStoryView(abstractStoryFragment, story);
                //configure the view in StoryView with position -1 since in profile
            } else {
                // subtracting 1 takes the intial story card into account
                story = stories.get(position - 1);
                storyViewHolder.storyView.configureStoryView(abstractStoryFragment, story);
                //configure the view in StoryView with position since in feed
                storyViewHolder.storyView.ivProfile.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent i = new Intent(view.getContext(), ProfileActivity.class);
                        i.putExtra("user", Parcels.wrap(story.getPoster()));
                        i.putExtra("activityTag", 1);
                        ((MainActivity)abstractStoryFragment.getContext()).
                                startActivityForResult(i, MainActivity.SENDMESSAGE);
                    }
                });
            }
        } else if (getItemViewType(position) == MYATTRIBUTES) {
            MyAttributesViewHolder myAttributesViewHolder = (MyAttributesViewHolder) holder;
            //configure the view in myAttributes view
            myAttributesViewHolder.myAttributesView.configureMyAttributesView(abstractStoryFragment);
            ///user myAttributes view
        } else if (getItemViewType(position) == ATTRIBUTES) {
            AttributesViewHolder attributesViewHolder = (AttributesViewHolder) holder;
            attributesViewHolder.
                    attributesView.configureAttributesView((ProfileStoriesFragment) abstractStoryFragment);
        } else if (getItemViewType(position) != QUESTIONPROMPT) {
            //prompt
            StoryPromptViewHolder storyPromptViewHolder = (StoryPromptViewHolder) holder;
            if (abstractStoryFragment.fragmentFlag == 1)
                storyPromptViewHolder.
                        storyPromptView.configureStoryPromptView(abstractStoryFragment, stories.get(0).getTheme());
            else
                storyPromptViewHolder.
                        storyPromptView.configureStoryPromptView(abstractStoryFragment, -1);
        }
    }

    @Override
    public int getItemCount() {
        if (abstractStoryFragment.fragmentFlag == 2 && ((ProfileStoriesFragment) abstractStoryFragment).profileUser.getUid().
                equals(((ProfileStoriesFragment) abstractStoryFragment).thisUser.getUid())) {
            promptCount = 1;
            return stories.size() + 2;
        } else if (abstractStoryFragment.fragmentFlag == 2) {
            promptCount = 0;
            return stories.size() + 1;
        } else return stories.size() + 2; //add question to the beginning of feed streams and prompt to the end
    }

    public void setData(ArrayList<Story> data) {
        if (stories != data) {
            stories = data;
            notifyDataSetChanged();
        }
    }

    public void setQuestion(String question){
        this.question = question;
    }
}