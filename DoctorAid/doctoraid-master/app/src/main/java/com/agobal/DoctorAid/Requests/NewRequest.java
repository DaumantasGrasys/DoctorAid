package com.agobal.DoctorAid.Requests;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mlsdev.rximagepicker.RxImagePicker;
import com.mlsdev.rximagepicker.Sources;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import cn.pedant.SweetAlert.SweetAlertDialog;
import io.reactivex.Observable;


public class NewRequest extends AppCompatActivity {


    private StorageReference mDatabaseStorage;

    private DatabaseReference mUserRequestsDatabase;
    private DatabaseReference mUserDatabase;

    private final FirebaseUser mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
    private final String current_uid = Objects.requireNonNull(mCurrentUser).getUid();

    private EditText etRequestDescription;


    //private String CheckedMoney;
    //private String CheckedBoth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_new_request);

        Objects.requireNonNull(getSupportActionBar()).setDisplayOptions(android.support.v7.app.ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.action_bar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        TextView title = findViewById(getResources().getIdentifier("action_bar_title", "id", getPackageName()));
        title.setText("Nauja užklausa");

        mDatabaseStorage = FirebaseStorage.getInstance().getReference();
        mUserRequestsDatabase = FirebaseDatabase.getInstance().getReference().child("Requests").child(current_uid);
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        etRequestDescription = findViewById(R.id.inputDescription);

        Button btnCreate = findViewById(R.id.btnCreateRequest);

        btnCreate.setOnClickListener(view -> {
            String requestDescription = etRequestDescription.getText().toString().trim();
            CreateRequest(requestDescription);
        });


    }


    private void CreateRequest(String requestDescription){


        //String key = FirebaseDatabase.getInstance().getReference("Requests").push().getKey();


        Map<String, Object> Updates = new HashMap<>();
        Updates.put("description", requestDescription);
        Updates.put("active", 1);
        Updates.put("dateCreated", System.currentTimeMillis());
        Updates.put("taken", 0);
        Updates.put("takenBy", "none");

        mUserRequestsDatabase.updateChildren(Updates);


        SweetAlertDialog pDialog = new SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE);
        pDialog.setTitleText("Pavyko!");
        pDialog.setContentText("Užklausa sukurta.");
        pDialog.setConfirmText("Tęsti");
        pDialog.setConfirmClickListener(sweetAlertDialog -> {
                    pDialog.hide();
                    SweetAlertDialog pDialog2 = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
                    pDialog2.getProgressHelper().setBarColor(Color.parseColor("#3498DB"));
                    pDialog2.setTitleText("Ieškoma gydytojo...");
                    pDialog2.setCancelable(true);
                    pDialog2.showCancelButton(true);
                    pDialog2.setCancelText("Grįžti");
                    pDialog2.show();
                    pDialog2.setCancelClickListener(SweetAlertDialog -> {
                                Intent intent = new Intent(NewRequest.this, MainActivity.class);
                                startActivity(intent);
                    }
                    );
                });
        pDialog.show();



    }


    public void onBackPressed() {
        int backstack = getSupportFragmentManager().getBackStackEntryCount();

        if (backstack > 0) {
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
