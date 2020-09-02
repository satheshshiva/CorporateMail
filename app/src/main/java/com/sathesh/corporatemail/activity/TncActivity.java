package com.sathesh.corporatemail.activity;

import android.os.Bundle;

import com.sathesh.corporatemail.R;
import com.sathesh.corporatemail.application.MailApplication;
import com.sathesh.corporatemail.application.MyActivity;
import com.sathesh.corporatemail.constants.Constants;

import android.webkit.WebSettings;
import android.webkit.WebView;

public class TncActivity  extends MyActivity implements Constants {

    private WebView webview;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tnc);
        MailApplication.toolbarInitialize(this);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        webview = (WebView) findViewById(R.id.tnc_webview);
        WebSettings webSettings = webview.getSettings();

        webSettings.setJavaScriptEnabled(true);
        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
        webview.loadUrl(Tnc.privacyUrl);
    }
}