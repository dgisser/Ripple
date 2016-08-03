package com.facebook.peepingtom.Database;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Log;

import com.facebook.peepingtom.Activities.AccountActivity;
import com.facebook.peepingtom.Activities.MainActivity;
import com.facebook.peepingtom.Activities.MessageActivity;
import com.facebook.peepingtom.Fragments.AbstractFriendsFragment;
import com.facebook.peepingtom.Fragments.AbstractStoryFragment;
import com.facebook.peepingtom.Fragments.FeedFragment;
import com.facebook.peepingtom.Fragments.InboxFragment;
import com.facebook.peepingtom.Fragments.ProfileStoriesFragment;
import com.facebook.peepingtom.Fragments.SearchFragment;
import com.facebook.peepingtom.GlobalVars;
import com.facebook.peepingtom.Models.BasicStory;
import com.facebook.peepingtom.Models.ChatDescription;
import com.facebook.peepingtom.Models.Message;
import com.facebook.peepingtom.Models.Story;
import com.facebook.peepingtom.Models.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Created by dgisser on 7/7/16.
 */
public class DatabaseLayer {

    public interface QuestionsDownloaded {
        void onQuestionsDownloaded(ArrayList<String> questions);
    }

    public interface MainUser {
        void mainUser(User user);
    }

    public interface UserStory {
        void storyAdded(Story story, ChildEventListener childEventListener,
                        DatabaseReference databaseReference);
        void storyRemoved(Story story);
    }

    public interface FeedStory {
        void followingStoryQuestionAdded(ArrayList<BasicStory> stories);
        void generalStoryQuestionAdded(ArrayList<BasicStory> stories);
        void userFetched(User user, int localListKind);
    }

    public interface SearchUsers {
        void onUsersFetched(ArrayList<User> userList);
    }

    public interface InboxChats {
        void onUserFetched(User user);
        void onChatIdFetched(String descriptionId, ChildEventListener childEventListener,
                             DatabaseReference databaseReference);
        void onChatFetched(ChatDescription description, ValueEventListener valueEventListener);
        void onUserChatsFetched(ArrayList<String> chatlist);
    }

    public interface Messages {
        void onMessageFetched(Message message);
    }

    public interface FollowUsers {
        void onFollowUsersFetched(User user);
    }

    public interface StoryFragment {
        void onVideoDownloaded(Story story);
    }

