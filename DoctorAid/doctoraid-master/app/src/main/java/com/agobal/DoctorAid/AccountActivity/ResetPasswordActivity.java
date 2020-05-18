package com.agobal.DoctorAid.AccountActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.agobal.DoctorAid.R;
import com.google.firebase.auth.FirebaseAuth;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class ResetPasswordActivity extends Activity {

    private static final String TAG = "ResetPasswordActivity";

    private EditText inputEmail;
    private FirebaseAuth auth;
    ProgressBar spinner;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // remove title
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //Remove notification bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_reset_password);

        inputEmail = findViewById(R.id.p_email);
        Button btnReset = findViewById(R.id.btn_reset_password);
        TextView txtBack = findViewById(R.id.linkToLogin);
        spinner = findViewById(R.id.progressBar);

        spinner.setVisibility(View.GONE);


        auth = FirebaseAuth.getInstance();

        txtBack.setOnClickListener(v -> {
            Intent i = new Intent(getApplicationContext(),
                    LoginActivity.class);
            startActivity(i);
            finish();
        });

        btnReset.setOnClickListener(v -> {

            String email = inputEmail.getText().toString().trim();
            if (TextUtils.isEmpty(email)) {
                Toast.makeText(getApplication(), "Įveskite prisijungimo informaciją!", Toast.LENGTH_SHORT).show();
                return;
            }


            spinner.setVisibility(View.VISIBLE);

            auth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {

                            new SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                                    .setTitleText("Pavyko!")
                                    .setContentText("Slaptažodžio pakeitimo nuoroda Jums išsiųsta į el. pašto adresą")
                                    .setConfirmClickListener(sweetAlertDialog -> {
                                        Intent intent = new Intent(ResetPasswordActivity.this, LoginActivity.class);
                                        startActivity(intent);
                                    })
                                    .show();

                        } else {
                            new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                                    .setTitleText("Pavyko!")
                                    .setContentText("Nuorodos su slaptažodžiu išsiųsti nepavyko. Prašome patikrinti duomenis!")
                                    .setConfirmClickListener(sweetAlertDialog -> {
                                        Intent intent = new Intent(ResetPasswordActivity.this, LoginActivity.class);
                                        startActivity(intent);
                                    })
                                    .show();
                        }

                        spinner.setVisibility(View.GONE);
                    });
        });
    }

}