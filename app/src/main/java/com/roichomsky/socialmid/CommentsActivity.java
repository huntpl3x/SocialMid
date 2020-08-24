package com.roichomsky.socialmid;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class CommentsActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    ImageView avatarIv;
    TextView usernameTv, descriptionTv;

    ActionBar actionBar;

    ArrayList<Comment> commentsList;
    CommentAdapter commentAdapter;

    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        final FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();

        //support bar
        actionBar = getSupportActionBar();
        actionBar.setTitle("Comments");
        //set back button enabled
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        //init
        avatarIv = findViewById(R.id.avatarIv);
        usernameTv = findViewById(R.id.usernameTv);
        descriptionTv = findViewById(R.id.descriptionTv);

        recyclerView = findViewById(R.id.comments_recyclerView);
        //properties
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        commentsList = new ArrayList<>();

        String postID = getIntent().getStringExtra("postID");
        final String publisherID = getIntent().getStringExtra("publisherID");
        String description = getIntent().getStringExtra("description");

        publisherInfo(avatarIv, usernameTv, publisherID);
        usernameTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!fUser.getUid().equals(publisherID) &&
                        !getIntent().getStringExtra("baseClass").equals("UserProfileActivity")) {
                    Intent profileIntent = new Intent(CommentsActivity.this, UserProfileActivity.class);
                    profileIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    profileIntent.putExtra("uid", publisherID);
                    startActivity(profileIntent);
                }
            }
        });
        descriptionTv.setText(description);
        getAllComments(postID, publisherID);
    }

    private void publisherInfo(final ImageView avatarIv, final TextView usernameTv, String uid){
        //get path of database named "Users" containing users info
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(uid);
        //get all data from path

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ModelUser user = dataSnapshot.getValue(ModelUser.class);
                if (user.getImage() != null){
                    try{
                        Picasso.get().load(user.getImage()).into(avatarIv);
                    }
                    catch (Exception e){ }
                }
                usernameTv.setText(user.getName());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getAllComments(final String postID, final String publisherID){
        //get path of database named "Comments" containing comments info
        databaseReference = FirebaseDatabase.getInstance().getReference("Posts").child(postID).child("commentsList");
        //get all comments info related to the post
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                commentsList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()){
                    Comment comment = ds.getValue(Comment.class);
                    commentsList.add(comment);
                }
                if (getIntent().getStringExtra("baseClass").equals("UserProfileActivity")){
                    commentAdapter = new CommentAdapter(CommentsActivity.this, commentsList, publisherID, postID);
                }
                else{
                    commentAdapter = new CommentAdapter(CommentsActivity.this, commentsList, FirebaseAuth.getInstance().getCurrentUser().getUid(), postID);
                }
                recyclerView.setAdapter(commentAdapter);
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
}
