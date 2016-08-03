package com.facebook.peepingtom.Adapters;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.facebook.peepingtom.Activities.MainActivity;
import com.facebook.peepingtom.Activities.ProfileActivity;
import com.facebook.peepingtom.Database.DatabaseLayer;
import com.facebook.peepingtom.Fragments.SearchFragment;
import com.facebook.peepingtom.Models.User;
import com.facebook.peepingtom.R;
import com.facebook.peepingtom.UI.MyAttributesView;

import org.parceler.Parcels;

import java.util.ArrayList;

/**
 * Created by sophiehouser on 7/20/16.
 */
public class SearchRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public ArrayList<User> userList;
    private final int MYATTRIBUTES = 0;
    private final int USER = 1;
    private  final int NORESULTS = 2;
    private SearchFragment searchFragment;
    Context context;

    public SearchRecyclerAdapter(ArrayList<User> userList, SearchFragment searchFragment) {
        this.userList = userList;
        this.searchFragment = searchFragment;
    }

    // VIEWHOLDER CLASSES
    public static class SearchViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProfile;
        TextView tvName;
        GridLayout glAttributes;

        public SearchViewHolder(View itemView) {
            super(itemView);
            ivProfile = (ImageView) itemView.findViewById(R.id.ivProfile);
            tvName = (TextView) itemView.findViewById(R.id.tvName);
            glAttributes = (GridLayout) itemView.findViewById(R.id.glAttributes);
        }
    }

    public static class MyAttributesViewHolder extends RecyclerView.ViewHolder {
        MyAttributesView myAttributesView = new MyAttributesView(false);
        Button btnSearch;

        public MyAttributesViewHolder(View itemView){
            super(itemView);
            myAttributesView.setUpFromView(itemView);
            btnSearch = (Button) itemView.findViewById(R.id.btnSearch);
            btnSearch.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) return MYATTRIBUTES;
        else return USER;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        if (viewType == MYATTRIBUTES) {
            View myAttributesView = LayoutInflater.from(context).inflate(R.layout.item_my_attributes, parent, false);
            return new MyAttributesViewHolder(myAttributesView);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_search, parent, false);
            return new SearchViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == MYATTRIBUTES) {
            final MyAttributesViewHolder myAttributesViewHolder = (MyAttributesViewHolder) holder;
            //configure the view in myAttributes view
            myAttributesViewHolder.myAttributesView.configureMyAttributesView(searchFragment);
            ///user myAttributes view
            // THIS IS COMING UP NULL. WHY? BTN SHOULD BE MADE ALREADY IN VIEWHOLDER
            myAttributesViewHolder.btnSearch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    searchFragment.updating = 1;
                    DatabaseLayer.getSearchUsers(searchFragment,
                            myAttributesViewHolder.myAttributesView.regionChoice,
                            myAttributesViewHolder.myAttributesView.densityChoice,
                            myAttributesViewHolder.myAttributesView.genderChoice,
                            myAttributesViewHolder.myAttributesView.orientationChoice,
                            myAttributesViewHolder.myAttributesView.religionChoice);
                }
            });
        } else {
            final User user = userList.get(position - 1);
            SearchViewHolder viewHolder = (SearchViewHolder) holder;
            Glide.with(context).load(user.getProfileUrl()).override(200, 200).centerCrop().into(viewHolder.ivProfile);
            setAttributes(viewHolder, user);

            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = new Intent(view.getContext(), ProfileActivity.class);
                    i.putExtra("user", Parcels.wrap(user));
                    i.putExtra("activityTag", 1);
                    searchFragment.getActivity().startActivityForResult(i, MainActivity.SENDMESSAGE);
                }
            });
        }
        if (position == 0) holder.itemView.setPadding(0, 200, 0, 0); //not the right way to do this
    }

    public void setAttributes(SearchViewHolder viewHolder, User user) {
        ArrayList<String> attributesList = new ArrayList<>();
        Glide.with(context).load(user.getProfileUrl()).override(200, 200).centerCrop().into(viewHolder.ivProfile);
        if (user.getFirstName() != null && user.getLastName() != null) {
            viewHolder.tvName.setText(user.getFirstName());
        }

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
            if(!attributesList.contains(user.getSexualOrientationString()))
                attributesList.add(user.getSexualOrientationString());
        }

        Resources r = searchFragment.getContext().getResources();
        int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, r.getDisplayMetrics());
        //viewHolder.glAttributes.setLayoutParams(params);

        for (int i = 0; i < attributesList.size(); i++) {
            if(i == 0){
                viewHolder.glAttributes.removeAllViews();
            }
            View tv = getAttributeTextView(attributesList.get(i), context,
                    user.getColorHexDark(searchFragment.getContext()));
            viewHolder.glAttributes.addView(tv);
        }
    }


    public View getAttributeTextView(String text, Context context, int color) {
        // Convert the view as a TextView widget
        TextView tv = new TextView(context);
        // Set the TextView background color

        tv.setBackground(context.getResources().getDrawable(R.drawable.attribute_pills));
        tv.setBackground(context.getDrawable(R.drawable.attribute_pills));
        ((GradientDrawable)tv.getBackground()).setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
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

    @Override
    public int getItemCount() {
        return userList.size() + 1;
    }
}
