package com.agobal.DoctorAid.AccountActivity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.agobal.DoctorAid.MainActivity;
import com.agobal.DoctorAid.R;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {

    private static final String TAG = "LoginActivity";
    private static final int RC_SIGN_IN = 9001;

    FirebaseAuth auth;
    FirebaseAuth.AuthStateListener mAuthListener;
    ProgressBar spinner;

    @Override
    protected void onStart() {
        super.onStart();

        auth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        auth.removeAuthStateListener(mAuthListener);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // remove title
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() != null) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        }


        mAuthListener = new FirebaseAuth.AuthStateListener(){
            @Override
            public  void  onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth){
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if(user!=null){
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }

        };

        setContentView(R.layout.activity_login);

        spinner = findViewById(R.id.progressBar);
        spinner.setVisibility(View.GONE);


        final EditText inputEmail = findViewById(R.id.p_email);
        final EditText inputPassword = findViewById(R.id.password);
        Button btnLogin = findViewById(R.id.btnLogin);
        //Button btnLinkToRegister = findViewById(R.id.btnLinkToRegisterScreen);
        TextView btn_reset_password = findViewById(R.id.btn_reset_password);
        TextView linkToRegister = findViewById(R.id.linkToRegister);

        // Login button Click Event
        btnLogin.setOnClickListener(view -> {
            String email = inputEmail.getText().toString().trim();
            String password = inputPassword.getText().toString().trim();
            login(email, password);
        });

        // Link to Register Screen
        linkToRegister.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(),
                    RegisterActivity.class);
            startActivity(i);
            finish();
        });

        //link to reset password
        btn_reset_password.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), ResetPasswordActivity.class);
            startActivity(i);
            finish();
        });
    }

    private void login(String email, String password) {

        if (!email.isEmpty() && !password.isEmpty()) {
            //firebase/*
//            SweetAlertDialog pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
//            pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
//            pDialog.setTitleText("Prašome palaukti...");
//            pDialog.setCancelable(false);
//            pDialog.show();
            spinner.setVisibility(View.VISIBLE);


            auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(LoginActivity.this, task -> {
                        if (!task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), "Prisijungti nepavyko, prašome patikrinti įvestus duomenis", Toast.LENGTH_LONG).show();
                            spinner.setVisibility(View.GONE);

                        } else {
                            FirebaseUser mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
                            String current_uid = Objects.requireNonNull(mCurrentUser).getUid();
                            DatabaseReference mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);

                            mUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                    if (dataSnapshot.child("firstName").exists()) {
                                        //isUserDataExist = true;
                                        spinner.setVisibility(View.GONE);

                                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                    else
                                    {
                                        spinner.setVisibility(View.GONE);

                                        startActivity(new Intent(LoginActivity.this, UserDataActivity.class));
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                    });
        } else {
            Toast.makeText(getApplicationContext(), "Prošome užpildyti laukus!", Toast.LENGTH_LONG)
                    .show();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            // case R.id.sign_in_button:
            //signIn();
            // break;
        }
    }


//    private void signIn() {
//        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
//        startActivityForResult(signInIntent, RC_SIGN_IN);
//    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign-In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                // Google Sign-In failed
                Log.e(TAG, "Google Sign-In failed.");
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    // If sign in fails, display a message to the user. If sign in succeeds
                    // the auth state listener will be notified and logic to handle the
                    // signed in user can be handled in the listener.
                    if (!task.isSuccessful()) {
                        Log.w(TAG, "signInWithCredential", task.getException());
                        Toast.makeText(LoginActivity.this, "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    }
                });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
// An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }
}
