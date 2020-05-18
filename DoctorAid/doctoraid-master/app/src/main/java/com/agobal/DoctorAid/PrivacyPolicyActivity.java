package com.agobal.DoctorAid;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

public class PrivacyPolicyActivity extends Activity {
    WebView web;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_policy);

        web = findViewById(R.id.webView);
        web.loadUrl("https://medium.com/@AndreSand/add-privacy-policy-page-to-your-android-application-54a7ea8f0fc8"); //TODO: private policy

    }
}
