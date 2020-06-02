package com.agobal.DoctorAid.Requests;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
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
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import cn.pedant.SweetAlert.SweetAlertDialog;
import de.hdodenhof.circleimageview.CircleImageView;


public class ActiveRequest extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "ActiveRequestActivity";

    private StorageReference mDatabaseStorage;

    private DatabaseReference mUserRequestsDatabase;
    private DatabaseReference mUserDatabase;
    private DatabaseReference mUserDocorDatabase;

    private final FirebaseUser mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
    private final String current_uid = Objects.requireNonNull(mCurrentUser).getUid();

    private String firstName;
    private String lastName;
    private String address;

    MapView mapView;
    GoogleMap map;

    Marker marker;


    //private EditText etRequestDescription;


    //private String CheckedMoney;
    //private String CheckedBoth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_active_request);

        Objects.requireNonNull(getSupportActionBar()).setDisplayOptions(android.support.v7.app.ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.action_bar_main);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        TextView title = findViewById(getResources().getIdentifier("action_bar_title", "id", getPackageName()));
        title.setText("Aktyvi užklausa");

        String takenBy = getIntent().getStringExtra("takenBy");

        mDatabaseStorage = FirebaseStorage.getInstance().getReference();
        mUserRequestsDatabase = FirebaseDatabase.getInstance().getReference().child("Requests").child(current_uid);
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mUserDocorDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(takenBy);

        mapView = findViewById(R.id.ar_map);
        mapView.onCreate(savedInstanceState);

        mapView.getMapAsync(this);

        final TextView ar_firstAndLastName = findViewById(R.id.ar_name);
        final TextView ar_address = findViewById(R.id.ar_address);


        mUserRequestsDatabase.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                if(!dataSnapshot.hasChildren()){

                    SweetAlertDialog pDialog = new SweetAlertDialog(ActiveRequest.this, SweetAlertDialog.SUCCESS_TYPE);
                    pDialog.getProgressHelper().setBarColor(Color.parseColor("#3498DB"));
                    pDialog.setTitleText("Užklausa baigta");
                    pDialog.setConfirmClickListener(sweetAlertDialog -> {
                        Intent intent = new Intent(ActiveRequest.this, MainActivity.class);
                        startActivity(intent);
                    });
                    pDialog.show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        }) ;

        mUserDocorDatabase.addValueEventListener(new ValueEventListener() {
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

                ar_firstAndLastName.setText("Dr. "+userData.firstName+" "+userData.lastName);
                ar_address.setText(address);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        }) ;

        FloatingActionButton fabChat = findViewById(R.id.ar_message);

        fabChat.setOnClickListener(view -> {
            Intent chatIntent = new Intent(this, Chat_activity.class);
            chatIntent.putExtra("user_id", takenBy);
            chatIntent.putExtra("user_name", firstName + " " + lastName);
            startActivity(chatIntent);
        });


        mUserDocorDatabase.addValueEventListener(new ValueEventListener() {
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
