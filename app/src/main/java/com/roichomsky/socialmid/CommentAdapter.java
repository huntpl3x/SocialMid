package com.roichomsky.socialmid;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.MyHolder> {

    public Context mContext;
    public List<Comment> mComment;
    public String profileUserUID;
    public String postID;

    public CommentAdapter(Context mContext, List<Comment> mComment, String profileUserUID, String postID) {
        this.mContext = mContext;
        this.mComment = mComment;
        this.profileUserUID = profileUserUID;
        this.postID = postID;
    }

    @NonNull
    @Override
    public CommentAdapter.MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.comment_item, parent, false);
        return new CommentAdapter.MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final CommentAdapter.MyHolder holder, int position) {

        final FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
        final Comment comment = mComment.get(position);
        publisherInfo(holder.avatarIv, holder.usernameTv, comment.getPublisherID());
        holder.commentTv.setText(comment.getContent());

        if (comment.getPublisherID().equals(fUser.getUid())){
            holder.dltTv.setVisibility(View.VISIBLE);
        }
        holder.dltTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseDatabase.getInstance().getReference("Posts").child(postID).child("commentsList").child(comment.getCommentID()).removeValue()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(mContext, "Comment deleted...", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(mContext, "Comment has not been deleted...", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        holder.usernameTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!fUser.getUid().equals(comment.getPublisherID()) &&
                        !profileUserUID.equals(comment.getPublisherID())) {
                    Intent profileIntent = new Intent(mContext, UserProfileActivity.class);
                    profileIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    profileIntent.putExtra("uid", comment.getPublisherID());
                    mContext.startActivity(profileIntent);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mComment.size();
    }

    class MyHolder extends RecyclerView.ViewHolder{

        //Views - layout view holder
        ImageView avatarIv;
        TextView usernameTv, commentTv, dltTv;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            //init
            dltTv = itemView.findViewById(R.id.dltTv);
            avatarIv = itemView.findViewById(R.id.avatarIv);
            usernameTv = itemView.findViewById(R.id.usernameTv);
            commentTv = itemView.findViewById(R.id.commentTv);
        }

    }

    private void publisherInfo(final ImageView avatarIv, final TextView usernameTv, String uid){
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
                usernameTv.setText(user.getName());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
