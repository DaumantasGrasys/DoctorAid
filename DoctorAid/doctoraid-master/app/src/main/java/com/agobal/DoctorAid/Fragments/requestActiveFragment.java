package com.agobal.DoctorAid.Fragments;


import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;

import com.agobal.DoctorAid.MainActivity;
import com.agobal.DoctorAid.R;
import com.agobal.DoctorAid.Requests.ActiveRequest;
import com.agobal.DoctorAid.Requests.NewRequest;
import com.agobal.DoctorAid.helper.AsyncTaskCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import cn.pedant.SweetAlert.SweetAlertDialog;


/**
 * A simple {@link Fragment} subclass.
 */
public class requestActiveFragment extends Fragment implements AsyncTaskCompleteListener {

    private DatabaseReference mUserRef;
    private DatabaseReference mCurrentRequest;
    private DatabaseReference mRequestHistory;
    private String current_uid;
    private String mCurrentRequestKey;
    FirebaseUser mCurrentUser;
    private Object mCurrentRequestValue;



    public requestActiveFragment() {
        // Required empty public constructor
    }

    @Override
    public void onStart() {
        super.onStart();


    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_request_active, container, false);

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        current_uid = Objects.requireNonNull(mCurrentUser).getUid();
        mUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);


        mUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);
        mCurrentRequest = FirebaseDatabase.getInstance().getReference().child("Requests").child(current_uid);
        mRequestHistory = FirebaseDatabase.getInstance().getReference().child("RequestsHistory").child(current_uid);

//            pDialog = new SweetAlertDialog(Objects.requireNonNull(getContext()), SweetAlertDialog.PROGRESS_TYPE);
//            pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
//            pDialog.setTitleText("Prašome palaukti");
//            pDialog.setCancelable(true);
//            pDialog.show();
       // spinner.setVisibility(View.VISIBLE);

        mCurrentRequest.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                    //HelpRequest request = snapshot.getValue(HelpRequest.class);
                if(!dataSnapshot.hasChildren()){



                }
                else {

                    String isRequestActive = dataSnapshot.child("active").getValue().toString();
                    String isRequestTaken = dataSnapshot.child("taken").getValue().toString();

                    mCurrentRequestValue = dataSnapshot.getValue();

                    if (isRequestActive.equals("1") && isRequestTaken.equals("1")) {

                        String requestTakenBy = dataSnapshot.child("takenBy").getValue().toString();

                        SweetAlertDialog pDialog = new SweetAlertDialog(v.getContext(), SweetAlertDialog.SUCCESS_TYPE);
                        pDialog.getProgressHelper().setBarColor(Color.parseColor("#3498DB"));
                        pDialog.setTitleText("Gydytojas rastas");
                        pDialog.setConfirmClickListener(sweetAlertDialog -> {
                            Intent intent = new Intent(getActivity(), ActiveRequest.class);
                            intent.putExtra("takenBy", requestTakenBy);
                            startActivity(intent);
                        });
                        pDialog.show();

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });


//        SweetAlertDialog pDialog = new SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE);
//        pDialog.setTitleText("Pavyko!");
//        pDialog.setContentText("Užklausa sukurta.");
//        pDialog.setConfirmClickListener(sweetAlertDialog -> {
//            pDialog.hide();
//            SweetAlertDialog pDialog2 = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
//            pDialog2.getProgressHelper().setBarColor(Color.parseColor("#3498DB"));
//            pDialog2.setTitleText("Ieškoma gydytojo...");
//            pDialog2.setCancelable(true);
//            pDialog2.showCancelButton(true);
//            pDialog2.setCancelText("Grįžti");
//            pDialog2.show();
//            pDialog2.setCancelClickListener(SweetAlertDialog -> {
//                        Intent intent = new Intent(NewRequest.this, MainActivity.class);
//                        startActivity(intent);
//                    }
//            );
//        });
//        pDialog.show();




        Button btnHelp = v.findViewById(R.id.btnCancel);

        btnHelp.setOnClickListener(view -> {
            CancelRequest();

//            String key = FirebaseDatabase.getInstance().getReference("RequestsHistory").child(current_uid).push().getKey();
//
//            DatabaseReference mRequestHistory = FirebaseDatabase.getInstance().getReference().child("RequestsHistory").child(current_uid);
//            mRequestHistory.child(key).setValue(mCurrentRequestValue);
//            mRequestHistory.child(key).child("active").setValue(0);
//            mRequestHistory.child(key).child("canceled").setValue(1);
//            mRequestHistory.child(key).child("dateCanceled").setValue(System.currentTimeMillis());
//
//            mCurrentRequest.removeValue();


            SweetAlertDialog pDialogc = new SweetAlertDialog(getActivity(), SweetAlertDialog.SUCCESS_TYPE);
            pDialogc.setTitleText("re");
            pDialogc.setContentText("Užklausa atšaukta.");
            pDialogc.setConfirmClickListener(sweetAlertDialog -> {
                //sweetAlertDialog.dismissWithAnimation();
                Intent intent = new Intent(getActivity(), MainActivity.class);
                startActivity(intent);
                });
            pDialogc.show();
        });

        setHasOptionsMenu(true);

        return v;
    }


    public void CancelRequest(){


        String key = mRequestHistory.push().getKey();

        mRequestHistory.child(key).setValue(mCurrentRequestValue);
        mRequestHistory.child(key).child("active").setValue(0);
        mRequestHistory.child(key).child("canceled").setValue(1);
        mRequestHistory.child(key).child("dateCanceled").setValue(System.currentTimeMillis());

        //mCurrentRequest.removeValue();
    }


    @Override
    public void onTaskComplete() {
        //pDialog.dismissWithAnimation();

    }

}
