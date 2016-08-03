package com.facebook.peepingtom.UI;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import com.facebook.peepingtom.Fragments.ProfileStoriesFragment;
import com.facebook.peepingtom.Models.User;
import com.facebook.peepingtom.R;

import java.util.ArrayList;

/**
 * Created by aespino on 7/19/16.
 */
public class AttributesView {
    ImageButton btnEdit;
    GridLayout glAttributes;
    int ProfileColor;
    int darkColor;
    ArrayList<String> attributesList = new ArrayList<>();

    User user;

    public void setUpFromView(View view) {
        btnEdit = (ImageButton) view.findViewById(R.id.btnEdit);
        glAttributes = (GridLayout) view.findViewById(R.id.glAttributes);
    }

    public void configureAttributesView(final ProfileStoriesFragment profileStoriesFragment) {
        final Context context = profileStoriesFragment.getContext();
        user = profileStoriesFragment.profileUser;
        darkColor = user.getColorHexDark(profileStoriesFragment.getContext());

        if (user.equals(profileStoriesFragment.thisUser)) {
            btnEdit.setVisibility(View.VISIBLE);
            btnEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    profileStoriesFragment.storyAdapter.inEditMode = true;
                    profileStoriesFragment.storyAdapter.notifyItemChanged(0);
                }
            });
        } else btnEdit.setVisibility(View.GONE);

        attributesList.clear();

        if (user.age != 0) {
            if(!attributesList.contains(String.format("Age: %s", user.age)))
                attributesList.add(String.format("Age: %s", user.age));
        }
        if (user.gender != User.Gender.NONE) {
            if(!attributesList.contains(user.getGenderString()))
            attributesList.add(user.getGenderString());
        }
        if (user.communityDensity != User.CommunityDensity.NONE) {
            if(!attributesList.contains(user.getCommunityDensityString()))
            attributesList.add(user.getCommunityDensityString());
        }
        if (user.region != User.Region.NONE) {

            if(!attributesList.contains(user.getRegionString()))
            attributesList.add(user.getRegionString());
        }
        if (user.religion != User.Religion.NONE) {
            if(!attributesList.contains(user.getReligionString()))
            attributesList.add(user.getReligionString());
        }
        if (user.sexualOrientation != User.SexualOrientation.NONE) {
            if (!attributesList.contains(user.getSexualOrientationString()))
            attributesList.add(user.getSexualOrientationString());
        }
        for (int i = 0; i < attributesList.size(); i++) {
            if (i == 0) glAttributes.removeAllViews();
            glAttributes.addView(getTextView(attributesList.get(i), context));
        }
    }

    public View getTextView(String text, Context context) {
        // Convert the view as a TextView widget
        TextView tv = new TextView(context);
        // Set the TextView background color

        tv.setBackground(context.getResources().getDrawable(R.drawable.attribute_pills));
        tv.setBackground(context.getDrawable(R.drawable.attribute_pills));
        ((GradientDrawable)tv.getBackground()).setColorFilter(darkColor, PorterDuff.Mode.SRC_IN);

        //tv.setBackgroundTintList(accentColor);
        tv.setTextColor(Color.WHITE);

        // Set the layout parameters for TextView widget
        tv.setGravity(Gravity.CENTER);

        // Set the TextView text font family and text size
        tv.setTypeface(Typeface.SANS_SERIF, Typeface.BOLD);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);

        // Set the TextView text (GridView item text)
        tv.setText(text);
        // Return the TextView widget as GridView item
        return tv;
    }
}
