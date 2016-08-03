package com.facebook.peepingtom.UI;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.facebook.peepingtom.Activities.MainActivity;
import com.facebook.peepingtom.Database.DatabaseLayer;
import com.facebook.peepingtom.Fragments.AbstractStoryFragment;
import com.facebook.peepingtom.GlobalVars;
import com.facebook.peepingtom.Models.Story;
import com.facebook.peepingtom.Models.User;
import com.facebook.peepingtom.R;

import java.util.Objects;

/**
 * Created by sophiehouser on 7/11/16.
 */
public class StoryView {

    public int viewPosition;
    public TextView tvQuestion = null;
    public ImageView ivImage = null;
    public VideoView videoView = null;
    public TextView tvText = null;
    public ImageView ivProfile = null;
    public Button btnDelete;
    public Button btnReport;
    public TextView tvCreationDate;
    public TextView tvAuthor;
    public CardView cardView;
    public View gradientView;

    /* sets up an the story view given an item view by the adapter*/
    public void setUpFromView(View view){
        tvQuestion = (TextView) view.findViewById(R.id.tvQuestion);
        ivImage = (ImageView) view.findViewById(R.id.ivMedia);
        videoView = (VideoView) view.findViewById(R.id.vvVideo);
        tvText = (TextView) view.findViewById(R.id.tvText);
        ivProfile = (ImageView) view.findViewById(R.id.ivProfile);
        btnDelete = (Button) view.findViewById(R.id.btnDelete);
        btnReport = (Button) view.findViewById(R.id.btnReport);
        tvCreationDate = (TextView) view.findViewById(R.id.tvCreationDate);
        tvAuthor = (TextView) view.findViewById(R.id.tvAuthor);
        cardView = (CardView) view.findViewById(R.id.card_view);
        gradientView = view.findViewById(R.id.gradient);
    }

    public void configureStoryView(final AbstractStoryFragment abstractStoryFragment, final Story story) {
        videoView.setBackgroundResource(0);
        User user = story.getPoster();
        if (Objects.equals(user.getUid(), ((GlobalVars) abstractStoryFragment.getActivity().getApplication()).getUser().getUid()))
            story.setPoster(((GlobalVars) abstractStoryFragment.getActivity().getApplication()).getUser());
        int profileColor = story.getPoster().getColorHex(abstractStoryFragment.getContext());
        int darkColor = story.getPoster().getColorHexDark(abstractStoryFragment.getContext());

        //((GradientDrawable)btnReport.getBackground()).setColor(darkColor);
        //((GradientDrawable)btnDelete.getBackground()).setColor(darkColor);
        cardView.setCardBackgroundColor(profileColor);
        //((Drawable)cardView.getBackground()).setColor(profileColor);
        //cardView.getBackground().setColorFilter(profileColor, PorterDuff.Mode.SRC_OVER);
        btnReport.getBackground().setColorFilter(darkColor, PorterDuff.Mode.SRC_OVER);
        btnDelete.getBackground().setColorFilter(darkColor, PorterDuff.Mode.SRC_OVER);

        tvAuthor.setText(story.getPoster().getFirstName());
        String createdAt = TimeFormatter.getTimeDifference(story.getCreationDate().getTime());
        tvCreationDate.setText(createdAt);

        if (story.getUserId().equals(((GlobalVars)abstractStoryFragment.getActivity()
                .getApplication()).getUser().getUid()) && abstractStoryFragment.fragmentFlag == 1) {
            btnDelete.setVisibility(View.GONE);
            btnReport.setVisibility(View.GONE);
        } else if (!story.getUserId().equals(((GlobalVars)abstractStoryFragment.getActivity()
                .getApplication()).getUser().getUid())) {
            btnDelete.setVisibility(View.GONE);
            btnReport.setVisibility(View.VISIBLE);
            btnReport.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(abstractStoryFragment.getActivity(), "Story has been reported", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            btnDelete.setVisibility(View.GONE);
            btnReport.setVisibility(View.GONE);
            if(abstractStoryFragment.fragmentFlag == 2) {
                btnDelete.setVisibility(View.VISIBLE);
                btnDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        abstractStoryFragment.deleteStory(story);
                    }
                });
            }
        }

        if (abstractStoryFragment.fragmentFlag == 2) {
            tvQuestion.setText(MainActivity.questionsList.get(story.getTheme()));
            tvQuestion.setVisibility(View.VISIBLE);
        } else tvQuestion.setVisibility(View.GONE);

        // adds user profile image to story if on feed fragment
        if (story.getPoster() != null) {
            //TODO make user pictures work
            Glide.with(abstractStoryFragment.getContext()).load(story.getPoster().getProfileUrl()).centerCrop().into(ivProfile);
            ivProfile.setVisibility(View.VISIBLE);
        }

        // if the story has an image answer
        if (story.getImageURL() != null && !story.getImageURL().isEmpty()){
        //if (story.media != null) {
            tvAuthor.setTextColor(Color.WHITE);
            tvCreationDate.setTextColor(Color.WHITE);
            tvQuestion.setTextColor(Color.WHITE);
            tvText.setVisibility(View.GONE);
            videoView.setVisibility(View.GONE);
            Glide.with(abstractStoryFragment.getContext()).load(story.getImageURL()).centerCrop().into(ivImage);///cropping
            gradientView.setVisibility(View.VISIBLE);
            ivImage.setVisibility(View.VISIBLE);
        }
        // if the story has a video
        else if (story.getVideoURL() != null && !story.getVideoURL().isEmpty()) {
            tvText.setVisibility(View.GONE);
            ivImage.setVisibility(View.GONE);
            tvAuthor.setTextColor(Color.WHITE);
            tvCreationDate.setTextColor(Color.WHITE);
            tvQuestion.setTextColor(Color.WHITE);
            gradientView.setVisibility(View.VISIBLE);
            if (story.getVideoUri() == null) DatabaseLayer.downloadVideoStory(story, abstractStoryFragment);
            else {
                videoView.setVisibility(View.VISIBLE);
                MediaController mc = new MediaController(videoView.getContext());
                mc.setAnchorView(videoView);
                mc.setMediaPlayer(videoView);
                videoView.setMediaController(mc);
                videoView.setVideoURI(story.getVideoUri());
                videoView.requestFocus();
                videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                    @Override
                    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
                        Log.d("video", "setOnErrorListener ");
                        return true;
                    }
                });
            }
        }
        // if the story has a text description
        else if (story.getDescription() != null && !story.getDescription().isEmpty()) {
            tvText.setTextColor(Color.DKGRAY);
            tvAuthor.setTextColor(Color.DKGRAY);
            tvCreationDate.setTextColor(Color.DKGRAY);
            tvQuestion.setTextColor(Color.DKGRAY);
            ivImage.setVisibility(View.GONE);
            videoView.setVisibility(View.GONE);
            gradientView.setVisibility(View.GONE);
            // add the text to the story
            tvText.setText("\"" + story.getDescription() + "\"");
            // show the text view
            tvText.setVisibility(View.VISIBLE);
            // hide the buttons
        }
    }
}