package com.example.hermes.rd_hermes_flores;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

public class Browser extends AppCompatActivity {
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browser);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

         progressBar =( ProgressBar) findViewById(R.id.progressBarWV);// (this, null, android.R.attr.progressBarStyleSmall);
        Toast.makeText(this, "Loading", Toast.LENGTH_LONG).show();

        String url=getIntent().getExtras().get("URL").toString();

        WebView myWebView = (WebView) findViewById(R.id.browser);
        WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        myWebView.loadUrl(url);

        myWebView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                //elimina ProgressBar.

                            }
        });

        progressBar.setVisibility(View.GONE);



    }

}
