package com.roichomsky.socialmid;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
    public String profileUserUID;
    public String baseClass;

    FirebaseUser firebaseUser;

    ProgressDialog pd;

    public PostAdapter(Context mContext, List<Post> mPost, String profileUserUID, String baseClass){
        this.mContext = mContext;
        this.mPost = mPost;
        this.profileUserUID = profileUserUID;
        this.baseClass = baseClass;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.post_item, parent, false);
        return new PostAdapter.MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyHolder holder, final int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        final Post post = mPost.get(position);

        if (post.getDescription().equals("")){
            holder.descriptionTv.setVisibility(View.GONE);
        } else {
            holder.descriptionTv.setVisibility(View.VISIBLE);
            holder.descriptionTv.setText(post.getDescription());
        }

        pd = new ProgressDialog(mContext);

        final Intent intent = new Intent(mContext, CommentsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("postID", post.getPostID());
        intent.putExtra("publisherID", post.getPublisherID());
        intent.putExtra("description", post.getDescription());
        intent.putExtra("baseClass", baseClass);

        holder.commentsTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mContext.startActivity(intent);
            }
        });

        holder.commentIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseUser.getUid();
                addComment(firebaseUser.getUid(), post.getPostID(), intent);
            }
        });

        holder.postIv.setOnClickListener(new DoubleClickListener() {
            @Override
            public void onDoubleClick() {
                alreadyLiked(firebaseUser.getUid(), post.getPostID(), holder.likeBtn);

                if (!holder.likeBtn.isLiked()){
                    final int[] currentLikes = {0};
                    final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Likes").child(post.getPostID());
                    final DatabaseReference reference2 = reference.child("likedByList");
                    reference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Like likes = dataSnapshot.getValue(Like.class);

                            if (likes != null) {
                                holder.likesTv.setText(likes.getLikesCounter() + " likes");
                                currentLikes[0] = Integer.parseInt(likes.getLikesCounter());
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                        }
                    });

                    holder.likeBtn.setLiked(true);
                    currentLikes[0] = currentLikes[0]+1;
                    HashMap<String, Object> results = new HashMap<>();
                    results.put("likesCounter", Integer.toString(currentLikes[0]));
                    results.put("postID", post.getPostID());
                    reference.updateChildren(results)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    holder.likesTv.setText(currentLikes[0]+" likes");
                                    HashMap<String, Object> results2 = new HashMap<>();
                                    results2.put(firebaseUser.getUid(), "true");
                                    reference2.updateChildren(results2);
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    holder.likeBtn.setLiked(false);
                                }
                            });
                }
            }
        });

        holder.usernameTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!firebaseUser.getUid().equals(post.getPublisherID()) &&
                        !profileUserUID.equals(post.getPublisherID())){
                    Intent profileIntent = new Intent(mContext, UserProfileActivity.class);
                    profileIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    profileIntent.putExtra("uid", post.getPublisherID());
                    mContext.startActivity(profileIntent);
                }
            }
        });

        if (post.getPublisherID().equals(firebaseUser.getUid())){
            holder.optionIv.setVisibility(View.VISIBLE);
            holder.optionIv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String[] options = {"Edit Description", "Delete Post"};

                    //alert dialog
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    //set title
                    builder.setTitle("Post Options");

                    builder.setItems(options, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (which==0){
                                EditDescription(post);
                            }
                            else if (which==1){
                                FirebaseDatabase.getInstance().getReference("Likes").child(post.getPostID()).removeValue();
                                FirebaseDatabase.getInstance().getReference("Posts").child(post.getPostID()).removeValue().addOnSuccessListener(
                                        new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Toast.makeText(mContext, "Post has been deleted...", Toast.LENGTH_LONG).show();
                                            }
                                        }
                                ).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(mContext, "Failed to delete...", Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                        }
                    });
                    builder.create().show();
                }
            });
        }

        getCommentsAmount(post.getPostID(), holder.commentsTv);
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
        ImageView avatarIv, postIv, commentIv, optionIv;
        TextView usernameTv, username2Tv, likesTv, descriptionTv, commentsTv;

        public MyHolder(@NonNull View itemView){
            super(itemView);

            //init
            optionIv = itemView.findViewById(R.id.optionIv);
            avatarIv = itemView.findViewById(R.id.avatarIv);
            postIv = itemView.findViewById(R.id.postIv);
            likeBtn = itemView.findViewById(R.id.likeBtn);
            commentIv = itemView.findViewById(R.id.commentIv);
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
                User user = dataSnapshot.getValue(User.class);
                if (user.getImage() != null){
                    try{
                        Picasso.get().load(user.getImage()).into(avatarIv);
                    }
                    catch (Exception e){ }
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
       reference.addValueEventListener(new ValueEventListener() {
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
               currentLikes[0] = currentLikes[0]+1;
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
               currentLikes[0] = currentLikes[0]-1;
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

    private void addComment(final String uid, String postID, final Intent intent){
        //creates the branch for the comment
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Posts").child(postID).child("commentsList");
        final String key = databaseReference.push().getKey();
        assert key != null;
        databaseReference.child(key);

        //creates the dialog view
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("Add a comment...");
        //set layout of dialog
        LinearLayout linearLayout = new LinearLayout(mContext);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setPadding(10,10,10,10);
        //add edit text
        final EditText editText = new EditText(mContext);
        editText.setHint("Add a comment...");
        linearLayout.addView(editText);

        pd.setMessage("Adding the comment...");

        builder.setView(linearLayout);

        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //input text from edit text
                String value = editText.getText().toString().trim();
                if (!TextUtils.isEmpty(value)){
                    pd.show();
                    HashMap<String, Object> result = new HashMap<>();
                    result.put("publisherID", uid);
                    result.put("content", value);
                    result.put("commentID", key);
                    databaseReference.child(key).updateChildren(result)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    //added, dismiss progress bar
                                    pd.dismiss();
                                    Toast.makeText(mContext, "Added...", Toast.LENGTH_SHORT).show();
                                    mContext.startActivity(intent);
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    //failed, dismiss progress bar, get and show error message
                                    pd.dismiss();
                                    Toast.makeText(mContext, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                }
                else{
                    Toast.makeText(mContext, "Please enter validate input", Toast.LENGTH_SHORT).show();
                }
            }
        });
        //add button in dialog to cancel
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        //create and show dialog
        builder.create().show();
    }

    private void getCommentsAmount(String postID, final TextView commentsTv){
        final long[] currentComments = {0};
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts").child(postID).child("commentsList");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                currentComments[0] = dataSnapshot.getChildrenCount();
                String commentsTvText = "View all "+ currentComments[0] +" comments";
                if (currentComments[0] == 1){
                    commentsTvText = "View 1 comment";
                }
                else if(currentComments[0] == 0){
                    commentsTv.setVisibility(View.INVISIBLE);
                }
                else {
                    commentsTvText = "View all "+ currentComments[0] +" comments";
                }
                commentsTv.setText(commentsTvText);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                commentsTv.setVisibility(View.INVISIBLE);
            }
        });
    }

    private void EditDescription(final Post post) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("Edit Description");
        //set layout of dialog
        LinearLayout linearLayout = new LinearLayout(mContext);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setPadding(10,10,10,10);
        //add edit text
        final EditText editText = new EditText(mContext);
        editText.setHint("Enter Description");
        linearLayout.addView(editText);

        builder.setView(linearLayout);

        builder.setPositiveButton("Edit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //input text from edit text
                String value = editText.getText().toString().trim();
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts").child(post.getPostID()).child("description");
                if (!TextUtils.isEmpty(value)){
                    pd.setMessage("Updating...");
                    pd.show();
                    reference.setValue(value).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            pd.dismiss();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            pd.dismiss();
                            Toast.makeText(mContext, "Failed to update...", Toast.LENGTH_LONG).show();
                        }
                    });
                }
                else{
                    Toast.makeText(mContext, "Please enter validate input", Toast.LENGTH_LONG).show();
                }
            }
        });

        //add button in dialog to cancel
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.create().show();
    }
}
