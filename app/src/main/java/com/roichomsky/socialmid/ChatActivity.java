package com.roichomsky.socialmid;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.like.Utils;
import com.squareup.picasso.Picasso;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ChatAdapter mChatAdapter;
    private List<Message> messageList;

    public String friendUID, myUID;

    ActionBar actionBar;
    Toolbar toolbar;

    TextView nameTv;
    ImageView avatarIv;
    EditText messageEt;
    Button sendBtn;

    DatabaseReference refForSeen;
    ValueEventListener seenListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        toolbar = findViewById(R.id.chat_toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_action_back));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        avatarIv = findViewById(R.id.avatarIv);
        nameTv = findViewById(R.id.toolbar_name);
        messageEt = findViewById(R.id.chatboxEt);
        sendBtn = findViewById(R.id.chatbox_sendBtn);

        friendUID = getIntent().getStringExtra("uid");
        myUID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        getUserData(friendUID,nameTv,avatarIv);


        recyclerView = findViewById(R.id.recyclerview_message_list);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        messageList = new ArrayList<>();

        readMessages();

        seenMessages();

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(messageEt.getText().toString())) {
                    sendMessage(messageEt.getText().toString());
                }
                else{
                    Toast.makeText(getApplicationContext(), "Cannot send empty message...", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home){
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    void getUserData(String uid, TextView nameTv, ImageView avatarIv){
        //get path of database named "Users" containing users info
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(uid);
        //get all data from path

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (user.getImage() != null){
                    try{
                        Picasso.get().load(user.getImage()).into(avatarIv);
                    }
                    catch (Exception e){ }
                }
                nameTv.setText(user.getName());

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    void sendMessage(String message){
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        HashMap<String, Object> results = new HashMap<>();
        results.put("sender", FirebaseAuth.getInstance().getCurrentUser().getUid());
        results.put("receiver", friendUID);
        results.put("timestamp", Long.toString(timestamp.getTime()));
        results.put("message", message);
        results.put("seen", "false");

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Chat");
        ref.push().setValue(results);
        messageEt.setText("");
    }

    void readMessages() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Chat");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                messageList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()){
                    Message message = ds.getValue(Message.class);
                    if ((message.getReceiver().equals(friendUID) && message.getSender().equals(myUID)) ||
                            (message.getReceiver().equals(myUID) && message.getSender().equals(friendUID))){
                        messageList.add(message);
                    }

                    mChatAdapter = new ChatAdapter(getApplicationContext(), messageList);
                    mChatAdapter.notifyDataSetChanged();

                    recyclerView.setAdapter(mChatAdapter);
                    recyclerView.scrollToPosition(messageList.size()-1);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    void seenMessages(){
        refForSeen = FirebaseDatabase.getInstance().getReference("Chat");
        HashMap<String, Object> hasSeen = new HashMap<>();
        hasSeen.put("seen", "true");
        seenListener = refForSeen.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()){
                    Message message = ds.getValue(Message.class);
                    if (message.getReceiver().equals(myUID) && message.getSender().equals(friendUID)){
                        ds.getRef().updateChildren(hasSeen);
                    }

                    mChatAdapter = new ChatAdapter(getApplicationContext(), messageList);
                    mChatAdapter.notifyDataSetChanged();

                    recyclerView.setAdapter(mChatAdapter);
                    recyclerView.scrollToPosition(messageList.size()-1);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        refForSeen.removeEventListener(seenListener);
    }
}
