package com.roichomsky.socialmid;


import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {

    FirebaseAuth firebaseAuth;
    RecyclerView recyclerView;
    ArrayList<Post> postList;


    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        //init recyclerView
        recyclerView = view.findViewById(R.id.posts_recyclerView);
        //set it's properties
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        //init firebaseAuth
        firebaseAuth = FirebaseAuth.getInstance();

        //init post list
        postList = new ArrayList<>();

        return view;
    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true); // show menu in fragment
        super.onCreate(savedInstanceState);
    }

    private void checkUserStatus(){
        // Get current user
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null){
            //user is signed in stay here
            //set email of logged in user
            //mProfileTv.setText(user.getEmail());
        }
        else {
            //user is'nt signed go to main activity
            startActivity(new Intent(getActivity(), MainActivity.class));
            getActivity().finish();
        }
    }

    //inflate option menu

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //inflating menu
        inflater.inflate(R.menu.menu_main, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }


    //handle menu item click

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //get item id
        int id = item.getItemId();
        if (id==R.id.action_logout){
            firebaseAuth.signOut();
            checkUserStatus();
        }
        return super.onOptionsItemSelected(item);
    }

}
