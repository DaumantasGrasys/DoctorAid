package com.agobal.DoctorAid.adapters;


import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.agobal.DoctorAid.Entities.Messages;
import com.agobal.DoctorAid.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder>{

    private static final String TAG = "MsgAdapterActivity";


    private final List<Messages> mMessageList;

    public MessageAdapter(List<Messages> mMessageList) {

        this.mMessageList = mMessageList;

    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message_single_layout ,parent, false);

        return new MessageViewHolder(v);

    }

    class MessageViewHolder extends RecyclerView.ViewHolder {

        final TextView messageText;
        final CircleImageView profileImage;
        final TextView displayName;
        final ImageView messageImage;

        MessageViewHolder(View view) {
            super(view);

            messageText = view.findViewById(R.id.message_text_layout);
            profileImage = view.findViewById(R.id.message_profile_layout);
            displayName = view.findViewById(R.id.name_text_layout);
            messageImage = view.findViewById(R.id.message_image_layout);


        }
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder viewHolder, int i) {

        Messages msg = mMessageList.get(i);

        String from_user = msg.getFrom();
        String message_type = msg.getType();


        DatabaseReference mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(from_user);

        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                String name = Objects.requireNonNull(dataSnapshot.child("userName").getValue()).toString();
                String image = Objects.requireNonNull(dataSnapshot.child("image").getValue()).toString();

                viewHolder.displayName.setText(name);

                Picasso.get().load(image)
                        .placeholder(R.drawable.unknown_profile_pic)
                        .into(viewHolder.profileImage);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        if(message_type.equals("text")) {

            viewHolder.messageText.setText(msg.getMessage());
            viewHolder.messageImage.setVisibility(View.INVISIBLE);

        }
        else {

            String imageURL = msg.getMessage();
            viewHolder.messageText.setVisibility(View.INVISIBLE);
            viewHolder.messageImage.setVisibility(View.VISIBLE);

            Picasso.get().load(imageURL)
                    .error(R.drawable.error_circle)
                    .resize(800,1200)
                    .rotate(90)
                    //.centerCrop()
                    .placeholder(R.drawable.ic_image_black_24dp)
                    .into(viewHolder.messageImage);
        }
    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }
}

