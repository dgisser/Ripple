package com.facebook.peepingtom.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.peepingtom.Activities.MainActivity;
import com.facebook.peepingtom.Activities.MessageActivity;
import com.facebook.peepingtom.Adapters.InboxRecyclerAdapter;
import com.facebook.peepingtom.Database.DatabaseLayer;
import com.facebook.peepingtom.GlobalVars;
import com.facebook.peepingtom.Models.ChatDescription;
import com.facebook.peepingtom.Models.User;
import com.facebook.peepingtom.R;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Objects;

/**
 * Created by aespino on 7/8/16.
 */
public class InboxFragment extends DialogFragment implements DatabaseLayer.InboxChats {
    RecyclerView rvInbox;
    InboxRecyclerAdapter inboxAdapter;
    ArrayList<ChatDescription> descriptionList;
    ArrayList<ValueEventListener> valueEventListeners;
    HashMap<String, User> userMap;
    DatabaseReference databaseReference;
    ChildEventListener childEventListener;
    User newMessageUser;
    int chatCounter = 0;
    int chatTotalSize = -1;
    public static final int CHANGEDESCRIPTION = 14234;

    @Override
    public void onCreate(Bundle savedInstanceState) { super.onCreate(savedInstanceState); }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                                Bundle savedInstanceState) {
        chatTotalSize = -1;
        View v = inflater.inflate(R.layout.fragment_inbox, container, false);
        rvInbox = (RecyclerView) v.findViewById(R.id.rvInbox);
        inboxAdapter = new InboxRecyclerAdapter(this);
        MainActivity mainActivity = (MainActivity) this.getActivity();
        if (mainActivity.descriptionList == null || mainActivity.descriptionList.size() == 0)
            descriptionList = new ArrayList<>();
        else descriptionList = ((MainActivity) this.getActivity()).descriptionList;
        chatCounter = descriptionList.size();
        inboxAdapter.descriptionList = descriptionList;
        mainActivity.descriptionList = descriptionList;
        newMessageUser = ((MainActivity)getActivity()).newMessageUser;
        if (mainActivity.userMap == null || mainActivity.userMap.size() == 0)
            userMap = new HashMap<>();
        else userMap = ((MainActivity) this.getActivity()).userMap;
        User mainUser = ((GlobalVars)getActivity().getApplication()).getUser();
        userMap.put(mainUser.getUid(), mainUser);
        inboxAdapter.userMap = userMap;
        if (newMessageUser != null) userMap.put(newMessageUser.getUid(), newMessageUser);
        mainActivity.userMap = userMap;
        valueEventListeners = new ArrayList<>();
        userMap.put(mainUser.getUid(), mainUser);
        if (childEventListener == null) DatabaseLayer.getChatIds(mainUser.getUid(), this);
        rvInbox.setAdapter(inboxAdapter);
        if (newMessageUser != null && !descriptionList.isEmpty()) {
            //so inefficient
            ChatDescription thisDescription = null;
            for (ChatDescription description : descriptionList) {
                if (Objects.equals(description.userid1, newMessageUser.getUid()) ||
                        Objects.equals(description.userid2, newMessageUser.getUid()))
                    thisDescription = description;
            }
            //todo fix message setup
            if (thisDescription != null) {
                Intent i = new Intent(getContext(), MessageActivity.class);
                i.putExtra("otherUser", Parcels.wrap(newMessageUser));
                i.putExtra("description", Parcels.wrap(thisDescription));
                startActivityForResult(i, InboxFragment.CHANGEDESCRIPTION);
                ((MainActivity) getActivity()).newMessageUser = null;
            } else DatabaseLayer.getUserChats(mainUser.getUid(), this);
        } else if (newMessageUser != null) DatabaseLayer.getUserChats(mainUser.getUid(), this);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        rvInbox.setLayoutManager(llm);
        ((MainActivity)getActivity()).getSupportActionBar().setTitle("Inbox");
        return v;
    }

    @Override
    public void onChatIdFetched(String descriptionId, ChildEventListener childEventListener,
                                DatabaseReference databaseReference) {
        this.childEventListener = childEventListener;
        this.databaseReference = databaseReference;
        DatabaseLayer.getChatFromId(descriptionId, this);
    }

    @Override
    public void onChatFetched(ChatDescription description, ValueEventListener valueEventListener) {
        chatCounter ++;
        valueEventListeners.add(valueEventListener);
        if (!userMap.containsKey(description.getUserid1())) {
            descriptionList.add(description);
            DatabaseLayer.getInboxUser(this, description.getUserid1());
        } else if (!userMap.containsKey(description.getUserid2())) {
            descriptionList.add(description);
            DatabaseLayer.getInboxUser(this, description.getUserid2());
        } else {
            int index = -1;
            for (int x = 0; x < descriptionList.size(); x ++) {
                if (Objects.equals(description.getChatId(), descriptionList.get(x).getChatId())) {
                    index = x;
                    break;
                }
            }
            if (index != -1) descriptionList.set(index, description);
            else descriptionList.add(description);
        }
    }

    //TODO would be more efficient if this was a map
    @Override
    public void onUserChatsFetched(ArrayList<String> chatList) {
        // user hasn't chatted anyone

        if (chatList.isEmpty()) {
            Intent i = new Intent(getContext(), MessageActivity.class);
            i.putExtra("otherUser", Parcels.wrap(newMessageUser));
            startActivityForResult(i, InboxFragment.CHANGEDESCRIPTION);
            ((MainActivity)getActivity()).newMessageUser = null;
            return;
        }

        User mainUser = ((GlobalVars)getActivity().getApplication()).getUser();
        if (chatList.size() == chatCounter) {
            ChatDescription thisDescription = null;
            for (ChatDescription description : descriptionList) {
                if (Objects.equals(description.userid1, newMessageUser.getUid()) ||
                        Objects.equals(description.userid2, newMessageUser.getUid()))
                    thisDescription = description;
            }
            if (thisDescription == null)
                thisDescription = new ChatDescription(mainUser.getUid(), newMessageUser.getUid(),
                    null, null);
            Intent i = new Intent(getContext(), MessageActivity.class);
            i.putExtra("otherUser", Parcels.wrap(newMessageUser));
            i.putExtra("description", Parcels.wrap(thisDescription));
            startActivityForResult(i, InboxFragment.CHANGEDESCRIPTION);
            ((MainActivity) getActivity()).newMessageUser = null;
        } else chatTotalSize = chatList.size();
    }

    @Override
    public void onUserFetched(User user) {
        userMap.put(user.getUid(), user);

        if (descriptionList.size() == userMap.size() - 1) {
            sortList();
            inboxAdapter.notifyDataSetChanged();
        }

        if (newMessageUser != null && chatCounter == chatTotalSize) {
            ChatDescription thisDescription = null;
            for (ChatDescription descriptionIter : descriptionList) {
                if (Objects.equals(descriptionIter.userid1, newMessageUser.getUid()) ||
                        Objects.equals(descriptionIter.userid2, newMessageUser.getUid())) {
                    thisDescription = descriptionIter;
                    break;
                }
            }
            if (thisDescription != null) {
                Intent i = new Intent(getContext(), MessageActivity.class);
                i.putExtra("otherUser", Parcels.wrap(newMessageUser));
                i.putExtra("description", Parcels.wrap(thisDescription));
                startActivityForResult(i, InboxFragment.CHANGEDESCRIPTION);
            } else {
                User mainUser = ((GlobalVars)getActivity().getApplication()).getUser();
                thisDescription = new ChatDescription(mainUser.getUid(), newMessageUser.getUid(),
                        null, null);
                Intent i = new Intent(getContext(), MessageActivity.class);
                i.putExtra("otherUser", Parcels.wrap(newMessageUser));
                i.putExtra("description", Parcels.wrap(thisDescription));
                startActivityForResult(i, InboxFragment.CHANGEDESCRIPTION);
            }
            ((MainActivity) getActivity()).newMessageUser = null;
        }
    }

    public void sortList() {
        Collections.sort(descriptionList, new Comparator<ChatDescription>() {
            @Override
            public int compare(ChatDescription chat2, ChatDescription chat1)
            {
                return  chat1.getLastMessage().getSentTime().
                        compareTo(chat2.getLastMessage().getSentTime());
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CHANGEDESCRIPTION && resultCode == MainActivity.RESULT_OK) {
            ChatDescription description = Parcels.unwrap(data.getParcelableExtra("description"));
            //this code is inefficient
            for (ChatDescription chatDescription : descriptionList) {
                if (Objects.equals(chatDescription.getChatId(), description.getChatId())) {
                    int index = descriptionList.indexOf(chatDescription);
                    descriptionList.set(index, description);
                }
            }
            sortList();
            inboxAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (databaseReference != null) databaseReference.removeEventListener(childEventListener);
        if (valueEventListeners != null) {
            for (ValueEventListener valueEventListener : valueEventListeners) {
                assert databaseReference != null;
                databaseReference.removeEventListener(valueEventListener);
            }
        }
    }
}
