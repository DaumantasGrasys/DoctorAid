package com.agobal.DoctorAid.AccountActivity;

import android.app.Activity;
import android.content.Intent;
import android.location.Address;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.agobal.DoctorAid.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class UserDataActivity extends Activity{

    private static final String TAG = "UserDataActivity";


    private EditText inputName;
    private EditText inputLastName;
    private EditText inputAddress;
    private Boolean isDataCorrect = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // remove title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_user_data);

        inputName = findViewById(R.id.inputName);
        inputLastName = findViewById(R.id.inputLastName);
        inputAddress = findViewById(R.id.inputLastName);
        Button btnNext = findViewById(R.id.btnNext);

        btnNext.setOnClickListener(view -> {

                    String FirstName = inputName.getText().toString();
                    String LastName = inputLastName.getText().toString();
                    String Address = inputAddress.getText().toString();

                    checkUserData(FirstName, LastName, Address);
                    if(isDataCorrect)
                    {
                        storeUserData(FirstName, LastName, Address);
                        // Launch main activity
                        Intent intent = new Intent(UserDataActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }
        );
    }


    private void checkUserData(String FirstName, String LastName, String Address) {

        if(TextUtils.isEmpty(FirstName)) {
            inputName.setError("Užpildykite visus laukus!");
            return;
        }

        if(TextUtils.isEmpty(LastName)) {
            inputLastName.setError("Užpildykite visus laukus!");
            return;
        }

        if(TextUtils.isEmpty(Address)) {
            inputLastName.setError("Užpildykite visus laukus!");
            return;
        }


        if(!FirstName.matches("[a-zA-Z.? ]*") || !LastName.matches("[a-zA-Z.? ]*"))
        {
            Toast.makeText(getApplicationContext(),
                    "Netinka specialūs simboliai!", Toast.LENGTH_LONG).show();
            isDataCorrect =false;
        }
        else
            isDataCorrect=true;

    }

    private void storeUserData(String FirstName, String LastName, String Address) {
        FirebaseDatabase  database = FirebaseDatabase.getInstance();
        DatabaseReference mDatabaseRef = database.getReference("Users");

        Map<String, Object> hopperUpdates = new HashMap<>();

        hopperUpdates.put(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid() + "/firstName", FirstName);
        hopperUpdates.put(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid() + "/lastName", LastName);
        hopperUpdates.put(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid() + "/address", Address);

        mDatabaseRef.updateChildren(hopperUpdates);
    }

    @Override
    public void onBackPressed() {
        Toast.makeText(getApplicationContext(),
                "Turite užpildyti duomenis!", Toast.LENGTH_LONG).show();
    }
}

