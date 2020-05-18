package com.agobal.DoctorAid.Chat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.agobal.DoctorAid.Entities.GetTimeAgo;
import com.agobal.DoctorAid.Entities.Messages;
import com.agobal.DoctorAid.R;
import com.agobal.DoctorAid.adapters.MessageAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


import de.hdodenhof.circleimageview.CircleImageView;

public class Chat_activity extends AppCompatActivity {

    private static final String TAG = "ChatAtivity";
    private String mChatUser;
    private DatabaseReference mRootRef; // comment
    private TextView mLastSeenView;
    private CircleImageView mProfileImage;
    private String mCurrentUserId;
    private EditText mChatMessageView;
    private RecyclerView mMessagesList;
    private SwipeRefreshLayout mRefreshLayout;
    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager mLinearLayout;
    private MessageAdapter mAdapter;
    private static final int TOTAL_ITEMS_TO_LOAD = 10;
    private int mCurrentPage = 1;
    private static final int GALLERY_PICK = 1;
    private StorageReference mImageStorage;
    // testing solution
    private int itemPos = 0;
    private String mLastKey = "";
    private String mPrevKey = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_activity);

        Toolbar mChatToolbar = findViewById(R.id.chat_app_bar);
        setSupportActionBar(mChatToolbar);
        ActionBar actionBar = getSupportActionBar();

        Objects.requireNonNull(actionBar).setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        mRootRef = FirebaseDatabase.getInstance().getReference();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

        mChatUser = getIntent().getStringExtra("user_id");
        String userName = getIntent().getStringExtra("user_name");

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        @SuppressLint("InflateParams") View action_bar_view = Objects.requireNonNull(inflater).inflate(R.layout.chat_custom_bar, null);

        actionBar.setCustomView(action_bar_view);

        TextView mTitleView = findViewById(R.id.custom_bar_title);
        mLastSeenView = findViewById(R.id.custom_bar_seen);
        mProfileImage = findViewById(R.id.custom_bar_image);
        // ImageButton mChatAddBtn = findViewById(R.id.chat_add_btn);
        ImageButton mChatSendBtn = findViewById(R.id.chat_send_btn);
        mChatMessageView = findViewById(R.id.chat_message_view);
        //spinner = findViewById(R.id.progressBar);
        mAdapter = new MessageAdapter(messagesList);
        mMessagesList = findViewById(R.id.messages_list);
        mRefreshLayout = findViewById(R.id.message_swipe_layout);
        mLinearLayout = new LinearLayoutManager(this);
        mMessagesList.setHasFixedSize(true);
        mMessagesList.setLayoutManager(mLinearLayout);
        mMessagesList.setAdapter(mAdapter);
        // image storage
        mImageStorage = FirebaseStorage.getInstance().getReference();
        //spinner.setVisibility(View.VISIBLE);

        mRootRef.child("Chat").child(mCurrentUserId).child(mChatUser).child("seen").setValue(true);
        mRootRef.keepSynced(true);
        loadMessages();

        mTitleView.setText(userName);

        mRootRef.child("Users").child(mChatUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String online = Objects.requireNonNull(dataSnapshot.child("online").getValue()).toString();
                String image = Objects.requireNonNull(dataSnapshot.child("image").getValue()).toString();

                if(online.equals("true")) {
                    mLastSeenView.setText("prisijungęs");
                } else {
                    new GetTimeAgo();

                    long lastTime = Long.parseLong(online);

                    String lastSeenTime = GetTimeAgo.getTimeAgo(lastTime);

                    mLastSeenView.setText(lastSeenTime);
                }

                Picasso.get()
                        .load(image)
                        .error(R.drawable.unknown_profile_pic)
                        .into(mProfileImage, new Callback() {
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

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mRootRef.child("Chat").child(mCurrentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.hasChild(mChatUser)){
                    Map<String, Object> chatAddMap = new HashMap<>();
                    chatAddMap.put("seen",false);
                    chatAddMap.put("timestamp", ServerValue.TIMESTAMP);


                    Map<String, Object> chatUserMap = new HashMap<>();
                    chatUserMap.put("Chat/"+mCurrentUserId+"/"+ mChatUser,chatAddMap);
                    chatUserMap.put("Chat/"+mChatUser+"/"+mCurrentUserId,chatAddMap);

                    mRootRef.updateChildren(chatUserMap, (databaseError, databaseReference) -> {
                        if(databaseError!=null){
                            Log.d(TAG,"CHAT_LOG" + databaseError.getMessage());
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        mChatSendBtn.setOnClickListener(view -> sendMessage());

        /*
        // IMG SEND
        mChatAddBtn.setOnClickListener(view -> {
            Intent galleryIntent = new Intent();
            galleryIntent.setType("image/*");
            galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

            startActivityForResult(Intent.createChooser(galleryIntent, "Pasirinkite nuotrauką"), GALLERY_PICK);
        });
*/

        mRefreshLayout.setOnRefreshListener(() -> {
            mCurrentPage++;
            itemPos = 0;

            loadMoreMessages();
        });

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_PICK && resultCode == RESULT_OK){

            Uri imageUri = data.getData();

            final String current_user_ref = "messages/" + mCurrentUserId + "/" + mChatUser;
            final String chat_user_ref = "messages/" + mChatUser + "/" + mCurrentUserId;

            DatabaseReference user_message_push = mRootRef.child("messages").child(mCurrentUserId).child(mChatUser).push();

            final String push_id = user_message_push.getKey();


            StorageReference filepath = mImageStorage.child("message_images").child( push_id + ".jpg");


            UploadTask uploadTask = filepath.putFile(Objects.requireNonNull(imageUri));

            uploadTask.continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    throw Objects.requireNonNull(task.getException());
                }
                // Continue with the task to get the download URL
                return filepath.getDownloadUrl(); }).addOnCompleteListener(task -> {

                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    String download_url = Objects.requireNonNull(downloadUri).toString();

                    Map<String, Object> messageMap = new HashMap<>();
                    messageMap.put("message", download_url);
                    messageMap.put("seen", false);
                    messageMap.put("type", "image");
                    messageMap.put("time", ServerValue.TIMESTAMP);
                    messageMap.put("from", mCurrentUserId);

                    Map<String, Object> messageUserMap = new HashMap<>();
                    messageUserMap.put(current_user_ref + "/" + push_id, messageMap);
                    messageUserMap.put(chat_user_ref + "/" + push_id, messageMap);

                    mChatMessageView.setText("");

                    mRootRef.updateChildren(messageUserMap, (databaseError, databaseReference) -> {

                        if (databaseError != null) {

                            Log.d(TAG,"CHAT_LOG " + databaseError.getMessage());

                        }

                    });

                } else {
                    Toast.makeText(getApplicationContext(), "KLAIDA CHAT", Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void loadMoreMessages(){
        DatabaseReference messageRef = mRootRef.child("messages").child(mCurrentUserId).child(mChatUser);
        Query messageQuery = messageRef.orderByKey().endAt(mLastKey).limitToLast(10);

        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String s) {

                Messages message = dataSnapshot.getValue(Messages.class);
                String messageKey = dataSnapshot.getKey();

                if(!mPrevKey.equals(messageKey)){
                    messagesList.add(itemPos++, message);
                    mAdapter.notifyDataSetChanged();

                } else {

                    mPrevKey = mLastKey;
                    mAdapter.notifyDataSetChanged();
                }
                if(itemPos == 1) {

                    mLastKey = messageKey;
                    mAdapter.notifyDataSetChanged();
                }

                mAdapter.notifyDataSetChanged();

                mRefreshLayout.setRefreshing(false);

                mLinearLayout.scrollToPositionWithOffset(10,0);
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
    }

    private void loadMessages(){
        DatabaseReference messageRef = mRootRef.child("messages").child(mCurrentUserId).child(mChatUser);

        Query messageQuery = messageRef.limitToLast(mCurrentPage * TOTAL_ITEMS_TO_LOAD);


        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String s) {

                Messages message = dataSnapshot.getValue(Messages.class);

                itemPos++;

                if(itemPos == 1){
                    String messageKey = dataSnapshot.getKey();

                    mLastKey = messageKey;
                    mPrevKey = messageKey;
                }

                messagesList.add(message);
                mAdapter.notifyDataSetChanged();
                mMessagesList.scrollToPosition(messagesList.size()-1);
                mRefreshLayout.setRefreshing(false);
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
    }
    private void sendMessage(){
        String message = mChatMessageView.getText().toString();
        if(!TextUtils.isEmpty(message)){

            String current_user_ref = "messages/" + mCurrentUserId +"/" + mChatUser;
            String chat_user_ref = "messages/" + mChatUser+"/" +mCurrentUserId;

            DatabaseReference user_message_push = mRootRef.child("messages").child(mCurrentUserId).child(mChatUser).push();

            String push_id = user_message_push.getKey();

            Map<String, Object> meessageMap = new HashMap<>();
            meessageMap.put( "message",message);
            meessageMap.put("seen",false);
            meessageMap.put("type","text");
            meessageMap.put("time",ServerValue.TIMESTAMP);
            meessageMap.put("from",mCurrentUserId);

            Map<String, Object> messageUserMap = new HashMap<>();
            messageUserMap.put(current_user_ref+"/"+push_id,meessageMap);
            messageUserMap.put(chat_user_ref+"/" + push_id,meessageMap);

            mChatMessageView.setText("");

            mRootRef.child("Chat").child(mCurrentUserId).child(mChatUser).child("seen").setValue(true);
            mRootRef.child("Chat").child(mCurrentUserId).child(mChatUser).child("timestamp").setValue(ServerValue.TIMESTAMP);

            mRootRef.child("Chat").child(mChatUser).child(mCurrentUserId).child("seen").setValue(false);
            mRootRef.child("Chat").child(mChatUser).child(mCurrentUserId).child("timestamp").setValue(ServerValue.TIMESTAMP);

            mAdapter.notifyDataSetChanged();

            mMessagesList.setAdapter(mAdapter);

            mRootRef.updateChildren(messageUserMap, (databaseError, databaseReference) -> {
                if(databaseError!=null){
                    Log.d(TAG,"CHAT_LOG "+ databaseError.getMessage());
                }
            });
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
