package com.facebook.peepingtom.UI;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.peepingtom.Activities.MainActivity;
import com.facebook.peepingtom.Database.CacheManager;
import com.facebook.peepingtom.Fragments.AbstractStoryFragment;
import com.facebook.peepingtom.Fragments.ProfileStoriesFragment;
import com.facebook.peepingtom.GlobalVars;
import com.facebook.peepingtom.Models.Story;
import com.facebook.peepingtom.R;

import java.io.File;

/**
 * Created by aespino on 7/20/16.
 */
public class StoryPromptView {
    private static final int CAMERA_PIC_REQUEST = 55;
    private static final int CAMERA_VIDEO_REQUEST = 65;
    public int viewPosition;
    public Button btnImage = null;
    public Button btnText = null;
    public Button btnVideo = null;
    public TextView tvPromptQuestion = null;
    public LinearLayout addHolder;
    private CardView cardView;

    public void onCreate(Story story) {}

    /* sets up an the story view given an item view by the adapter*/
    public void setUpFromView(View view){
        tvPromptQuestion = (TextView) view.findViewById(R.id.tvPromptQuestion);
        btnImage = (Button) view.findViewById(R.id.btnMedia);
        btnText = (Button) view.findViewById(R.id.btnText);
        btnVideo = (Button) view.findViewById(R.id.btnVideo);
        addHolder = (LinearLayout) view.findViewById(R.id.addHolder);
        cardView = (CardView) view.findViewById(R.id.card_view);
    }

    //if theme == -1 then get random theme
    public void configureStoryPromptView(final AbstractStoryFragment abstractStoryFragment, int presetTheme) {
        RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) cardView.getLayoutParams();
        int darkColor = ((GlobalVars)abstractStoryFragment.getActivity().getApplication())
                .getUser().getColorHexDark(abstractStoryFragment.getContext());
        ((GradientDrawable)abstractStoryFragment.getResources().getDrawable(R.drawable.custom_button))
                .setColor(darkColor);

        if (presetTheme == -1) presetTheme = Story.getRandomStoryPrompt(MainActivity.questionsList.size());
        final int theme = presetTheme;
        tvPromptQuestion.setText(MainActivity.questionsList.get(theme));
        //tvPromptQuestion.setTextColor(accentColor);
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) btnImage.getLayoutParams();
        // converts int (10 in this case) to dp
        Resources r = abstractStoryFragment.getContext().getResources();
        int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, r.getDisplayMetrics());
        // makes add buttons horizontal in profile
        if (abstractStoryFragment.fragmentFlag == 2) {
            addHolder.setOrientation(LinearLayout.HORIZONTAL);
            params.leftMargin = px;
            params.rightMargin = px;
            layoutParams.height = CardView.LayoutParams.WRAP_CONTENT;
            layoutParams.setMargins(0, 60,0,60);
            cardView.setLayoutParams(layoutParams);
        } // makes add buttons verticle in profile
        else {
            addHolder.setOrientation(LinearLayout.VERTICAL);
            params.topMargin = px;
            params.bottomMargin = px;
            layoutParams.height = CardView.LayoutParams.MATCH_PARENT;
            cardView.setLayoutParams(layoutParams);
        }

        // add image button click listener opens device camera
        btnImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaListener(abstractStoryFragment, theme, true, CAMERA_PIC_REQUEST, ".jpg");
            }
        });
        // add video button click listener opens device camera
        btnVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaListener(abstractStoryFragment, theme, false, CAMERA_VIDEO_REQUEST, ".mp4");
            }
        });
        // add text button click listener
        btnText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                abstractStoryFragment.onAddTextClicked(theme);
                //only overwrites storyPostition in abstractStory fragment when text is being added
                //instead of every time a new view item is added to the feed/profile
            }
        });

    }

    private void mediaListener(AbstractStoryFragment abstractStoryFragment,
                               int theme, boolean isImage, int REQUEST_CODE, String saveType) {
        abstractStoryFragment.currentTheme = theme;
        if (!getPermissions(abstractStoryFragment.getActivity())) return;
        if (abstractStoryFragment.fragmentFlag == 2)
            ((ProfileStoriesFragment) abstractStoryFragment).storyItemPosition = viewPosition;
        //only overwrites storyPostition in abstractStory fragment when media is being added
        //instead of every time a new view item is added to the feed/profile
        String state = Environment.getExternalStorageState();
        if (!Environment.MEDIA_MOUNTED.equals(state)) {
            Intent cameraIntent;
            if (isImage) cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            else cameraIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            abstractStoryFragment.startActivityForResult(cameraIntent, REQUEST_CODE);
        } else {
            Intent cameraIntent;
            if (isImage) cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            else cameraIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            File media;
            try {
                // place where to store camera taken picture
                media = CacheManager.createTemporaryFile("media", saveType);
                media.delete();
            } catch (Exception e) {
                Log.v("storyview", "Can't create file to take picture! " + e.toString());
                return;
            }

            abstractStoryFragment.imagePath = Uri.fromFile(media);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, abstractStoryFragment.imagePath);
            //cameraIntent.putExtra(MediaStore.Video.Media, abstractStoryFragment.imagePath);
            //start camera intent
            abstractStoryFragment.startActivityForResult(cameraIntent, REQUEST_CODE);
        }
    }

    public static boolean getPermissions(Activity activity) {
        boolean picPermission = ContextCompat.checkSelfPermission(activity,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED;
        boolean readPermission = ContextCompat.checkSelfPermission(activity,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED;
        boolean writePermission = ContextCompat.checkSelfPermission(activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED;

        if (picPermission || readPermission || writePermission) {

            // Should we show an explanation?
            ActivityCompat.requestPermissions(activity,
                    new String[]{
                            Manifest.permission.CAMERA,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
            return false;
        }
        return true;
    }
}
