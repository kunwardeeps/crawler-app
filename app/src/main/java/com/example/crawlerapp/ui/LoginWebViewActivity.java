package com.example.crawlerapp.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import android.view.View;
import android.widget.Button;
import androidx.annotation.Nullable;
import com.example.crawlerapp.R;

public class LoginWebViewActivity extends Activity {
    public static final String EXTRA_LOGIN_URL = "login_url";
    public static final String EXTRA_COOKIES = "cookies";
    private WebView webView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_webview);
        webView = findViewById(R.id.webViewLogin);
        Button doneBtn = findViewById(R.id.btnLoginDone);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                // Optionally, you can add a button to let the user confirm login is complete
            }
        });
        String loginUrl = getIntent().getStringExtra(EXTRA_LOGIN_URL);
        if (loginUrl != null) {
            webView.loadUrl(loginUrl);
        } else {
            Toast.makeText(this, "No login URL provided", Toast.LENGTH_SHORT).show();
            finish();
        }
        doneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishWithCookies();
            }
        });
    }

    // Call this when the user confirms login is complete
    private void finishWithCookies() {
        String url = webView.getUrl();
        String cookies = CookieManager.getInstance().getCookie(url);
        // Also persist cookies for the base domain for use in other requests
        if (cookies == null) cookies = "";
        String baseDomain = null;
        try {
            java.net.URI uri = new java.net.URI(url);
            baseDomain = uri.getScheme() + "://" + uri.getHost();
        } catch (Exception ignored) {}
        if (baseDomain != null) {
            String baseCookies = CookieManager.getInstance().getCookie(baseDomain);
            if (baseCookies != null && !baseCookies.isEmpty() && !cookies.contains(baseCookies)) {
                cookies = cookies + "; " + baseCookies;
            }
        }
        Intent result = new Intent();
        result.putExtra(EXTRA_COOKIES, cookies);
        setResult(Activity.RESULT_OK, result);
        finish();
    }
}

