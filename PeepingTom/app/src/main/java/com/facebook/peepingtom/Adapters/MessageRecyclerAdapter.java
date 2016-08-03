package com.facebook.peepingtom.Adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.facebook.peepingtom.Activities.MessageActivity;
import com.facebook.peepingtom.GlobalVars;
import com.facebook.peepingtom.Models.Message;
import com.facebook.peepingtom.Models.User;
import com.facebook.peepingtom.R;
import com.facebook.peepingtom.UI.TimeFormatter;

import java.util.ArrayList;
import java.util.Objects;

/**
 * Created by dgisser on 7/23/16.
 */
public class MessageRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public ArrayList<Message> messageList;
    User mainUser;
    User otherUser;
    Context context;
    public MessageActivity messageActivity;
    private final int LEFTMESSAGEITEM = 0;
    private final int RIGHTMESSAGEITEM = 1;

    public MessageRecyclerAdapter(MessageActivity messageActivity, User otherUser) {
        this.messageActivity = messageActivity;
        mainUser = ((GlobalVars) messageActivity.getApplication()).getUser();
        this.otherUser = otherUser;
    }

    // VIEWHOLDER CLASSES
    public static class MessageViewHolder extends RecyclerView.ViewHolder {

        TextView tvTime;
        TextView tvMessage;
        ImageView ivProfile;

        public MessageViewHolder(View itemView) {
            super(itemView);
            tvTime = (TextView) itemView.findViewById(R.id.tvTime);
            tvMessage = (TextView) itemView.findViewById(R.id.tvMessage);
            ivProfile = (ImageView) itemView.findViewById(R.id.ivProfile);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (Objects.equals(messageList.get(position).getUserId(), mainUser.getUid())) return RIGHTMESSAGEITEM;
        else return LEFTMESSAGEITEM;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        if (viewType == LEFTMESSAGEITEM) {
            View messsageView = LayoutInflater.from(context).inflate(R.layout.item_message_left, parent, false);
            return new MessageViewHolder(messsageView);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_message_right, parent, false);
            return new MessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Message message = messageList.get(position);
        User user;
        final MessageViewHolder messageHolder = (MessageViewHolder) holder;
        int color;
        if (Objects.equals(messageList.get(position).getUserId(), mainUser.getUid())) {
            user = mainUser;
            color = user.getColorHexDark(context);
            messageHolder.tvMessage.setTextColor(Color.WHITE);
        } else {
            user = otherUser;
            if(mainUser.getColorHex(context) == otherUser.getColorHex(context)){
                color = context.getResources().getColor(R.color.colorPrimary);
                messageHolder.tvMessage.setTextColor(context.getResources().getColor(R.color.colorPrimaryDark));
            }else{
                color = user.getColorHexDark(context);
                messageHolder.tvMessage.setTextColor(Color.WHITE);
            }
        }
        Glide.with(context).load(user.getProfileUrl()).asBitmap().centerCrop().
                            into(new BitmapImageViewTarget(messageHolder.ivProfile) {
            @Override
            protected void setResource(Bitmap resource) {
                RoundedBitmapDrawable circularBitmapDrawable =
                        RoundedBitmapDrawableFactory.create(context.getResources(), resource);
                circularBitmapDrawable.setCircular(true);
                messageHolder.ivProfile.setImageDrawable(circularBitmapDrawable);
            }
        });
        messageHolder.tvMessage.setText(message.getText());
        Long seconds = message.getSentTime().getTime();
        messageHolder.tvTime.setText(TimeFormatter.getTimeDifference(seconds));
        ((GradientDrawable)messageHolder.tvMessage.getBackground()).setColorFilter(color, PorterDuff.Mode.SRC_IN);// TODO set this up with the twitter time library
        if (position == 0) messageHolder.itemView.setPadding(0,220,0,0);
    }

    @Override
    public int getItemCount() {
        if (messageList == null) return 0;
        return messageList.size();
    }
}
