package com.roichomsky.socialmid;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class DashboardActivity extends AppCompatActivity {

    //views
    FirebaseAuth firebaseAuth;

    BottomNavigationView navigationView;

    ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Actionbar and its title
        actionBar = getSupportActionBar();
        actionBar.setTitle("SocialMid");

        //init firebaseAuth
        firebaseAuth = FirebaseAuth.getInstance();


        // Bottom navigation
        navigationView = findViewById(R.id.navigation);
        navigationView.setOnNavigationItemSelectedListener(selectedListener);

        // Home fragment transaction (default, on start)
        //actionBar.setTitle("Home"); // Change actionbar title
        HomeFragment homeFragment = new HomeFragment();
        FragmentTransaction ftl = getSupportFragmentManager().beginTransaction();
        ftl.replace(R.id.content, homeFragment, "");
        ftl.commit();

        //init views

    }

    private BottomNavigationView.OnNavigationItemSelectedListener selectedListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                    // Handle item clicks
                    switch (menuItem.getItemId()){
                        case R.id.nav_home:
                            // Home fragment transaction
                            //actionBar.setTitle("Home"); // Change actionbar title
                            HomeFragment homeFragment = new HomeFragment();
                            FragmentTransaction ftl = getSupportFragmentManager().beginTransaction();
                            ftl.replace(R.id.content, homeFragment, "");
                            ftl.commit();
                            return true;
                        case R.id.nav_profile:
                            // Profile fragment transaction
                            //actionBar.setTitle("Profile"); // Change actionbar title
                            ProfileFragment profileFragment = new ProfileFragment();
                            FragmentTransaction ftl2 = getSupportFragmentManager().beginTransaction();
                            ftl2.replace(R.id.content, profileFragment, "");
                            ftl2.commit();
                            return true;
                        case R.id.nav_friends:
                            // Friends fragment transaction
                            //actionBar.setTitle("Friends"); // Change actionbar title
                            FriendsFragment friendsFragment = new FriendsFragment();
                            FragmentTransaction ftl3 = getSupportFragmentManager().beginTransaction();
                            ftl3.replace(R.id.content, friendsFragment, "");
                            ftl3.commit();
                            return true;
                    }

                    return false;
                }
            };

    @Override
    protected void onStart() {
        //checks on start of app
        startService(new Intent(this, UserService.class));
        super.onStart();
    }
}
