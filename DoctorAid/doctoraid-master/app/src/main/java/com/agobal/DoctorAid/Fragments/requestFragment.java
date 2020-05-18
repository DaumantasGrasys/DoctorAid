package com.agobal.DoctorAid.Fragments;


import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;

import com.agobal.DoctorAid.Entities.HelpRequest;
import com.agobal.DoctorAid.MainActivity;
import com.agobal.DoctorAid.R;
import com.agobal.DoctorAid.Requests.NewRequest;
import com.agobal.DoctorAid.helper.AsyncTaskCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

import cn.pedant.SweetAlert.SweetAlertDialog;


/**
 * A simple {@link Fragment} subclass.
 */
public class requestFragment extends Fragment implements AsyncTaskCompleteListener {


    private DatabaseReference mUserRef;
    private DatabaseReference mCurrentRequest;
    private String current_uid;
    private String mCurrentRequestKey;
    FirebaseUser mCurrentUser;



    public requestFragment() {
        // Required empty public constructor
    }

    @Override
    public void onStart() {
        super.onStart();


    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_request, container, false);
        //SweetAlertDialog pDialog;
        //ProgressBar spinner = v.findViewById(R.id.progressBar1);

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        current_uid = Objects.requireNonNull(mCurrentUser).getUid();
        mUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);


        mUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);
        mCurrentRequest = FirebaseDatabase.getInstance().getReference().child("Requests").child(current_uid);





        Button btnHelp = v.findViewById(R.id.btnHelp);

        btnHelp.setOnClickListener(view -> {
            Intent intent = new Intent(getActivity(), NewRequest.class);
            startActivity(intent);
        });

        setHasOptionsMenu(true);

        return v;
    }




    @Override
    public void onTaskComplete() {
        //pDialog.dismissWithAnimation();

    }

}
