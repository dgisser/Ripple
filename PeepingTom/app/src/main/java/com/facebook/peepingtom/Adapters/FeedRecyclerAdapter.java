package com.facebook.peepingtom.Adapters;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.peepingtom.Activities.MainActivity;
import com.facebook.peepingtom.Fragments.AbstractStoryFragment;
import com.facebook.peepingtom.GlobalVars;
import com.facebook.peepingtom.Models.Story;
import com.facebook.peepingtom.R;
import com.facebook.peepingtom.Snappy.SnappyLinearLayoutManager;
import com.facebook.peepingtom.Snappy.SnappyRecyclerView;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by sophiehouser on 7/15/16.
 */
public class FeedRecyclerAdapter extends RecyclerView.Adapter<FeedRecyclerAdapter.FeedViewHolder> {
    private final Context mContext;
    private static ArrayList<Integer> keys;
    // keys map to the position of the current story in the carosel in this list
    private static ArrayList<Integer> scrollPositionList;
    private HashMap<Integer, ArrayList<Story>> questionsDict;
    public AbstractStoryFragment abstractStoryFragment;
    //public FeedViewHolder viewHolder;

    public FeedRecyclerAdapter(Context context, HashMap<Integer, ArrayList<Story>> questionsDict,
                          AbstractStoryFragment abstractStoryFragment, ArrayList<Integer> keysList) {
        mContext = context;
        this.abstractStoryFragment = abstractStoryFragment;
        this.questionsDict = questionsDict;
        keys = keysList;
    }

    // contains the question, StoryRecyclerAdapter and View and the nav dots
    public static class FeedViewHolder extends RecyclerView.ViewHolder implements SnappyRecyclerView.ScrollPosition {
        public TextView tvQuestion;
        public CardView cvQuestion;
        private StoryRecyclerAdapter horizontalAdapter;
        public SnappyRecyclerView rvHorizontal;
        public FeedViewHolder(View view, AbstractStoryFragment abstractStoryFragment) {
            super(view);
            Context context = itemView.getContext();
            tvQuestion = (TextView) view.findViewById(R.id.tvQuestion);
            cvQuestion = (CardView) view.findViewById(R.id.cvQuestion);
            // horizontalList is a SnappyRecyclerView which means items snap to a position on scroll
            rvHorizontal = (SnappyRecyclerView) itemView.findViewById(R.id.rvStories);
            rvHorizontal.setLayoutManager(new SnappyLinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
            rvHorizontal.feedViewHolder = this;
            // passing in null for stories list but this is set in onBindViewHolder
            horizontalAdapter = new StoryRecyclerAdapter(new ArrayList<Story>(), abstractStoryFragment);
            rvHorizontal.setAdapter(horizontalAdapter);
        }

        // sets the position of the current story in the list to the key index in scrollPositionList
        // called in the fling method of snappy recycler view
        @Override
        public void scrollPositionListener(int position) {
            if (scrollPositionList != null && !scrollPositionList.isEmpty()) scrollPositionList.set(getAdapterPosition(), position);
        }
    }

    @Override
    public FeedViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_feed, parent, false);
        return new FeedViewHolder(view, abstractStoryFragment);
    }

    @Override
    public void onBindViewHolder(FeedViewHolder holder, final int position) {

        int darkColor = ((GlobalVars) abstractStoryFragment.getActivity().getApplication()).
                getUser().getColorHexDark(abstractStoryFragment.getContext());

        holder.tvQuestion.setText(MainActivity.questionsList.get(keys.get(position)));
        holder.tvQuestion.setTextColor(darkColor);
        holder.horizontalAdapter.setData(questionsDict.get(keys.get(position)));
        if (scrollPositionList == null || (scrollPositionList.size() != keys.size())) {
            scrollPositionList = new ArrayList<>();
            // filling the list with 0s so it's not empty
            for (int i = 0; i < keys.size(); i++) scrollPositionList.add(0);
        }
        if (scrollPositionList != null && !scrollPositionList.isEmpty())
            holder.rvHorizontal.scrollToPosition(scrollPositionList.get(position));
            holder.rvHorizontal.changeBackground(scrollPositionList.get(position));
        if (position == 0) holder.itemView.setPadding(0, 90, 0, 0); //not the right way to do this
    }

    @Override
    public int getItemCount() { return keys.size(); }

}