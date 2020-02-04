package mist.api.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Toast;

import java.io.File;
import java.util.HashMap;

/**
 * Created by jeppe on 11/16/16.
 */

public class Sandbox extends Activity {

    private final static String TAG = "Sandbox";

    WebView webView;
    private String uiDirectory;
    private int id;

    private HashMap<Integer, BackListener> backListenerHashMap = new HashMap<Integer, BackListener>();
    private int backListenerId = 0;

    private MistSandboxApi api;;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sandbox);

        Intent intent = this.getIntent();
        if (!intent.hasExtra("dir") || !intent.hasExtra("id")) {
            Toast.makeText(getApplicationContext(), "Tried to start ui without reference", Toast.LENGTH_LONG).show();
            Log.d(TAG, "dir: " + intent.hasExtra("dir") + " id: " + intent.hasExtra("id"));
            finish();
        }

        uiDirectory = intent.getStringExtra("dir");
        id = intent.getIntExtra("id", -2);

        Log.d(TAG, "dir: " + uiDirectory);

        api = MistSandboxApi.getInstance();
        // api.register(id, response);

        webView = (WebView) findViewById(R.id.webview);

        webView.getSettings().setJavaScriptEnabled(true);

        webView.getSettings().setAllowUniversalAccessFromFileURLs(true);
        if (Build.VERSION.SDK_INT >= 19) {
            webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } else {
            webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        webView.addJavascriptInterface(new WebViewInterface(), "android");
        webView.setOnKeyListener(keyListener);

        File file = new File(uiDirectory + "/package/src/application.html");
        Log.d(TAG, "url: " + file);

        webView.loadUrl("file:///" + file);

    }

    private class WebViewInterface {

        @JavascriptInterface
        public void send(byte[] bson) throws Exception {
            api.sandboxSouth(id, bson);
        }

        @JavascriptInterface
        public void connect(String args) {
        Intent intent = new Intent();
            intent.putExtra("id", id);
            intent.putExtra("args", args);
            intent.setAction("CUSTOM_UI_CONNECT");
            sendBroadcast(intent);
        }

        @JavascriptInterface
        public void onBack(int id, boolean state) {
            Log.d("log", "onBack");
            if (backListenerHashMap.containsKey(id)) {
                BackListener listener = backListenerHashMap.get(id);
                listener.backButton(state);
                backListenerHashMap.remove(id);
            }
        }
    }

    private MistSandboxApi.Response response = new MistSandboxApi.Response() {
        @Override
        public void sandboxNorth(byte[] bson) {
            final String bson64string = Base64.encodeToString(bson, Base64.DEFAULT).replace("\n", "").replace("\r", "");
            webView.post(new Runnable() {
                @Override
                public void run() {
                    webView.loadUrl("javascript: android.receive('" + bson64string + "')");
                }
            });
        }
    };

    private View.OnKeyListener keyListener = new View.OnKeyListener() {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                switch (keyCode) {
                    case KeyEvent.KEYCODE_BACK:
                        back(new BackListener() {
                            @Override
                            public void backButton(boolean state) {
                                Log.d("log", "backbutton");
                                if (!state) {
                                    finish();
                                    // finishActivity()
                                }
                            }
                        });
                        return true;
                }
            }
            return false;
        }
    };

    private void back(BackListener listener) {
        backListenerHashMap.put(backListenerId, listener);
        webView.post(new Runnable() {
            @Override
            public void run() {
                webView.loadUrl("javascript:android.onBack(" + backListenerId + ", android.backButton())");
            }
        });
    }

    private interface BackListener {
        public void backButton(boolean state);
    }

    protected void onPause() {
        super.onPause();
        if (webView != null) {
            webView.pauseTimers();

        }
    }

    protected void onResume() {
        super.onResume();
        webView.resumeTimers();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (webView != null) {
            webView.clearHistory();
            webView.clearCache(true);
            webView.loadUrl("about:blank");
            webView = null;
        }
    }

    public interface Signals {
        public void onConnect(String args);
    }
}
