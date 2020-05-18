package com.agobal.DoctorAid.AccountActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.agobal.DoctorAid.MainActivity;
import com.agobal.DoctorAid.R;
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

public class RegisterActivity extends Activity {

    private static final String TAG = "RegisterActivity";


    private EditText inputUserName;
    private EditText inputEmail;
    private EditText inputPassword;
    private EditText inputPassword2;
    private TextView linkToLogin;
    private RadioGroup radioGroup;
    private RadioButton radioButton;
    private FirebaseAuth auth;
    ProgressBar spinner;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // remove title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_register);

        inputUserName = findViewById(R.id.userName);
        inputEmail = findViewById(R.id.p_email);
        inputPassword = findViewById(R.id.password);
        inputPassword2 = findViewById(R.id.password2);
        Button btnRegister = findViewById(R.id.btnRegister);
        spinner = findViewById(R.id.progressBar);
        linkToLogin = findViewById(R.id.linkToLogin);
        radioGroup = findViewById(R.id.radio);


        auth = FirebaseAuth.getInstance();

        spinner.setVisibility(View.GONE);

        // Register Button Click event
        btnRegister.setOnClickListener(view -> {

            final String userName = inputUserName.getText().toString().trim();
            final String email = inputEmail.getText().toString().trim();
            String password = inputPassword.getText().toString().trim();
            String password2 = inputPassword2.getText().toString().trim();
            int selectedId = radioGroup.getCheckedRadioButtonId();
            radioButton = (RadioButton) findViewById(selectedId);
            String radioButtonText =(String) radioButton.getText();
            checkRegistrationData(userName, email, password, password2, radioButtonText);
        });

        // Link to Login Screen
        linkToLogin.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(),
                    LoginActivity.class);
            startActivity(i);
            finish();
        });
    }

    private void checkRegistrationData(String userName, String email, String password, String password2, String radioButtonText)
    {
        if(TextUtils.isEmpty(userName)) {
            inputUserName.setError("Šis laukas yra privalomas!");
            return;
        }

        if(!userName.matches("[a-zA-Z0-9.? ]*")) { // PauliusII
            inputUserName.setError("Netinka specialūs simboliai!");
            return;
        }

        if(TextUtils.isEmpty(email)) {
            inputEmail.setError("Šis laukas yra privalomas!");
            return;
        }

        if (!isValidEmail(inputEmail.getText().toString())) {
            inputEmail.setError("Įveskite teisingą el. pašto adresą!");
            return;
        }

        if(TextUtils.isEmpty(password)) {
            inputPassword.setError("Šis laukas yra privalomas!");
            return;
        }

        if(TextUtils.isEmpty(password2)) {
            inputPassword2.setError("Šis laukas yra privalomas!");
            return;
        }

        if  (!password.matches(password2)) {
            inputPassword2.setError("Slaptažodžiai nesutampa!");
            return;
        }

        if (password.length()<6){
            inputPassword2.setError("Slaptažodis per trumpas!");
        }
        else
            registerUser(userName, email, password, radioButtonText);
    }

    private void registerUser(final String userName, final String email, final String password, final String radioButtonText) {

        spinner.setVisibility(View.VISIBLE);

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(RegisterActivity.this, task -> {

                    if (!task.isSuccessful()) {
                        Toast.makeText(RegisterActivity.this, "Registuojantis įvyko klaida!" ,Toast.LENGTH_SHORT).show();
                        spinner.setVisibility(View.GONE);
                    }
                    else
                    {
                        Toast.makeText(RegisterActivity.this, "Registracija sėkminga!", Toast.LENGTH_SHORT).show();

                        FirebaseDatabase  database = FirebaseDatabase.getInstance();
                        DatabaseReference mDatabaseRef = database.getReference("Users");

                        Map<String, Object> Updates = new HashMap<>();
                        Updates.put(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()+ "/userName", userName);
                        Updates.put(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()+ "/email", email);
                        Updates.put(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()+ "/image", "default");
                        Updates.put(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()+ "/thumb_image", "default");
                        Updates.put(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()+ "/type", radioButtonText);

                        mDatabaseRef.updateChildren(Updates);

                        FirebaseUser mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
                        String current_uid = Objects.requireNonNull(mCurrentUser).getUid();
                        DatabaseReference mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);

                        spinner.setVisibility(View.GONE);

                        mUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                if (dataSnapshot.child("firstName").exists()) {
                                    //isUserDataExist = true;
                                    Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                                else
                                {
                                    startActivity(new Intent(RegisterActivity.this, UserDataActivity.class));
                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                });

    }
    private static boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    public void onBackPressed() {
        Intent i = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(i);
        finish();
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