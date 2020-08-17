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

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder>{

    public Context mContext;
    public List<Post> mPost;

    private FirebaseUser firebaseUser;

    public PostAdapter(Context mContext, List<Post> mPost){
        this.mContext = mContext;
        this.mPost = mPost;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.post_item, parent, false);
        return new PostAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        Post post = mPost.get(position);
        try {
            Picasso.get().load(post.getImage()).into(ViewHolder.postIv);
        }
        catch (Exception e){
            Picasso.get().load(R.drawable.ic_add_image).into(ViewHolder.postIv);
        }

        if (post.getDescription().equals("")){
            ViewHolder.descriptionTv.setVisiblity(View.GONE);
        } else {
            ViewHolder.descriptionTv.setVisibility(View.VISIBLE);
            ViewHolder.descriptionTv.setText(post.getDescription());
        }

        publisherInfo(ViewHolder.avatarIv, ViewHolder.usernameTv, post.getPublisherID());

    }

    @Override
    public int getItemCount() {
        //returns the posts list size
        return mPost.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        //layout view holder

        //Views
        public ImageView avatarIv, postIv, likeIv, commentIv, shareIv;
        public TextView usernameTv, likesTv, descriptionTv, commentsTv;

        public ViewHolder(@NonNull View itemView){
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
        }
    }

    private void publisherInfo(final ImageView avatarIv, final TextView usernameTv, String uid){
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

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
