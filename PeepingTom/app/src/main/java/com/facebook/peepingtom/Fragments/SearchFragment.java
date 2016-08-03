package com.facebook.peepingtom.Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.peepingtom.Activities.MainActivity;
import com.facebook.peepingtom.Adapters.SearchRecyclerAdapter;
import com.facebook.peepingtom.Database.DatabaseLayer;
import com.facebook.peepingtom.Models.User;
import com.facebook.peepingtom.R;

import java.util.ArrayList;
import java.util.Objects;

/**
 * Created by aespino on 7/8/16.
 */
public class SearchFragment extends Fragment implements DatabaseLayer.SearchUsers,
                                                                 MainActivity.SetUser {
    public RecyclerView rvSearch;
    public ArrayList<User> userList;
    public SearchRecyclerAdapter searchAdapter;
    public int updating;
    public User.Gender gender = User.Gender.NONE;
    public User.Religion religion = User.Religion.NONE;
    public User.Region region = User.Region.NONE;
    public User.CommunityDensity density = User.CommunityDensity.NONE;
    public User.SexualOrientation orientation = User.SexualOrientation.NONE;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userList = new ArrayList<>();
        updating = 1;
        ((MainActivity)getActivity()).searchFragment = this;
        //userList.add(((GlobalVars)getActivity().getApplication()).getUser());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                                Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_search, container, false);
        rvSearch = (RecyclerView) v.findViewById(R.id.rvSearch);
        searchAdapter = new SearchRecyclerAdapter(userList, this);
        rvSearch.setAdapter(searchAdapter);
        DatabaseLayer.getSearchUsers(this, User.Region.NONE, User.CommunityDensity.NONE,
                User.Gender.NONE, User.SexualOrientation.NONE, User.Religion.NONE);
        rvSearch.setLayoutManager(new LinearLayoutManager(getActivity()));
        ((MainActivity)getActivity()).getSupportActionBar().setTitle("Search");
        return v;
    }

    @Override
    public void onUsersFetched(ArrayList<User> userList) {
        if (updating == 1) {
            this.userList.clear();
            updating = 0;
        }
        // must update the local userList AND the userList in the adapter
        // (they should be the same list though so unclear why?!?)
        this.userList = userList;
        searchAdapter.userList = userList;
        searchAdapter.notifyDataSetChanged();
    }

    @Override
    public void setUser(User user) {
        // TODO: MAKE THIS MORE EFFICIENT
        // looks for the profileUser in the userList and updates it localy and in the adapter
        for (int i = 0; i < userList.size(); i ++){
            if (Objects.equals(userList.get(i).getUid(), user.getUid())) {
                userList.set(i, user);
                searchAdapter.notifyItemChanged(i + 1);
            }
        }
    }
}