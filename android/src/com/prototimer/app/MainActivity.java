package com.prototimer.app;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.json.JSONArray;

/** Hosts one of the bundled timer pages in a full-screen WebView. */
public class MainActivity extends Activity {

    private static final String ASSET_ROOT = "file:///android_asset/";

    private WebView web;

    /** The page this launcher icon opens. */
    protected String page() {
        return "index.html";
    }

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);

        web = new WebView(this);
        web.setBackgroundColor(Color.BLACK);
        web.setVerticalScrollBarEnabled(false);
        web.setHorizontalScrollBarEnabled(false);
        web.setOverScrollMode(View.OVER_SCROLL_NEVER);

        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);          // settings and presets live in localStorage
        s.setAllowFileAccess(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setTextZoom(100);                    // keep the layout fixed regardless of system font size

        web.addJavascriptInterface(new Bridge(), "AndroidTimer");
        web.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith(ASSET_ROOT)) return false;
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                } catch (Exception ignored) {
                }
                return true;
            }
        });

        setContentView(web);
        if (state == null || web.restoreState(state) == null) {
            web.loadUrl(ASSET_ROOT + page());
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle out) {
        super.onSaveInstanceState(out);
        web.saveState(out);
    }

    @Override
    public void onBackPressed() {
        // Let the page close its settings sheet first; otherwise keep the timer
        // running and just send the app to the background.
        web.evaluateJavascript("window.protoTimerBack ? window.protoTimerBack() : false",
                new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String handled) {
                        if (!"true".equals(handled)) moveTaskToBack(true);
                    }
                });
    }

    @Override
    protected void onDestroy() {
        if (web != null) {
            web.destroy();
            web = null;
        }
        super.onDestroy();
    }

    /** Native features the page calls through window.AndroidTimer. */
    private class Bridge {

        @JavascriptInterface
        public void keepScreenOn(final boolean on) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (on) getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    else getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                }
            });
        }

        /** Takes a JSON array in the Web Vibration API format, e.g. [400, 200, 400]. */
        @JavascriptInterface
        public void vibrate(String json) {
            Vibrator v = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            if (v == null || !v.hasVibrator()) return;
            try {
                JSONArray a = new JSONArray(json);
                if (a.length() == 0 || (a.length() == 1 && a.getLong(0) <= 0)) {
                    v.cancel();
                } else if (a.length() == 1) {
                    v.vibrate(a.getLong(0));
                } else {
                    long[] pattern = new long[a.length() + 1]; // Android patterns start with a delay
                    for (int i = 0; i < a.length(); i++) pattern[i + 1] = a.getLong(i);
                    v.vibrate(pattern, -1);
                }
            } catch (Exception ignored) {
            }
        }
    }
}
