/**
 * Copyright (C) 2020, ControlThings Oy Ab
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * @license Apache-2.0
 */
package fi.ct.mist.ui.custom_ui;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ShareCompat;
import android.util.Log;

import mist.api.ui.CustomUi;
import fi.ct.mist.ui.MainActivity;
import fi.ct.mist.ui.R;

/**
 * Created by jeppe on 11/24/16.
 */

public class CustomUiLoader extends Activity {

    private final String TAG = "CustomWebView";

    private final int requestCode = 23;
    private CustomUi customUi = null;
    private String md5;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_ui);

        Intent intent = getIntent();
        if (intent.hasExtra("CustomApp")) {
            md5 = intent.getStringExtra("CustomApp");
            Uri uri = ShareCompat.IntentReader.from(this).getStream();

            customUi = new CustomUi(this, requestCode);
            customUi.registerConnectListener(connectListener);
            customUi.addUi(uri, addUiResponse);
        }
    }

    //pushed back from ui
    protected void onActivityResult(int code, int resultCode, Intent data) {
        Log.d(TAG, "nActivityResult" + code + " : " + resultCode);
        if (code == requestCode) {
            finish();
        }
    }

    private CustomUi.AddUiResponse addUiResponse = new CustomUi.AddUiResponse() {
        @Override
        public void added() {
            customUi.load(md5);
        }

        @Override
        public void exists() {
            customUi.load(md5);
        }
    };

    private CustomUi.ConnectListener connectListener = new CustomUi.ConnectListener() {
        @Override
        public void connect(String args) {
            Log.d(TAG, "connect: " + args);
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.putExtra("fragment", "connect");
            intent.putExtra("args", args);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getApplicationContext().startActivity(intent);
        }
    };



    protected void onPause() {
        super.onPause();
    }

    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (customUi != null) {
            customUi.clean();
        }
    }



}
