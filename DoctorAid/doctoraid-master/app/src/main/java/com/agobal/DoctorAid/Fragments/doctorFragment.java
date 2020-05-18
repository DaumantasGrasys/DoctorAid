package com.agobal.DoctorAid.Fragments;


import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.agobal.DoctorAid.R;
import com.agobal.DoctorAid.Requests.ActiveRequestDoctor;
import com.agobal.DoctorAid.helper.AsyncTaskCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import cn.pedant.SweetAlert.SweetAlertDialog;


/**
 * A simple {@link Fragment} subclass.
 */
public class doctorFragment extends Fragment implements AsyncTaskCompleteListener {


    private String current_uid;
    FirebaseUser mCurrentUser;
    private DatabaseReference mUserRef;
    private DatabaseReference mRequests;
    private DatabaseReference mUsers;

    private String latitude;
    private String Longitude;
    private String Test;


    public doctorFragment() {
        // Required empty public constructor
    }

    @Override
    public void onStart() {
        super.onStart();


    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_doctor, container, false);

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        current_uid = Objects.requireNonNull(mCurrentUser).getUid();
        mUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);
        mUsers = FirebaseDatabase.getInstance().getReference().child("Users");

        mUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                latitude = dataSnapshot.child("lat").getValue().toString();
                Longitude = dataSnapshot.child("lon").getValue().toString();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });



        mRequests = FirebaseDatabase.getInstance().getReference().child("Requests");


        mRequests.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                ArrayList<String> userIds = new ArrayList<String>();


                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                    userIds.add(snapshot.getKey());
                    String userId = snapshot.getKey();

                    if (snapshot.child("taken").getValue().toString().equals("1") && snapshot.child("takenBy").getValue().toString().equals(current_uid)){
                        Intent intent = new Intent(getActivity(), ActiveRequestDoctor.class);
                        intent.putExtra("requester", userId);
                        startActivity(intent);
                        break;
                    }
                    else if (snapshot.child("taken").getValue().toString().equals("1"))
                        continue;



                    mUsers.orderByKey().equalTo(userId).addValueEventListener(
                            new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot userDataSnapshot) {

                                    String requestLat = userDataSnapshot.child(userId).child("lat").getValue().toString();
                                    String requestLon = userDataSnapshot.child(userId).child("lon").getValue().toString();

                                    Location loc1 = new Location("");
                                    loc1.setLatitude(Double.parseDouble(requestLat));
                                    loc1.setLongitude(Double.parseDouble(requestLon));

                                    Location loc2 = new Location("");
                                    loc2.setLatitude(Double.parseDouble(latitude));
                                    loc2.setLongitude(Double.parseDouble(Longitude));

                                    float distanceInMeters = loc1.distanceTo(loc2);

                                    if (distanceInMeters <= 5000){

                                        mUsers.removeEventListener(this);

                                        Fragment newFrag= new doctorActiveFragment();
                                        Bundle args = new Bundle();
                                        args.putString("userid", userId);
                                        newFrag.setArguments(args);
                                        loadFragment(newFrag);
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            }
                    );
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
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
