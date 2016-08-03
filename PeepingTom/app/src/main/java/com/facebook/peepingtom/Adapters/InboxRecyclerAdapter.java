package com.facebook.peepingtom.Adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.facebook.peepingtom.Activities.MessageActivity;
import com.facebook.peepingtom.Fragments.InboxFragment;
import com.facebook.peepingtom.GlobalVars;
import com.facebook.peepingtom.Models.ChatDescription;
import com.facebook.peepingtom.Models.User;
import com.facebook.peepingtom.R;
import com.facebook.peepingtom.UI.TimeFormatter;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by dgisser on 7/22/16.
 */
public class InboxRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public ArrayList<ChatDescription> descriptionList;
    public HashMap<String, User> userMap;
    InboxFragment inboxFragment;
    Context context;

    public InboxRecyclerAdapter(InboxFragment inboxFragment) {
        this.inboxFragment = inboxFragment;
    }

    // VIEWHOLDER CLASSES
    public static class InboxViewHolder extends RecyclerView.ViewHolder {

        TextView tvOtherUserName;
        TextView tvLastMessage;
        TextView tvTime;
        CircleImageView ivProfile;

        public InboxViewHolder(View itemView) {
            super(itemView);
            tvOtherUserName = (TextView) itemView.findViewById(R.id.tvOtherUserName);
            tvLastMessage = (TextView) itemView.findViewById(R.id.tvLastMessage);
            tvTime = (TextView) itemView.findViewById(R.id.tvTime);
            ivProfile = (CircleImageView) itemView.findViewById(R.id.ivProfile);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_chat, parent, false);
        return new InboxViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        //TODO set last message time
        final ChatDescription description = descriptionList.get(position);
        User mainUser = ((GlobalVars)(inboxFragment.getActivity()).getApplication()).getUser();
        final User otherUser;
        if (Objects.equals(mainUser.getUid(), description.getUserid1()))
            otherUser = userMap.get(description.getUserid2());
        else otherUser = userMap.get(description.getUserid1());
        final InboxViewHolder viewHolder = (InboxViewHolder) holder;
        Glide.with(context).load(otherUser.getProfileUrl()).asBitmap().centerCrop().dontAnimate().
                into(new BitmapImageViewTarget(viewHolder.ivProfile) {
                    @Override
                    protected void setResource(Bitmap resource) {
                        RoundedBitmapDrawable circularBitmapDrawable =
                                RoundedBitmapDrawableFactory.create(context.getResources(), resource);
                        circularBitmapDrawable.setCircular(true);
                        viewHolder.ivProfile.setImageDrawable(circularBitmapDrawable);
                        viewHolder.ivProfile.setBorderColor(otherUser.getColorHexDark(context));
                    }
                });
        String lastMessage;
        if (description.lastMessage != null) lastMessage = description.lastMessage.getText();
        else lastMessage = "";
        viewHolder.tvLastMessage.setText(lastMessage);
        viewHolder.tvLastMessage.setEllipsize(TextUtils.TruncateAt.END);
        viewHolder.tvLastMessage.setMaxLines(2);
        viewHolder.tvOtherUserName.setText(otherUser.getFirstName());
        Long seconds = description.getLastMessage().getSentTime().getTime();
        viewHolder.tvTime.setText(TimeFormatter.getTimeDifference(seconds));

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(view.getContext(), MessageActivity.class);
                i.putExtra("otherUser", Parcels.wrap(otherUser));
                i.putExtra("description", Parcels.wrap(description));
                inboxFragment.startActivityForResult(i, InboxFragment.CHANGEDESCRIPTION);
            }
        });
        if (position == 0) holder.itemView.setPadding(0, 200, 0, 0); //not the right way to do this
    }

    @Override
    public int getItemCount() {
        return descriptionList.size();
    }
}
