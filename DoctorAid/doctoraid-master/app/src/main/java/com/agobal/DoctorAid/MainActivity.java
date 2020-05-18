package com.agobal.DoctorAid;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.app.ActivityCompat;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.agobal.DoctorAid.AccountActivity.LoginActivity;
import com.agobal.DoctorAid.Fragments.MessagesFragment;
import com.agobal.DoctorAid.Fragments.ProfileFragment;
import com.agobal.DoctorAid.Fragments.doctorFragment;
import com.agobal.DoctorAid.Fragments.requestActiveFragment;
import com.agobal.DoctorAid.Fragments.requestFragment;
import com.agobal.DoctorAid.Requests.ActiveRequest;
import com.agobal.DoctorAid.helper.BottomNavigationBehavior;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.perf.FirebasePerformance;
import com.google.firebase.perf.metrics.Trace;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.core.app.ActivityCompat;
import android.Manifest;
import android.content.Context;
import android.location.LocationManager;

import java.util.Objects;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "MainActivity";

    private FirebaseAuth auth;
    private DatabaseReference mUserRef;
    private DatabaseReference mCurrentRequest;
    private TextView title;
    FusedLocationProviderClient mFusedLocationClient;
    private String latText;
    private String lonText;
    private String current_uid;
    private boolean isRequestActive = false;

    int PERMISSION_ID = 44;

    private String ClientType;

    FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    public void onStart()
    {
        super.onStart();

        mAuthListener = firebaseAuth -> {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if(user==null){
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
            else
                mUserRef.child("online").setValue("true");
        };
        auth.addAuthStateListener(mAuthListener);

        mUserRef.child("online").setValue("true");

    }

    @Override
    public void onStop() {
        super.onStop();

        mUserRef.child("online").setValue(ServerValue.TIMESTAMP);

        auth.removeAuthStateListener(mAuthListener);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Trace myTrace = FirebasePerformance.getInstance().newTrace("test_trace");
        myTrace.start();

        FirebaseUser mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        auth = FirebaseAuth.getInstance();


        current_uid = Objects.requireNonNull(mCurrentUser).getUid();
        mUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);

        /*
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this , this )
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build();
        */
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        //latTextView = findViewById(R.id.latTextView);
        //lonTextView = findViewById(R.id.lonTextView);

        getLastLocation();

        Objects.requireNonNull(getSupportActionBar()).setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.action_bar_main);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        title = findViewById(getResources().getIdentifier("action_bar_title", "id", getPackageName()));

        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        // attaching bottom sheet behaviour - hide / show on scroll
        CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) navigation.getLayoutParams();
        layoutParams.setBehavior(new BottomNavigationBehavior());

        title.setText("DoctorAid");

        mUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    if (dataSnapshot.child("type").getValue().equals("Gydytojas")) {

                        ClientType = dataSnapshot.child("type").getValue().toString();

                        loadFragment(new doctorFragment());
                        myTrace.stop();

                    } else if (dataSnapshot.child("type").getValue().equals("Klientas")) {

                        ClientType = dataSnapshot.child("type").getValue().toString();
                        CheckIfActiveRequest();
                        loadFragment(new requestFragment());
                        myTrace.stop();
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        title.setText("DoctorAid");


    }




    private final BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment fragment;
            switch (item.getItemId()) {
                case R.id.navigation_kvietimas:
                    title.setText("Pagalba");
                    if (ClientType.equals("Gydytojas")) {
                        fragment = new doctorFragment();
                        loadFragment(fragment);
                    }
                    else if (ClientType.equals("Klientas")) {

                        CheckIfActiveRequest();

                        fragment = new requestFragment();
                        loadFragment(fragment);

                    }

                    return true;
                case R.id.navigation_pranesimai:
                    title.setText("Pranešimai");
                    fragment = new MessagesFragment();
                    loadFragment(fragment);
                    return true;
                case R.id.navigation_profilis:
                    title.setText("Profilis");
                    fragment = new ProfileFragment();
                    loadFragment(fragment);
                    return true;
            }

            return false;
        }
    };



    private void loadFragment(Fragment fragment) {
        // load fragment
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void CheckIfActiveRequest(){

        mCurrentRequest = FirebaseDatabase.getInstance().getReference().child("Requests").child(current_uid);

        mCurrentRequest.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    Object test =  dataSnapshot.child("active").getValue();

                    String RequestActiveVal = dataSnapshot.child("active").getValue() == null ? "0" : dataSnapshot.child("active").getValue().toString();
                    String RequestTakenVal = dataSnapshot.child("taken").getValue() == null ? "0" : dataSnapshot.child("taken").getValue().toString();

                    if (RequestActiveVal.equals("1") && RequestTakenVal.equals("0")){

                        //String requestKey = snapshot.getKey();

                        //mCurrentRequest.child(requestKey).child("lat")
                        //mCurrentRequest.child(requestKey).child("lon")


                        Fragment fragment;
                        fragment = new requestActiveFragment();
                        loadFragment(fragment);



                    }
                    else if (RequestActiveVal.equals("1") && RequestTakenVal.equals("1")){
                        Intent intent = new Intent(MainActivity.this, ActiveRequest.class);
                        startActivity(intent);

                    }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }


    private void logoutUser() {

        FirebaseAuth.getInstance().signOut();
        auth.signOut();

        //Auth.GoogleSignInApi.signOut(mGoogleApiClient);

        FirebaseUser currentUser = auth.getCurrentUser();

        startActivity(new Intent(MainActivity.this, LoginActivity.class));
        finish();
        //sendToStart();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.logOut:
                logoutUser();
            case R.id.PrivatePolicy:
                Intent intent = new Intent(MainActivity.this, PrivacyPolicyActivity.class);
                startActivity(intent);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }

    private boolean checkPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        return false;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                PERMISSION_ID
        );
    }

    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER
        );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_ID) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Granted. Start getting the location information
            }
        }
    }


    @SuppressLint("MissingPermission")
    private void getLastLocation(){
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                mFusedLocationClient.getLastLocation().addOnCompleteListener(
                        new OnCompleteListener<Location>() {
                            @Override
                            public void onComplete(@NonNull Task<Location> task) {
                                Location location = task.getResult();
                                if (location == null) {
                                    requestNewLocationData();
                                } else {
                                    mUserRef.child("lat").setValue(location.getLatitude()+"");
                                    mUserRef.child("lon").setValue(location.getLongitude()+"");
                                }
                            }
                        }
                );
            } else {
                Toast.makeText(this, "Turn on location", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        } else {
            requestPermissions();
        }
    }

    @SuppressLint("MissingPermission")
    private void requestNewLocationData(){

        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(0);
        mLocationRequest.setFastestInterval(0);
        mLocationRequest.setNumUpdates(1);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient.requestLocationUpdates(
                mLocationRequest, mLocationCallback,
                Looper.myLooper()
        );

    }

    private LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            Location mLastLocation = locationResult.getLastLocation();
            latText = mLastLocation.getLatitude()+"";
            lonText = mLastLocation.getLongitude()+"";
        }
    };


}
