package com.roichomsky.socialmid;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.like.LikeButton;
import com.like.OnLikeListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.MyHolder>{

    public Context mContext;
    public List<Post> mPost;

    FirebaseUser firebaseUser;

    public PostAdapter(Context mContext, List<Post> mPost){
        this.mContext = mContext;
        this.mPost = mPost;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.post_item, parent, false);
        return new PostAdapter.MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        final Post post = mPost.get(position);

        if (post.getDescription().equals("")){
            holder.descriptionTv.setVisibility(View.GONE);
        } else {
            holder.descriptionTv.setVisibility(View.VISIBLE);
            holder.descriptionTv.setText(post.getDescription());
        }

        publisherInfo(holder.avatarIv, holder.usernameTv, holder.username2Tv, post.getPublisherID());
        getPostImage(holder.postIv, post.getPostID());
        addLikesToPost(post, holder.likeBtn, holder.likesTv, firebaseUser.getUid());
    }

    @Override
    public int getItemCount() {
        //returns the posts list size
        return mPost.size();
    }

    class MyHolder extends RecyclerView.ViewHolder{

        //layout view holder

        //Views
        LikeButton likeBtn;
        ImageView avatarIv, postIv, commentIv, shareIv;
        TextView usernameTv, username2Tv, likesTv, descriptionTv, commentsTv;

        public MyHolder(@NonNull View itemView){
            super(itemView);

            //init
            avatarIv = itemView.findViewById(R.id.avatarIv);
            postIv = itemView.findViewById(R.id.postIv);
            likeBtn = itemView.findViewById(R.id.likeBtn);
            commentIv = itemView.findViewById(R.id.commentIv);
            shareIv = itemView.findViewById(R.id.shareIv);
            usernameTv = itemView.findViewById(R.id.usernameTv);
            likesTv = itemView.findViewById(R.id.likesTv);
            descriptionTv = itemView.findViewById(R.id.descriptionTv);
            commentsTv = itemView.findViewById(R.id.commentsTv);
            username2Tv = itemView.findViewById(R.id.username2Tv);
        }
    }

    private void publisherInfo(final ImageView avatarIv, final TextView usernameTv, final TextView username2Tv, String uid){
        //get path of database named "Users" containing users info
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(uid);
        //get all data from path

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ModelUser user = dataSnapshot.getValue(ModelUser.class);
                try {
                    Picasso.get().load(user.getImage()).into(avatarIv);
                }
                catch (Exception e){
                    Picasso.get().load(R.drawable.ic_add_image).into(avatarIv);
                }
                usernameTv.setText(user.getName());
                username2Tv.setText(user.getName());

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getPostImage(final ImageView postIv, String postID){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts").child(postID);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Post post = dataSnapshot.getValue(Post.class);
                try {
                    Picasso.get().load(post.getPostURL()).into(postIv);
                }
                catch (Exception e){
                    Picasso.get().load(R.drawable.ic_add_image).into(postIv);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void addLikesToPost(final Post post, final LikeButton likeBtn, final TextView likesTv, final String uid){
       final int[] currentLikes = {0};
       final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Likes").child(post.getPostID());
       final DatabaseReference reference2 = reference.child("likedByList");
       reference.addListenerForSingleValueEvent(new ValueEventListener() {
           @Override
           public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
               Like likes = dataSnapshot.getValue(Like.class);

               if (likes != null) {
                   likesTv.setText(likes.getLikesCounter() + " likes");
                   currentLikes[0] = Integer.parseInt(likes.getLikesCounter());
               }
           }

           @Override
           public void onCancelled(@NonNull DatabaseError databaseError) {
           }
       });

        alreadyLiked(uid, post.getPostID(), likeBtn);

       likeBtn.setOnLikeListener(new OnLikeListener() {
           @Override
           public void liked(final LikeButton likeButton) {
               likeButton.setLiked(true);
               currentLikes[0]++;
               HashMap<String, Object> results = new HashMap<>();
               results.put("likesCounter", Integer.toString(currentLikes[0]));
               results.put("postID", post.getPostID());
               reference.updateChildren(results)
                       .addOnSuccessListener(new OnSuccessListener<Void>() {
                           @Override
                           public void onSuccess(Void aVoid) {
                               likesTv.setText(currentLikes[0]+" likes");
                               HashMap<String, Object> results2 = new HashMap<>();
                               results2.put(uid, "true");
                               reference2.updateChildren(results2);
                           }
                       })
                       .addOnFailureListener(new OnFailureListener() {
                           @Override
                           public void onFailure(@NonNull Exception e) {
                               likeButton.setLiked(false);
                           }
                       });
           }

           @Override
           public void unLiked(final LikeButton likeButton) {
               likeButton.setLiked(false);
               HashMap<String, Object> results = new HashMap<>();
               currentLikes[0]--;
               results.put("likesCounter", Integer.toString(currentLikes[0]));
               results.put("postID", post.getPostID());
               reference.updateChildren(results)
                       .addOnSuccessListener(new OnSuccessListener<Void>() {
                           @Override
                           public void onSuccess(Void aVoid) {
                               likesTv.setText(currentLikes[0]+" likes");
                                reference2.child(uid).removeValue();
                           }
                       })
                       .addOnFailureListener(new OnFailureListener() {
                           @Override
                           public void onFailure(@NonNull Exception e) {
                               likeButton.setLiked(true);
                           }
                       });
           }
       });
   }

    private void alreadyLiked(final String uid, String postID, final LikeButton likeButton){
        final boolean[] has = {false};
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Likes").child(postID).child("likedByList").child(uid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                has[0] = dataSnapshot.exists();
                if (has[0]){
                    likeButton.setLiked(true);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
}
