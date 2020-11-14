package com.roichomsky.socialmid;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ChatAdapter extends RecyclerView.Adapter{

    private static final int VIEW_TYPE_MESSAGE_SENT = 1;
    private static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;

    public Context mContext;
    public List<Message> mMessage;

    public ChatAdapter(Context context , List<Message> message){
        this.mContext = context;
        this.mMessage = message;
    }

    @Override
    public int getItemViewType(int position) {
        Message message = mMessage.get(position);
        if (message.getSender().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
            // If the current user is the sender of the message
            return VIEW_TYPE_MESSAGE_SENT;
        } else {
            // If some other user sent the message
            return VIEW_TYPE_MESSAGE_RECEIVED;
        }
    }

    @Override
    public int getItemCount() {
        return mMessage.size();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;

        if (viewType == VIEW_TYPE_MESSAGE_SENT) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_sent, parent, false);
            return new SentHolder(view);
        } else if (viewType == VIEW_TYPE_MESSAGE_RECEIVED) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_received, parent, false);
            return new ReceivedHolder(view);
        }

        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = mMessage.get(position);

        switch (holder.getItemViewType()) {
            case VIEW_TYPE_MESSAGE_SENT:
                ((SentHolder) holder).bind(message);
                break;
            case VIEW_TYPE_MESSAGE_RECEIVED:
                ((ReceivedHolder) holder).bind(message);
        }
    }

    public class ReceivedHolder extends RecyclerView.ViewHolder{

        TextView timestampTv, messageTv;

        public ReceivedHolder(@NonNull View itemView) {
            super(itemView);
            messageTv = itemView.findViewById(R.id.text_message_body);
            timestampTv = itemView.findViewById(R.id.text_message_time);
        }

        void bind(Message message) {
            messageTv.setText(message.getMessage());

            // Format the stored timestamp into a readable String using method.
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(Long.parseLong(message.getTimestamp()));
            Date d = c.getTime();
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
            timestampTv.setText(sdf.format(d));
        }

    }
    public class SentHolder extends RecyclerView.ViewHolder{

        TextView timestampTv, messageTv, isSeenTv;

        public SentHolder(@NonNull View itemView) {
            super(itemView);
            messageTv = itemView.findViewById(R.id.text_message_body);
            timestampTv = itemView.findViewById(R.id.text_message_time);
            isSeenTv = itemView.findViewById(R.id.text_message_seen);
        }

        void bind(Message message) {
            messageTv.setText(message.getMessage());

            System.out.println(message.getSeen());

            if(message.getSeen().equals("true")){
                isSeenTv.setText("seen");
                isSeenTv.setTextColor(Color.parseColor("#0066ff"));
            }

            // Format the stored timestamp into a readable String using method.
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(Long.parseLong(message.getTimestamp()));
            Date d = c.getTime();
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
            timestampTv.setText(sdf.format(d));
        }

    }
}
