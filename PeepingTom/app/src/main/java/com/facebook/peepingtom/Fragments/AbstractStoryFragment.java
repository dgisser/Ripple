package com.facebook.peepingtom.Fragments;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;

import com.facebook.peepingtom.Activities.MainActivity;
import com.facebook.peepingtom.Database.DatabaseLayer;
import com.facebook.peepingtom.Models.Story;

/**
 * Created by sophiehouser on 7/8/16.
 */

public abstract class AbstractStoryFragment extends Fragment
        implements ComposeTextFragment.ComposeTextListener,
        MainActivity.QuestionsDownloaded, DatabaseLayer.StoryFragment {
    // new compose text dialog fragment
    ComposeTextFragment composeTextFragment = ComposeTextFragment.newInstance();
    public int fragmentFlag = 0;
    //0 is not updating, 1 is updating
    protected int updating = 0;
    public Uri imagePath;
    public int currentTheme;

    // opens compose text dialog fragment when add text is clicked
    public void onAddTextClicked(int theme) {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        composeTextFragment.show(fm, "fragment_compose_text");
        // setTargetFragment requires a REQUEST_CODE so 0 is an arbitrary int
        Bundle bundle = new Bundle();
        bundle.putInt("theme", theme);
        composeTextFragment.setArguments(bundle);
        composeTextFragment.setTargetFragment(this, 0);
    }

    // when done button pressed in dialogue fragment
    //creates a new story with input text, removes empty one, adds it to the array and notifies the adapter
    public abstract void onSubmitText(String text, int theme);

    public abstract void deleteStory(Story story);

    public Bitmap grabImage() {
        getContext().getContentResolver().notifyChange(imagePath, null);
        ContentResolver cr = getContext().getContentResolver();
        Bitmap bitmap;
        try
        {
            bitmap = android.provider.MediaStore.Images.Media.getBitmap(cr, imagePath);
            ExifInterface exif = new ExifInterface(imagePath.getPath());
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
            int rotation = exifToDegrees(orientation);
            Matrix matrix = new Matrix();
            if (rotation != 0f) matrix.preRotate(rotation);
            return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        }
        catch (Exception e)
        {
            Log.d("profilefragment", "Failed to load", e);
        }
        return null;
    }

    private static int exifToDegrees(int exifOrientation) {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) { return 90; }
        else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {  return 180; }
        else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {  return 270; }
        return 0;
    }

    /*
    public void loadStories() {
        storyAdapter.addAll(Story.getStories());
    }
    */

}