    //this must pass on a reference to the listener so we can get rid of it
    public static void getUserStories(final ProfileStoriesFragment profileStoriesFragment, final User user) {
        final DatabaseReference myRef = FirebaseDatabase.getInstance().getReference()
                                             .child("userStories").child(user.getUid());
        Query recentPostsQuery = myRef.limitToLast(30);
        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                HashMap<String, Object> obj = (HashMap<String, Object>) dataSnapshot.getValue();
                BasicStory bsStory = new BasicStory(((String) obj.get("storyId")), ((String) obj.get("description")),
                        ((String) obj.get("imageURL")), ((String) obj.get("videoURL")),
                        new Date((Long)obj.get("creationDate")),
                        (((Long) obj.get("theme")).intValue()), ((String) obj.get("userId")));
                //turn basicstory into story
                Story story = new Story(bsStory, user, null);
                CacheManager.checkForVideoAndUpdate(story, profileStoriesFragment.getContext());
                profileStoriesFragment.storyAdded(story, this, myRef);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                HashMap<String, Object> obj = (HashMap<String, Object>) dataSnapshot.getValue();
                BasicStory bsStory = new BasicStory(((String) obj.get("storyId")), ((String) obj.get("description")),
                        ((String) obj.get("imageURL")), ((String) obj.get("videoURL")),
                        new Date((Long)obj.get("creationDate")),
                        (((Long) obj.get("theme")).intValue()), ((String) obj.get("userId")));
                //turn basicstory into story
                Story story = new Story(bsStory, user, null);
                profileStoriesFragment.storyRemoved(story);
            }
            // IT'S NOT WORKING

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("databaselayer","getuserstories failure: " + databaseError.toString());
            }
        };
        recentPostsQuery.addChildEventListener(childEventListener);
    }


    public static void getFeedStories(FeedFragment feedFragment, User user) {
        ArrayList<String> list = new ArrayList<>(user.following.keySet());
        if (list.isEmpty()) {
            final DatabaseReference myRef = FirebaseDatabase.getInstance().getReference()
                                                                            .child("stories");
            Query recentPostsQuery = myRef.limitToLast(20);
            createQuery(recentPostsQuery, feedFragment, 0);
        } else {
            for (String uid : list) {
                final DatabaseReference myRef = FirebaseDatabase.getInstance().getReference()
                                                                .child("userStories").child(uid);
                Query recentPostsQuery = myRef.limitToLast(10);
                createQuery(recentPostsQuery, feedFragment, 1);
            }
        }
    }

    public static void createQuery (Query query, final FeedFragment feedFragment, final int queryNum) {
        ValueEventListener valueEventListener = new ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                ArrayList<BasicStory> storyList = new ArrayList<>();
                for (DataSnapshot snap : dataSnapshot.getChildren()) {
                    HashMap<String, Object> obj = (HashMap<String, Object>) snap.getValue();
                    storyList.add(new BasicStory(((String) obj.get("storyId")), ((String) obj.get("description")),
                            ((String) obj.get("imageURL")), ((String) obj.get("videoURL")),
                            new Date((Long) obj.get("creationDate")),
                            (((Long) obj.get("theme")).intValue()), ((String) obj.get("userId"))));
                }
                if (queryNum == 0) feedFragment.generalStoryQuestionAdded(storyList);
                else feedFragment.followingStoryQuestionAdded(storyList);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w("DatabaseLayer", "loadPost:onCancelled", databaseError.toException());
            }
        };
        query.addListenerForSingleValueEvent(valueEventListener);
    }

    // submit Story to database
    public static void submitStory (final BasicStory story) {
        final DatabaseReference myRef = FirebaseDatabase.getInstance().getReference();
        story.storyId = myRef.child("stories").push().getKey();
        submitStoryTransaction(story);
    }

    //uploads image
    public static void uploadImageStory(final Story story) {
        //TODO what if no storyid though
        final DatabaseReference myRef = FirebaseDatabase.getInstance().getReference();
        final String storiesKey = myRef.child("stories").push().getKey();
        story.storyId = storiesKey;
        StorageReference storageRef = FirebaseStorage.getInstance().
                getReferenceFromUrl("gs://peeping-tom.appspot.com").
                child("stories").child(storiesKey);

        UploadTask uploadTask;
        boolean isImage;
        if (story.getMedia() != null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            story.getMedia().compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();
            uploadTask = storageRef.putBytes(data);
            isImage = true;
        } else {
            Uri file = story.getVideoUri();
            StorageMetadata metadata = new StorageMetadata.Builder().setContentType("video/mp4").build();
            uploadTask = storageRef.putFile(file, metadata);
            isImage = false;
        }
        final boolean finalIsImage = isImage;
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // TODO: Handle unsuccessful uploads
                Log.d("databaselayer","upload failed!");
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                if (downloadUrl != null) {
                    if (finalIsImage) story.imageURL = downloadUrl.toString();
                    else story.videoURL = downloadUrl.toString();
                    submitStoryTransaction(story);
                }
            }
        });
    }

    //download videos
    public static void downloadVideoStory(final Story story, final AbstractStoryFragment storyFragment) {
        if (CacheManager.checkForVideoAndUpdate(story, storyFragment.getContext())) {
            storyFragment.onVideoDownloaded(story);
            return;
        }
        final StorageReference storageRef = FirebaseStorage.getInstance().
                        getReferenceFromUrl("gs://peeping-tom.appspot.com").
                                    child("stories").child(story.getStoryId());
        File localFile = null;
        try {
            String state = Environment.getExternalStorageState();
            if (Environment.MEDIA_MOUNTED.equals(state)) {
                localFile = CacheManager.createCacheFile(storyFragment.getActivity(), story.getStoryId(), ".mp4");
                if (localFile == null) return;
            } else localFile = File.createTempFile(story.getStoryId(), ".mp4");
        } catch (IOException e) {
            e.printStackTrace();
        }
        final File finalLocalFile = localFile;
        storageRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                story.setVideoUri(Uri.fromFile(finalLocalFile));
                storyFragment.onVideoDownloaded(story);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.d("databaselayer", exception.toString());
            }
        });
    }

    private static void submitStoryTransaction(final BasicStory story) {
        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference();
        Map<String, Object> postValues = story.toMap();
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/stories/" + story.storyId, postValues);
        childUpdates.put("/userStories/" + story.userId + "/" + story.storyId, postValues);
        myRef.updateChildren(childUpdates);
    }

    //deletes story
    public static void deleteStory (final BasicStory story) {
        //TODO transactions suck
        final DatabaseReference myRef = FirebaseDatabase.getInstance().getReference();
        Log.d("databaselayer",story.getStoryId());
        if (story.getImageURL() != null || story.getVideoURL() != null) {
            final StorageReference storageRef = FirebaseStorage.getInstance().
                    getReferenceFromUrl("gs://peeping-tom.appspot.com").child("stories").child(story.getStoryId());
            storageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d("databaseLayer","file deleted successfully");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d("databaseLayer","file deleted unsuccessfully");
                }
            });
        }
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/stories/" + story.storyId, null);
        childUpdates.put("/userStories/" + story.userId + "/" + story.storyId, null);
        myRef.updateChildren(childUpdates);
    }

    // get user from accountactivity
    public static void getMainUser (final AccountActivity accountActivity, final String userid) {
        final DatabaseReference myRef = FirebaseDatabase.getInstance().getReference()
                                    .child("users").child(userid);
        final ValueEventListener userListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    User user = dataSnapshot.getValue(User.class);
                    User.initializeUser(user);
                    accountActivity.mainUser(user);
                }
                else accountActivity.mainUser(null);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w("DatabaseLayer", "loadPost:onCancelled", databaseError.toException());
                // ...
            }
        };
        myRef.addListenerForSingleValueEvent(userListener);
    }

    public static void getInboxUser (final InboxFragment inboxFragment, String userId) {
        final DatabaseReference myRef = FirebaseDatabase.getInstance().getReference()
                .child("users").child(userId);
        final ValueEventListener userListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                User user;
                if (dataSnapshot.getValue() != null) {
                    user = dataSnapshot.getValue(User.class);
                    User.initializeUser(user);
                    inboxFragment.onUserFetched(user);
                } else
                    Log.e("databaselayer", "NO USER ERR");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w("DatabaseLayer", "loadPost:onCancelled", databaseError.toException());
                // ...
            }
        };
        myRef.addListenerForSingleValueEvent(userListener);
    }

    public static void getFeedUsers (final FeedFragment feedFragment, ArrayList<String> userIdList,
                                                                          final int localListKind) {
        for (String userId : userIdList) {
            final DatabaseReference myRef = FirebaseDatabase.getInstance().getReference()
                    .child("users").child(userId);
            final ValueEventListener userListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // Get Post object and use the values to update the UI
                    User user;
                    if (dataSnapshot.getValue() != null) {
                        user = dataSnapshot.getValue(User.class);
                        User.initializeUser(user);
                        feedFragment.userFetched(user, localListKind);
                    } else Log.e("databaselayer", "NO USER ERR");
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Getting Post failed, log a message
                    Log.w("DatabaseLayer", "loadPost:onCancelled", databaseError.toException());
                    // ...
                }
            };
            myRef.addListenerForSingleValueEvent(userListener);
        }
    }

    public static void getFollowUsers (final AbstractFriendsFragment friendsFragment, ArrayList<String> userIdList) {
        for (String userId : userIdList) {
            final DatabaseReference myRef = FirebaseDatabase.getInstance().getReference()
                    .child("users").child(userId);
            final ValueEventListener userListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // Get Post object and use the values to update the UI
                    User user;
                    if (dataSnapshot.getValue() != null) {
                        user = dataSnapshot.getValue(User.class);
                        User.initializeUser(user);
                        friendsFragment.onFollowUsersFetched(user);
                    } else
                        Log.e("databaselayer", "NO USER ERR");
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Getting Post failed, log a message
                    Log.w("DatabaseLayer", "loadPost:onCancelled", databaseError.toException());
                    // ...
                }
            };
            myRef.addListenerForSingleValueEvent(userListener);
        }
        userIdList.clear();
    }

    public static void getSearchUsers (final SearchFragment searchFragment,User.Region region,
            User.CommunityDensity density, User.Gender gender, User.SexualOrientation orientation,
                                                                          User.Religion religion) {
        //TODO search by age range
        final DatabaseReference myRef = FirebaseDatabase.getInstance().getReference().child("users");
        Query query = myRef.limitToLast(15);
        //you can only do one at a time
        if (region != User.Region.NONE)
            query = query.orderByChild("region").equalTo(region.ordinal());
        else if (density != User.CommunityDensity.NONE)
            query = query.orderByChild("communityDensity").equalTo(density.ordinal());
        else if (gender != User.Gender.NONE)
            query = query.orderByChild("gender").equalTo(gender.ordinal());
        else if (orientation != User.SexualOrientation.NONE)
            query = query.orderByChild("sexualOrientation").equalTo(orientation.ordinal());
        else if (religion != User.Religion.NONE)
            query = query.orderByChild("religion").equalTo(religion.ordinal());

        final ValueEventListener userListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                ArrayList<User> userList = new ArrayList<>();
                if (dataSnapshot.getValue() != null) {
                    for (DataSnapshot snap : dataSnapshot.getChildren()) {
                        User user = snap.getValue(User.class);
                        User.initializeUser(user);
                        if (!Objects.equals(user.getUid(), ((GlobalVars) searchFragment.getActivity().getApplication()).getUser().getUid()))
                            userList.add(user);
                    }
                }
                searchFragment.onUsersFetched(userList);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w("DatabaseLayer", "loadPost:onCancelled", databaseError.toException());
                // ...
            }
        };
        query.addListenerForSingleValueEvent(userListener);
    }

    // submit User to database
    public static void submitUser (User user) {
        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference();
        myRef.child("users").child(user.getUid()).setValue(user);
    }

    public static void updateUserProfile (User user) {
        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference();
        myRef.child("users").child(user.getUid()).child("profileUrl").setValue(user.getProfileUrl());
    }

    public static void updateUserFollowers (User user, String otherUserID, boolean toAdd) {
        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference().child("users").
                                        child(user.getUid()).child("followers").child(otherUserID);
        if (toAdd) myRef.setValue(true);
        else myRef.setValue(null);
    }

    public static void updateUserFollowing (User user, String otherUserID, boolean toAdd) {
        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference().child("users").
                                        child(user.getUid()).child("following").child(otherUserID);
        if (toAdd) myRef.setValue(true);
        else myRef.setValue(null);
    }

    public static void submitNewMessage(String userId1, String userId2, Message message,
                                        ChatDescription description, MessageActivity messageActivity) {
        //TODO update main user because they have new chat
        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference();
        String descriptionId = myRef.child("chats").child(message.getUserId()).push().getKey();
        String messageId = myRef.child("messages").child(descriptionId).push().getKey();
        message.setMessageId(messageId);
        description.setChatId(descriptionId);
        Map<String, Object> postDescription = description.toMap();
        Map<String, Object> postMessage = message.toMap();
        Map<String, Object> childUpdates = new HashMap<>();

        childUpdates.put("/userChats/" + userId1 + "/" + descriptionId, descriptionId);
        childUpdates.put("/userChats/" + userId2 + "/" + descriptionId, descriptionId);
        childUpdates.put("/chats/" + descriptionId, postDescription);
        childUpdates.put("/messages/" + descriptionId + "/" + messageId, postMessage);
        myRef.updateChildren(childUpdates);
        messageActivity.onMessageFetched(message);
    }

    public static void submitMessage(Message message, ChatDescription description,
                                                    MessageActivity messageActivity) {
        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference();
        String messageId = myRef.child("messages").child(description.getChatId()).push().getKey();
        Map<String, Object> postMessage = message.toMap();
        Map<String, Object> childUpdates = new HashMap<>();
        description.lastMessage = message;
        Map<String, Object> postDescription = description.toMap();
        childUpdates.put("/messages/" + description.getChatId() + "/" + messageId, postMessage);
        childUpdates.put("/chats/" + description.getChatId(), postDescription);
        myRef.updateChildren(childUpdates);
        //messageActivity.onMessageFetched(message);
    }

    public static void getMessages(String descriptionId, final MessageActivity messageActivity) {
        final DatabaseReference myRef = FirebaseDatabase.getInstance().getReference()
                .child("messages").child(descriptionId);
        Query recentPostsQuery = myRef.limitToLast(20);
        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                HashMap<String, Object> obj = (HashMap<String, Object>) dataSnapshot.getValue();
                Message message = new Message();
                message.setMessageFromMap(obj);
                messageActivity.onMessageFetched(message);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                //TODO this
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("databaselayer","getuserstories failure: " + databaseError.toString());
            }
        };
        recentPostsQuery.addChildEventListener(childEventListener);
    }

    public static void getChatIds(String userId, final InboxFragment inboxFragment) {
        final DatabaseReference myRef = FirebaseDatabase.getInstance().getReference()
                                                        .child("userChats").child(userId);
        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                inboxFragment.onChatIdFetched((String) dataSnapshot.getValue(), this, myRef);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("databaselayer","getuserstories failure: " + databaseError.toString());
            }
        };
        myRef.addChildEventListener(childEventListener);
    }

    public static void getChatFromId(String id, final InboxFragment inboxFragment) {
        final DatabaseReference myRef = FirebaseDatabase.getInstance().getReference()
                .child("chats").child(id);
        final ValueEventListener chatListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                ChatDescription description = new ChatDescription();
                HashMap<String, Object> obj = (HashMap<String, Object>) dataSnapshot.getValue();
                description.lastMessage = new Message();
                description.setDescriptionFromMap(obj);
                inboxFragment.onChatFetched(description, this);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w("DatabaseLayer", "loadPost:onCancelled", databaseError.toException());
                // ...
            }
        };
        myRef.addValueEventListener(chatListener);
    }

    public static void getUserChats(String userId, final InboxFragment inboxFragment) {
        final DatabaseReference myRef = FirebaseDatabase.getInstance().getReference()
                .child("userChats").child(userId);

        final ValueEventListener userChatListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                ArrayList<String> userIdList = new ArrayList<>();
                for (DataSnapshot snap : dataSnapshot.getChildren()) {
                    userIdList.add((String)snap.getValue());
                }
                inboxFragment.onUserChatsFetched(userIdList);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w("DatabaseLayer", "loadPost:onCancelled", databaseError.toException());
            }
        };
        myRef.addListenerForSingleValueEvent(userChatListener);
    }

    //gets the questions and puts the arraylist in mainactivity
    public static void downloadQuestions (final MainActivity mainActivity, final Context context)
            throws IOException {
        final StorageReference storageRef = FirebaseStorage.getInstance().
                getReferenceFromUrl("gs://peeping-tom.appspot.com").
                child("questionslist.csv");

        final File file = new File(context.getFilesDir(), "questionslist.csv");

        storageRef.getFile(file).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                //file created
                    if (mainActivity != null)
                        mainActivity.onQuestionsDownloaded(readQuestions(file));
                    storageRef.getFile(file).removeOnSuccessListener(this);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
                Log.d("databaselayer",exception.toString());
                storageRef.getFile(file).removeOnFailureListener(this);
            }
        });
    }
    //this is called when we want to download the questions, but don't need to do anything with the file
    public static void downloadQuestions (final Context context) throws IOException {
        final StorageReference storageRef = FirebaseStorage.getInstance().
                getReferenceFromUrl("gs://peeping-tom.appspot.com").
                child("questionslist.csv");

        final File file = new File(context.getFilesDir(), "questionslist.csv");

        storageRef.getFile(file).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                //file created
                storageRef.getFile(file).removeOnSuccessListener(this);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
                Log.d("databaselayer",exception.toString());
                storageRef.getFile(file).removeOnFailureListener(this);
            }
        });
    }

    public static ArrayList<String> readQuestions(File file) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        ArrayList<String> questionsList= new ArrayList<>();
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] rowData = line.split(",");
                questionsList.add(rowData[0]);
                // do something with "data" and "value"
            }
        }
        catch (IOException ex) {
            // handle exception
            Log.d("databaselayer",ex.toString());
        }
        return questionsList;
    }
}