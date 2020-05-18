package com.agobal.DoctorAid.Fragments;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.agobal.DoctorAid.Chat.Chat_activity;
import com.agobal.DoctorAid.Entities.Conversation;
import com.agobal.DoctorAid.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessagesFragment extends Fragment {

    private static final String TAG = "MessageFragment";

    private DatabaseReference mConvDatabase;
    private DatabaseReference mMessageDatabase;
    private DatabaseReference mUsersDatabase;

    private RecyclerView mConvList;
    private FirebaseRecyclerAdapter<Conversation, ConvViewHolder> firebaseConvAdapter;

    private TextView tvEmpty;
    //ProgressBar spinner;


    public MessagesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_messages, container, false);

        tvEmpty = v.findViewById(R.id.p_helpCount);
        tvEmpty.setVisibility(View.GONE);
        mConvList = v.findViewById(R.id.conv_list);
        // spinner = v.findViewById(R.id.progressBar);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        String mCurrent_user_id = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();


        mConvDatabase = FirebaseDatabase.getInstance().getReference().child("Chat").child(mCurrent_user_id);

        mConvDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChildren())
                {
                    tvEmpty.setVisibility(View.GONE);
                }
                else
                    tvEmpty.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mConvDatabase.keepSynced(true);
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mMessageDatabase = FirebaseDatabase.getInstance().getReference().child("messages").child(mCurrent_user_id);



        mUsersDatabase.keepSynced(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        mConvList.setHasFixedSize(true);
        mConvList.setLayoutManager(linearLayoutManager);
        mConvList.setLayoutManager(new LinearLayoutManager(getActivity()));

        onActivityStarted();

        // Inflate the layout for this fragment
        return v;
    }

    private void onActivityStarted() {

        Query conversationQuery = mConvDatabase.orderByKey();

        FirebaseRecyclerOptions<Conversation> personsOptions = new FirebaseRecyclerOptions.Builder<Conversation>().setQuery(conversationQuery, Conversation.class).build();

        firebaseConvAdapter = new FirebaseRecyclerAdapter<Conversation, ConvViewHolder>(personsOptions)
        {

            @NonNull
            @Override
            public ConvViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                return new ConvViewHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.users_single_layout, parent, false));
            }

            @Override
            protected void onBindViewHolder(@NonNull ConvViewHolder convViewHolder, int i, @NonNull Conversation conv) {

                final String list_user_id = getRef(i).getKey();

                Query lastMessageQuery = mMessageDatabase.child(Objects.requireNonNull(list_user_id)).limitToLast(1);

                lastMessageQuery.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String s) {

                        String data = Objects.requireNonNull(dataSnapshot.child("message").getValue()).toString();
                        convViewHolder.setMessage(data, conv.isSeen());


                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


                mUsersDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        final String userName = Objects.requireNonNull(dataSnapshot.child("userName").getValue()).toString();
                        String userThumb = Objects.requireNonNull(dataSnapshot.child("image").getValue()).toString();

                        if(dataSnapshot.hasChild("online")) {

                            String userOnline = Objects.requireNonNull(dataSnapshot.child("online").getValue()).toString();
                            convViewHolder.setUserOnline(userOnline);
                        }

                        convViewHolder.setName(userName);
                        convViewHolder.setUserImage(userThumb);

                        convViewHolder.mView.setOnClickListener(view -> {

                            Intent chatIntent = new Intent(getContext(), Chat_activity.class);
                            chatIntent.putExtra("user_id", list_user_id);
                            chatIntent.putExtra("user_name", userName);
                            startActivity(chatIntent);
                        });

                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }
        };

        mConvList.addItemDecoration(new DividerItemDecoration(mConvList.getContext(), DividerItemDecoration.VERTICAL));

        mConvList.setAdapter(firebaseConvAdapter);

    }

    @Override
    public void onStop() {
        super.onStop();
        firebaseConvAdapter.stopListening();
    }

    @Override
    public void onStart() {
        super.onStart();

        firebaseConvAdapter.startListening();
    }

    public class ConvViewHolder extends RecyclerView.ViewHolder {


        final View mView;

        ConvViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
        }

        void setMessage(String message, boolean isSeen){

            TextView userStatusView = mView.findViewById(R.id.users_single_status);
            userStatusView.setText(message);

            if(!isSeen){
                userStatusView.setTypeface(userStatusView.getTypeface(), Typeface.BOLD);
            } else {
                userStatusView.setTypeface(userStatusView.getTypeface(), Typeface.NORMAL);
            }

        }

        void setName(String name){

            TextView userNameView = mView.findViewById(R.id.user_single_name);
            userNameView.setText(name);

        }

        void setUserImage(String thumb_image){

//            SweetAlertDialog pDialog = new SweetAlertDialog(Objects.requireNonNull(getContext()), SweetAlertDialog.PROGRESS_TYPE);
//            pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
//            pDialog.setTitleText("Prašome palaukti");
//            pDialog.setCancelable(true);
//            pDialog.show();
            //spinner.setVisibility(View.VISIBLE);


            CircleImageView userImageView = mView.findViewById(R.id.user_single_image);
            Picasso.get()
                    .load(thumb_image)
                    .placeholder(R.drawable.unknown_profile_pic)
                    .into(userImageView, new Callback() {
                        @Override
                        public void onSuccess() {
                            //spinner.setVisibility(View.GONE);

                        }

                        @Override
                        public void onError(Exception e) {
                            //spinner.setVisibility(View.GONE);
                        }
                    });

        }

        void setUserOnline(String online_status) {

            ImageView userOnlineView = mView.findViewById(R.id.user_single_online_icon);

            if(online_status.equals("true")){

                userOnlineView.setVisibility(View.VISIBLE);

            } else {

                userOnlineView.setVisibility(View.INVISIBLE);
            }
        }
    }
}
