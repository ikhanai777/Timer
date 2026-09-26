package com.prototimer.garden;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
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

/** Hosts the Pomodoro Garden page (3D, WebGL) in a full-screen WebView. */
public class GardenActivity extends Activity {

    private static final String ASSET_ROOT = "file:///android_asset/";
    private static final String NOTIFICATION_PERMISSION = "android.permission.POST_NOTIFICATIONS";

    /** True while the app is on screen; the alarm receiver stays quiet then, because the page chimes itself. */
    static volatile boolean foreground;

    private WebView web;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);

        web = new WebView(this);
        web.setBackgroundColor(Color.parseColor("#0f1a12"));
        web.setVerticalScrollBarEnabled(false);
        web.setHorizontalScrollBarEnabled(false);
        web.setOverScrollMode(View.OVER_SCROLL_NEVER);

        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);          // the garden and settings live in localStorage
        s.setAllowFileAccess(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setTextZoom(100);

        web.addJavascriptInterface(new Bridge(), "AndroidGarden");
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
        web.loadUrl(ASSET_ROOT + "garden.html");
    }

    @Override
    protected void onResume() {
        super.onResume();
        foreground = true;
        web.onResume();
        web.resumeTimers();
    }

    @Override
    protected void onPause() {
        foreground = false;
        // Pause rendering to save battery; the native alarm covers the phase end.
        web.onPause();
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        web.evaluateJavascript("window.gardenBack ? window.gardenBack() : false",
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

    private PendingIntent alarmIntent(String title, String text) {
        Intent i = new Intent(this, AlarmReceiver.class);
        if (title != null) i.putExtra("title", title);
        if (text != null) i.putExtra("text", text);
        return PendingIntent.getBroadcast(this, 1, i,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    /** Native features the page calls through window.AndroidGarden. */
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

        /** Takes a JSON array in the Web Vibration API format, e.g. [200, 100, 200]. */
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
                    long[] pattern = new long[a.length() + 1];
                    for (int i = 0; i < a.length(); i++) pattern[i + 1] = a.getLong(i);
                    v.vibrate(pattern, -1);
                }
            } catch (Exception ignored) {
            }
        }

        /** Rings a notification at the given wall-clock time (ms) if the app is not on screen. */
        @JavascriptInterface
        public void scheduleAlarm(double atMillis, String title, String text) {
            AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (am == null) return;
            PendingIntent pi = alarmIntent(title, text);
            long at = (long) atMillis;
            try {
                if (Build.VERSION.SDK_INT >= 31 && !am.canScheduleExactAlarms()) {
                    am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi);
                } else {
                    am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi);
                }
            } catch (SecurityException e) {
                am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi);
            }
        }

        @JavascriptInterface
        public void cancelAlarm() {
            AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (am != null) am.cancel(alarmIntent(null, null));
        }

        /** Asks for notification permission (Android 13+) the first time a session starts. */
        @JavascriptInterface
        public void ensureNotifications() {
            if (Build.VERSION.SDK_INT < 33) return;
            if (checkSelfPermission(NOTIFICATION_PERMISSION) == PackageManager.PERMISSION_GRANTED) return;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    requestPermissions(new String[]{NOTIFICATION_PERMISSION}, 7);
                }
            });
        }
    }
}
