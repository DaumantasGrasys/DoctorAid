package com.agobal.DoctorAid.Fragments;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.agobal.DoctorAid.Chat.Chat_activity;
import com.agobal.DoctorAid.Entities.UserData;
import com.agobal.DoctorAid.MainActivity;
import com.agobal.DoctorAid.R;
import com.agobal.DoctorAid.Requests.ActiveRequest;
import com.agobal.DoctorAid.Requests.ActiveRequestDoctor;
import com.agobal.DoctorAid.Requests.NewRequest;
import com.agobal.DoctorAid.helper.AsyncTaskCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class doctorActiveFragment extends Fragment implements AsyncTaskCompleteListener {


    private String current_uid;
    FirebaseUser mCurrentUser;
    private DatabaseReference mUserRef;
    private DatabaseReference mRequestRef;
    private DatabaseReference mUserRequestRef;

    private String requestUserId;
    private String firstName;
    private String lastName;
    private String address;
    private String description;


    public doctorActiveFragment() {
        // Required empty public constructor
    }

    @Override
    public void onStart() {
        super.onStart();


    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_doctor_active, container, false);

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        requestUserId = getArguments().getString("userid");
        mRequestRef = FirebaseDatabase.getInstance().getReference().child("Requests").child(requestUserId);
        mUserRequestRef = FirebaseDatabase.getInstance().getReference().child("Users").child(requestUserId);

        current_uid = Objects.requireNonNull(mCurrentUser).getUid();
        mUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);




        final TextView Da_RequestFullName = v.findViewById(R.id.ad_firstAndLastName2);
        final TextView Da_RequestDescription = v.findViewById(R.id.ad_description);
        final TextView Da_RequestAddress = v.findViewById(R.id.ad_distance);
        final CircleImageView ProfilePic = v.findViewById(R.id.da_requesterPic);


        mUserRequestRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                firstName = dataSnapshot.child("firstName").getValue(String.class);
                lastName = dataSnapshot.child("lastName").getValue(String.class);
                address = dataSnapshot.child("address").getValue(String.class);

                final String image = dataSnapshot.child("image").getValue(String.class);

                assert image != null;
                if(!image.equals("default")){
                    Picasso.get().load(image).networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.drawable.unknown_profile_pic)
                            .into(ProfilePic, new Callback() {
                                @Override
                                public void onSuccess() {

                                }

                                @Override
                                public void onError(Exception e) {
                                    Picasso.get().load(image).placeholder(R.drawable.unknown_profile_pic).into(ProfilePic);
                                }

                            });

                }



                UserData userData = new UserData();

                userData.setFirstName(firstName);
                userData.setLastName(lastName);
                userData.setAddress(address);


                Da_RequestFullName.setText(userData.firstName+" "+userData.lastName);
                Da_RequestAddress.setText(userData.address);


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });




        mRequestRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                description = dataSnapshot.child("description").getValue(String.class);

                Da_RequestDescription.setText(description);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        }) ;

        Button btnCreate = v.findViewById(R.id.btnGiveHelp);

        btnCreate.setOnClickListener(view -> {


            Map<String, Object> Updates = new HashMap<>();
            Updates.put("taken", 1);
            Updates.put("takenBy", current_uid);

            mRequestRef.updateChildren(Updates);


            Intent intent = new Intent(getActivity(), ActiveRequestDoctor.class);
            intent.putExtra("requester", requestUserId);
            startActivity(intent);
        });

        setHasOptionsMenu(true);

        return v;
    }

    private void loadFragment(Fragment fragment) {

        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.frame_container, fragment, "findThisFragment")
                .addToBackStack(null)
                .commit();
    }



    @Override
    public void onTaskComplete() {
        //pDialog.dismissWithAnimation();

    }

}
