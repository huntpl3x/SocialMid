package com.roichomsky.socialmid;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

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

    public CommentAdapter(Context mContext, List<Comment> mComment) {
        this.mContext = mContext;
        this.mComment = mComment;
    }

    @NonNull
    @Override
    public CommentAdapter.MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.comment_item, parent, false);
        return new CommentAdapter.MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentAdapter.MyHolder holder, int position) {
        final Comment comment = mComment.get(position);
        publisherInfo(holder.avatarIv, holder.usernameTv, comment.getPublisherID());
        holder.commentTv.setText(comment.getContent());
    }

    @Override
    public int getItemCount() {
        return mComment.size();
    }

    class MyHolder extends RecyclerView.ViewHolder{

        //Views - layout view holder
        ImageView avatarIv;
        TextView usernameTv, commentTv;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            //init
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
