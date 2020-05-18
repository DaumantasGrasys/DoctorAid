package com.agobal.DoctorAid.Requests;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.agobal.DoctorAid.Chat.Chat_activity;
import com.agobal.DoctorAid.Entities.UserData;
import com.agobal.DoctorAid.MainActivity;
import com.agobal.DoctorAid.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import cn.pedant.SweetAlert.SweetAlertDialog;


public class ActiveRequestDoctor extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "ActiveRequestDoctorActivity";

    private StorageReference mDatabaseStorage;

    private DatabaseReference mUserRequestsDatabase;
    private DatabaseReference mUserDatabase;
    private DatabaseReference mRequesterDatabase;
    private DatabaseReference mRequestHistory;

    private final FirebaseUser mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
    private final String current_uid = Objects.requireNonNull(mCurrentUser).getUid();

    private EditText etRequestDescription;

    private String firstName;
    private String lastName;
    private String address;
    private String description;

    Object mCurrentRequestValue;

    Marker marker;

    MapView mapView;
    GoogleMap map;


    //private String CheckedMoney;
    //private String CheckedBoth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_active_request_doctor);


        Objects.requireNonNull(getSupportActionBar()).setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.action_bar_main);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        TextView title = findViewById(getResources().getIdentifier("action_bar_title", "id", getPackageName()));
        title.setText("Aktyvi užklausa");

        String requester = getIntent().getStringExtra("requester");

        mDatabaseStorage = FirebaseStorage.getInstance().getReference();
        mUserRequestsDatabase = FirebaseDatabase.getInstance().getReference().child("Requests").child(requester);
        mRequesterDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(requester);
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mRequestHistory = FirebaseDatabase.getInstance().getReference().child("RequestsHistory").child(requester);


        etRequestDescription = findViewById(R.id.inputDescription);

        mapView = findViewById(R.id.ard_map);
        mapView.onCreate(savedInstanceState);

        mapView.getMapAsync(this);


        final TextView ard_firstAndLastName = findViewById(R.id.ard_name);
        final TextView ard_description = findViewById(R.id.ard_description);
        final TextView ard_address = findViewById(R.id.ard_address);


        mRequesterDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                firstName = dataSnapshot.child("firstName").getValue(String.class);
                lastName = dataSnapshot.child("lastName").getValue(String.class);
                address = dataSnapshot.child("address").getValue(String.class);


                UserData userData = new UserData();

                userData.setFirstName(firstName);
                userData.setLastName(lastName);

                userData.setAddress(address);

                ard_firstAndLastName.setText(userData.firstName+" "+userData.lastName);
                ard_address.setText(address);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        mRequesterDatabase.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {



                String lat = dataSnapshot.child("lat").getValue(String.class);
                String lon = dataSnapshot.child("lon").getValue(String.class);

                if(map != null){

                    LatLng Location = new LatLng(Double.valueOf(lat),
                            Double.valueOf(lon));

                    marker.remove();

                    marker = map.addMarker(new MarkerOptions()
                            .position(Location).title("")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))
                            .draggable(false).visible(true));

                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        mUserRequestsDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                mCurrentRequestValue = dataSnapshot.getValue();

                description = dataSnapshot.child("description").getValue(String.class);
                ard_description.setText(description);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        }); ;


        Button btnEnd = findViewById(R.id.btnEnd);
        FloatingActionButton fabChat = findViewById(R.id.ard_message);
        FloatingActionButton fabDelete = findViewById(R.id.ard_cancel);


        btnEnd.setOnClickListener(view -> {


            SweetAlertDialog pDialog = new SweetAlertDialog(this, SweetAlertDialog.NORMAL_TYPE);
            pDialog.setTitleText("");
            pDialog.setContentText("Ar tikrai norite pabaigti užklausą?");
            pDialog.setConfirmText("Taip");
            pDialog.setConfirmClickListener(sweetAlertDialog -> {
                EndRequest();

            });
            pDialog.show();

        });

        fabChat.setOnClickListener(view -> {
            Intent chatIntent = new Intent(this, Chat_activity.class);
            chatIntent.putExtra("user_id", requester);
            chatIntent.putExtra("user_name", firstName + " " + lastName);
            startActivity(chatIntent);
        });

        fabDelete.setOnClickListener(view -> {

            SweetAlertDialog pDialog = new SweetAlertDialog(this, SweetAlertDialog.NORMAL_TYPE);
            pDialog.setTitleText("");
            pDialog.setContentText("Ar tikrai norite atšaukti užklausą?");
            pDialog.setConfirmText("Taip");
            pDialog.setConfirmClickListener(sweetAlertDialog -> {
                CancelRequest();

            });
            pDialog.show();

        });


    }

    public void CancelRequest(){


        String key = mRequestHistory.push().getKey();

        mRequestHistory.child(key).setValue(mCurrentRequestValue);
        mRequestHistory.child(key).child("active").setValue(0);
        mRequestHistory.child(key).child("canceled").setValue(1);
        mRequestHistory.child(key).child("dateCanceled").setValue(System.currentTimeMillis());

        mUserRequestsDatabase.removeValue();

        Intent intent = new Intent(ActiveRequestDoctor.this, MainActivity.class);
        startActivity(intent);
    }

    public void EndRequest(){


        String key = mRequestHistory.push().getKey();

        mRequestHistory.child(key).setValue(mCurrentRequestValue);
        mRequestHistory.child(key).child("active").setValue(0);
        mRequestHistory.child(key).child("completed").setValue(1);
        mRequestHistory.child(key).child("dateCompleted").setValue(System.currentTimeMillis());

        mUserRequestsDatabase.removeValue();

        Intent intent = new Intent(ActiveRequestDoctor.this, MainActivity.class);
        startActivity(intent);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.getUiSettings().setMyLocationButtonEnabled(false);
        map.setMyLocationEnabled(true);

        LatLng Location = new LatLng(37.4219983,
                -122.084);

        marker = map.addMarker(new MarkerOptions()
                .position(Location).title("")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))
                .draggable(false).visible(true));

        map.moveCamera(CameraUpdateFactory.newLatLngZoom(Location, 16));

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

    @Override
    public void onResume() {
        mapView.onResume();
        super.onResume();
    }


    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }


}
