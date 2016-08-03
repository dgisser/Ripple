package com.facebook.peepingtom.Models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import org.parceler.Parcel;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by dgisser on 7/12/16.
 */
@Parcel
@IgnoreExtraProperties
public class BasicStory implements Serializable {

    public String getStoryId() {
        return storyId;
    }

    public String getDescription() {
        return description;
    }

    public String getImageURL() {
        return imageURL;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public int getTheme() {
        return theme;
    }

    public String getUserId() { return userId; }

    public String getVideoURL() { return videoURL; }

    public String storyId;
    public String description;
    public String imageURL;
    public String videoURL;
    public Date creationDate;//TODO change dates to longs
    public int theme; //theme is int because associated with theme number
    public String userId;

    public BasicStory() {}

    public BasicStory(String storyId, String description, String imageURL, String videoURL,
                      Date creationDate, int theme, String userId) {
        this.storyId = storyId;
        this.description = description;
        this.imageURL = imageURL;
        this.videoURL = videoURL;
        this.creationDate = creationDate;
        this.theme = theme;
        this.userId = userId;
    }

    public BasicStory(BasicStory story) {
        this.storyId = story.storyId;
        this.description = story.description;
        this.imageURL = story.imageURL;
        this.creationDate = story.creationDate;
        this.theme = story.theme;
        this.userId = story.userId;
        this.videoURL = story.videoURL;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("storyId", storyId);
        result.put("description", description);
        result.put("imageURL", imageURL);
        result.put("videoURL", videoURL);
        result.put("creationDate", creationDate.getTime());
        result.put("theme", theme);
        result.put("userId", userId);
        return result;
    }
}
