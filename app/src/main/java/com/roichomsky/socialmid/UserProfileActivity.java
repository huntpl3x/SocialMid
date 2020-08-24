package com.roichomsky.socialmid;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;

public class UserProfileActivity extends AppCompatActivity {

    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    ActionBar actionBar;

    ImageView avatarIv, coverIv;
    TextView nameTv, emailTv;
    Button addBtn;

    FirebaseUser firebaseUser;

    ArrayList<Post> postList;
    PostAdapter postAdapter;
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        //init views
        avatarIv = findViewById(R.id.avatarIv);
        coverIv = findViewById(R.id.coverIv);
        nameTv = findViewById(R.id.nameTv);
        emailTv = findViewById(R.id.emailTv);
        addBtn = findViewById(R.id.addBtn);

        final String uid = getIntent().getStringExtra("uid");

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Users").child(uid);

        //support bar
        actionBar = getSupportActionBar();

        //set back button enabled
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        //init recyclerView
        recyclerView = findViewById(R.id.posts_recyclerView);
        //set it's properties
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerView.setHasFixedSize(true);

        postList = new ArrayList<>();

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ModelUser userProfile = dataSnapshot.getValue(ModelUser.class);

                actionBar.setTitle(userProfile.getName());

                nameTv.setText(userProfile.getName());
                emailTv.setText(userProfile.getEmail());
                if (userProfile.getImage()!= null){
                    Picasso.get().load(userProfile.getImage()).into(avatarIv);
                }
                if (userProfile.getCover() != null){
                    Picasso.get().load(userProfile.getCover()).into(coverIv);
                }

                getAllPosts(userProfile);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                finish();
            }
        });

        alreadyFriend(uid, addBtn);

        addBtn.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onClick(View v) {
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid())
                        .child("friendsList").child(uid);
                if (!addBtn.getText().equals("Friends")){
                    reference.setValue(uid).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @SuppressLint("ResourceAsColor")
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(getApplicationContext(), "Added...", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                else{
                    reference.removeValue();
                    addBtn.setText("Add Friend");
                    Toast.makeText(getApplicationContext(), "Removed...", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void getAllPosts(final ModelUser user) {
        //get path of database named "Posts" containing posts info
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
        //get all data from path
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Post post = ds.getValue(Post.class);
                    if (post.getPublisherID().equals(user.getUid())){
                        postList.add(post);
                    }

                    Collections.reverse(postList);

                    //adapter
                    postAdapter = new PostAdapter(UserProfileActivity.this, postList, user.getUid(), "UserProfileActivity");
                    recyclerView.setAdapter(postAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home){
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void alreadyFriend(final String uid, final Button addBtn){
        final boolean[] has = {false};
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid()).child("friendsList").child(uid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                has[0] = dataSnapshot.exists();
                if (has[0]){
                    addBtn.setText("Friends");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
}
