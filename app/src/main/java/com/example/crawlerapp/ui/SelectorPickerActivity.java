package com.example.crawlerapp.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import androidx.annotation.Nullable;
import com.example.crawlerapp.R;

public class SelectorPickerActivity extends Activity {
    public static final String EXTRA_URL = "url";
    public static final String EXTRA_SELECTOR = "selector";
    private WebView webView;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selector_picker);
        webView = findViewById(R.id.webView);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                injectSelectorScript();
            }
        });
        webView.addJavascriptInterface(new JSInterface(), "SelectorPicker");

        String url = getIntent().getStringExtra(EXTRA_URL);
        if (url != null) {
            webView.loadUrl(url);
        } else {
            Toast.makeText(this, "No URL provided", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void injectSelectorScript() {
        String js = "javascript:(function() {\n" +
                "  function cssPath(el) {\n" +
                "    if (!(el instanceof Element)) return '';\n" +
                "    var path = [];\n" +
                "    while (el.nodeType === Node.ELEMENT_NODE) {\n" +
                "      var selector = el.nodeName.toLowerCase();\n" +
                "      if (el.id) {\n" +
                "        selector += '#' + el.id;\n" +
                "        path.unshift(selector);\n" +
                "        break;\n" +
                "      } else {\n" +
                "        var sib = el, nth = 1;\n" +
                "        while (sib = sib.previousElementSibling) {\n" +
                "          if (sib.nodeName.toLowerCase() == selector) nth++;\n" +
                "        }\n" +
                "        if (nth > 1) selector += ':nth-of-type(' + nth + ')';\n" +
                "      }\n" +
                "      path.unshift(selector);\n" +
                "      el = el.parentNode;\n" +
                "    }\n" +
                "    return path.join(' > ');\n" +
                "  }\n" +
                "  function clearHighlights() {\n" +
                "    var old = document.querySelectorAll('.__selector_highlight');\n" +
                "    for (var i = 0; i < old.length; i++) {\n" +
                "      old[i].style.outline = old[i].__oldOutline || '';\n" +
                "      old[i].classList.remove('__selector_highlight');\n" +
                "    }\n" +
                "  }\n" +
                "  document.addEventListener('click', function(e) {\n" +
                "    e.preventDefault();\n" +
                "    e.stopPropagation();\n" +
                "    clearHighlights();\n" +
                "    var el = e.target;\n" +
                "    el.__oldOutline = el.style.outline;\n" +
                "    el.style.outline = '2px solid #2196F3';\n" +
                "    el.classList.add('__selector_highlight');\n" +
                "    var selector = cssPath(el);\n" +
                "    setTimeout(function() { window.SelectorPicker.onSelectorPicked(selector); }, 200);\n" +
                "  }, true);\n" +
                "})()";
        webView.evaluateJavascript(js, null);
    }

    private class JSInterface {
        @JavascriptInterface
        public void onSelectorPicked(String selector) {
            Intent result = new Intent();
            result.putExtra(EXTRA_SELECTOR, selector);
            setResult(Activity.RESULT_OK, result);
            finish();
        }
    }
}
