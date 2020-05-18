package com.agobal.DoctorAid.AccountActivity;

import android.app.ActionBar;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.agobal.DoctorAid.Entities.Category;
import com.agobal.DoctorAid.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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

import cn.pedant.SweetAlert.SweetAlertDialog;

public class ProfileEdit extends AppCompatActivity {

    private static final String TAG = "ProfileEditActivity";


    private EditText E_ProfileInputName;
    private EditText E_ProfileInputLastName;
    private EditText E_ProfileInputEmail;
    private EditText E_ProfileInputUsername;
    private EditText E_ProfileInputAddress;

    private final FirebaseUser mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);

        Objects.requireNonNull(getSupportActionBar()).setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.action_bar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        E_ProfileInputName = findViewById(R.id.ProfileInputName);
        E_ProfileInputLastName = findViewById(R.id.ProfileInputLastName);
        E_ProfileInputEmail = findViewById(R.id.ProfileInputEmail);
        E_ProfileInputUsername = findViewById(R.id.ProfileInputUsername);
        E_ProfileInputAddress = findViewById(R.id.ProfileInputAddress);
        Button BtnProfileSave= findViewById(R.id.btnSave);

        //noinspection unused
        ArrayList<Category> citiesList = new ArrayList<>();

        String firstName= getIntent().getStringExtra("firstName");
        String lastName= getIntent().getStringExtra("lastName");
        String email= getIntent().getStringExtra("email");
        String userName= getIntent().getStringExtra("userName");
        String address= getIntent().getStringExtra("address");

        //noinspection unused

        getUserData(firstName, lastName, email, userName, address);

        BtnProfileSave.setOnClickListener(view -> {

            String FirstName = E_ProfileInputName.getText().toString().trim();
            String LastName = E_ProfileInputLastName.getText().toString().trim();
            String Email = E_ProfileInputEmail.getText().toString().trim();
            String UserName = E_ProfileInputUsername.getText().toString().trim();
            String Address = E_ProfileInputAddress.getText().toString().trim();

            saveChanges(FirstName, LastName, Email, UserName, Address);
        });


    }




    private void getUserData(String firstName, String lastName, String email, String userName, String address) {

        E_ProfileInputName.setText(firstName);
        E_ProfileInputLastName.setText(lastName);
        E_ProfileInputEmail.setText(email);
        E_ProfileInputUsername.setText(userName);
        E_ProfileInputAddress.setText(address);

    }

    private void saveChanges(String firstName, String lastName, String email, String userName, String address) {

        FirebaseDatabase  database = FirebaseDatabase.getInstance();
        DatabaseReference mDatabaseRef = database.getReference("Users");

        Map<String, Object> hopperUpdates = new HashMap<>();

        hopperUpdates.put(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid() + "/firstName", firstName);
        hopperUpdates.put(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid() + "/lastName", lastName);
        hopperUpdates.put(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid() + "/email", email);
        hopperUpdates.put(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid() + "/userName", userName);
        hopperUpdates.put(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid() + "/address", address);
        mDatabaseRef.updateChildren(hopperUpdates);

        new SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                .setTitleText("Pavyko!")
                .setContentText("Duomenys atnaujinti!")
                .show();

    }

    public void onBackPressed() {
        int backstack = getSupportFragmentManager().getBackStackEntryCount();

        if (backstack > 0)
        {
            getSupportFragmentManager().popBackStack();
        }
        else
        {
            super.onBackPressed();
        }
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
