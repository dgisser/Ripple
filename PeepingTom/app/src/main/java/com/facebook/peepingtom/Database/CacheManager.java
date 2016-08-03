package com.facebook.peepingtom.Database;

import android.app.Activity;
import android.content.Context;
import android.os.Environment;

import com.facebook.peepingtom.Models.Story;
import com.facebook.peepingtom.UI.StoryPromptView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by dgisser on 7/29/16.
 */
public class CacheManager {

    private static final long MAX_SIZE = 78643200L; // 5MB
    public static ArrayList<String> dontDeleteList = new ArrayList<>();

    public static File createTemporaryFile (String part, String ext) throws IOException {
        File tempDir = Environment.getExternalStorageDirectory();
        tempDir = new File(tempDir.getAbsolutePath()+"/.temp/");
        File[] contents = tempDir.listFiles();
        long size = 0;
        if (contents != null && contents.length != 0) size = getDirSize(tempDir);
        if (size > MAX_SIZE) cleanDir(tempDir);
        if (!tempDir.exists()) tempDir.mkdirs();
        return File.createTempFile(part, ext, tempDir);
    }

    public static File createCacheFile (Activity activity, String part, String ext) throws IOException {
        if (!StoryPromptView.getPermissions(activity)) return null;
        File tempDir = Environment.getExternalStorageDirectory();
        tempDir = new File(tempDir.getAbsolutePath() + "/.temp/");
        long size = 0;
        File[] contents = tempDir.listFiles();
        if (contents != null && contents.length != 0) size = getDirSize(tempDir);
        if (size > MAX_SIZE) cleanDir(tempDir);
        if (!tempDir.exists()) tempDir.mkdirs();
        tempDir = new File(tempDir.getPath() + "/" + part + ext);
        tempDir.createNewFile();
        return tempDir;
    }

    public static boolean checkForVideoAndUpdate(Story story, Context context) {
        if (!CacheManager.dontDeleteList.contains(story.getStoryId()))
            CacheManager.dontDeleteList.add(story.getStoryId());
        if (story.getVideoURL() != null) {
            File file = null;
            String state = Environment.getExternalStorageState();
            if (Environment.MEDIA_MOUNTED.equals(state)) {
                file = Environment.getExternalStorageDirectory();
                file = new File(file.getAbsolutePath() + "/.temp/" + story.getStoryId() + ".mp4");
            }
            if (file == null || !file.exists()) {
                File tempFile = context.getCacheDir();
                file = new File(tempFile.getAbsolutePath() + "/" + story.getStoryId() + ".mp4");
            }
            if (file.exists()) {
                story.setVideoUri(android.net.Uri.parse(file.toURI().toString()));
                return true;
            }
        }
        return false;
    }

    private static void cleanDir(File dir) {
        File[] files = dir.listFiles();
        if (files == null || files.length == 0) return;
        long bytesDeleted = 0;
        for (File file : files) {
            bytesDeleted += file.length();
            boolean shouldContinue = false;
            for (String storyid : dontDeleteList) {
                if (shouldContinue) break;
                if (file.toString().contains(storyid)) shouldContinue = true;
            }
            if (shouldContinue) continue;
            file.delete();
        }
    }

    private static long getDirSize(File dir) {

        long size = 0;
        File[] files = dir.listFiles();

        for (File file : files) {
            if (file.isFile()) {
                size += file.length();
            }
        }

        return size;
    }
}
