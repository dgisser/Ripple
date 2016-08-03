package com.facebook.peepingtom.Models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import org.parceler.Parcel;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by dgisser on 7/21/16.
 */
@Parcel
@IgnoreExtraProperties
public class ChatDescription {

    public String userid1;
    public String userid2;
    public String chatId;
    public Message lastMessage;

    public ChatDescription() {

    }

    public ChatDescription(String userid1, String userid2, String chatId, Message lastMessage) {
        this.userid1 = userid1;
        this.userid2 = userid2;
        this.chatId = chatId;
        this.lastMessage = lastMessage;
    }

    public String getUserid1() { return userid1; }

    public void setUserid1(String userid1) { this.userid1 = userid1; }

    public String getUserid2() { return userid2; }

    public void setUserid2(String userid2) { this.userid2 = userid2; }

    public String getChatId() { return chatId; }

    public void setChatId(String chatId) { this.chatId = chatId; }

    public Message getLastMessage() { return lastMessage; }

    public void setLastMessage (Message lastMessage) {
        this.lastMessage = lastMessage;
    }

    public void setDescriptionFromMap (HashMap<String, Object> obj) {
        userid1 = (String)obj.get("userid1");
        userid2 = (String)obj.get("userid2");
        chatId = (String)obj.get("chatId");
        lastMessage.setMessageFromMap((HashMap<String, Object>) obj.get("lastMessage"));
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("userid1", userid1);
        result.put("userid2", userid2);
        result.put("chatId", chatId);
        result.put("lastMessage", lastMessage.toMap());
        return result;
    }
}
