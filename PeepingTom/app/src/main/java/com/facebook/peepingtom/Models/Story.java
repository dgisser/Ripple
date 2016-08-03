package com.facebook.peepingtom.Models;

import android.graphics.Bitmap;
import android.net.Uri;

import org.parceler.Parcel;

import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

/**
 * Created by dgisser on 7/6/16.
 */
@Parcel
public class Story extends BasicStory {
    User poster;
    Bitmap media;
    Uri videoUri;

    public Story() {}

    public Uri getVideoUri() {
        return videoUri;
    }

    public void setVideoUri(Uri videoUri) {
        this.videoUri = videoUri;
    }

    public void setPoster(User poster) {
        this.poster = poster;
        this.userId = poster.getUid();
    }

    public void setMedia(Bitmap media) {
        this.media = media;
    }

    public User getPoster() {
        return poster;
    }

    public Bitmap getMedia() { return media; }

    public Story(BasicStory bsStory, User user, Bitmap media) {
        this.storyId = bsStory.storyId;
        this.description = bsStory.description;
        this.imageURL = bsStory.imageURL;
        this.videoURL = bsStory.videoURL;
        this.creationDate = bsStory.creationDate;
        this.theme = bsStory.theme;
        this.userId = bsStory.userId;
        this.poster = user;
        this.media = media;
    }

    public Story(String storyId, String description, String imageURL, Date creationDate,
                                                    int theme, User poster, Bitmap media) {
        this.storyId = storyId;
        this.description = description;
        this.imageURL = imageURL;
        this.creationDate = creationDate;
        this.theme = theme;
        this.poster = poster;
        this.media = media;
        if (poster != null) this.userId = poster.getUid();
    }

    public static ArrayList<Story> getStories(){
        ArrayList<Story> results = new ArrayList<>();
        results.add(new Story());
        return results;
    }

    public static int getRandomStoryPrompt(int maxPrompt) {
        // max is exclusive
        return new Random().nextInt(maxPrompt);
    }
}