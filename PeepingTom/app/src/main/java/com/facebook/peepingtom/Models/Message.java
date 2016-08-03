package com.facebook.peepingtom.Models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import org.parceler.Parcel;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by dgisser on 7/21/16.
 */
@Parcel
@IgnoreExtraProperties
public class Message {

    public String userId;
    public String text;
    public String messageId;
    public Date sentTime;

    public Message() {

    }

    public Message(String userId, String text, String messageId, Date sentTime) {
        this.userId = userId;
        this.text = text;
        this.messageId = messageId;
        this.sentTime = sentTime;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Date getSentTime() {
        return sentTime;
    }

    public void setSentTime(Date sent) {
        this.sentTime = sentTime;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getMessageId() { return messageId; }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public void setMessageFromMap (HashMap<String, Object> obj) {
        userId = (String)obj.get("userId");
        text = (String)obj.get("text");
        messageId = (String)obj.get("messageId");
        sentTime = new Date((long) obj.get("sentTime"));
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("userId", userId);
        result.put("text", text);
        result.put("messageId", messageId);
        result.put("sentTime", sentTime.getTime());
        return result;
    }
}
