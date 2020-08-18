package com.roichomsky.socialmid;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
        Post post = mPost.get(position);
        try {
            Picasso.get().load(post.getImage()).into(holder.postIv);
        }
        catch (Exception e){
            Picasso.get().load(R.drawable.ic_add_image).into(holder.postIv);
        }

        if (post.getDescription().equals("")){
            holder.descriptionTv.setVisibility(View.GONE);
        } else {
            holder.descriptionTv.setVisibility(View.VISIBLE);
            holder.descriptionTv.setText(post.getDescription());
        }

        publisherInfo(holder.avatarIv, holder.usernameTv, holder.username2Tv, post.getPublisherID());

    }

    @Override
    public int getItemCount() {
        //returns the posts list size
        return mPost.size();
    }

    class MyHolder extends RecyclerView.ViewHolder{

        //layout view holder

        //Views
        ImageView avatarIv, postIv, likeIv, commentIv, shareIv;
        TextView usernameTv, username2Tv, likesTv, descriptionTv, commentsTv;

        public MyHolder(@NonNull View itemView){
            super(itemView);

            //init
            avatarIv = itemView.findViewById(R.id.avatarIv);
            postIv = itemView.findViewById(R.id.postIv);
            likeIv = itemView.findViewById(R.id.likeIv);
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
}
