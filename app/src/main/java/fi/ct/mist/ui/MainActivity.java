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
package fi.ct.mist.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;


import org.bson.BSONException;
import org.bson.BsonArray;
import org.bson.BsonBinaryWriter;
import org.bson.BsonDocument;
import org.bson.BsonValue;
import org.bson.BsonWriter;
import org.bson.RawBsonDocument;
import org.bson.io.BasicOutputBuffer;

import addon.AddonReceiver;
import mist.api.Service;
import mist.api.request.RawRequest;
import wish.WishApp;
import wish.request.Connection;


// implements serviceIntent receiver
public class MainActivity extends Activity implements AddonReceiver.Receiver {

    private final String TAG = "MainActivity";

    private Intent serviceIntent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Resopnse receiver for serviceIntent service (onConnected)
        AddonReceiver mistReceiver = new AddonReceiver(this);

        // Init Mist
        serviceIntent = new Intent(this, Service.class);
        serviceIntent.putExtra("receiver", mistReceiver);

        // Start Mist service
        startService(serviceIntent);

        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        Log.i("tag", "This'll run 300 milliseconds later");
                        onConnected();
                    }
                },
                300);


    }

    // Mist is runing and connected to wish
    @Override
    public void onConnected() {


        Log.d(TAG, "onConnected");

        final String op = "signals";

        BasicOutputBuffer buffer = new BasicOutputBuffer();
        BsonWriter writer = new BsonBinaryWriter(buffer);
        writer.writeStartDocument();

        writer.writeString("op", op);

        writer.writeStartArray("args");
        writer.writeEndArray();

        writer.writeInt32("id", 1);

        writer.writeEndDocument();
        writer.flush();

        RawRequest.request(buffer.toByteArray(), new RawRequest.RawRequestCb() {
            @Override
            public void cb(byte[] data) {
                BsonValue bsonValue;
                BsonDocument bson;
                try {
                    bson = new RawBsonDocument(data);
                    bsonValue = bson.get("data");
                } catch (BSONException e) {
                    Log.d(TAG, "cb error");
                    return;
                }

                Log.d(TAG, "cb bson:"+  bson.toJson());

                if (bsonValue.isString()) {
                    Log.d(TAG, "signal " + bsonValue.asString().getValue());
                }
                if (bsonValue.isArray()) {
                    BsonArray bsonArray = bsonValue.asArray();
                    if (bsonArray.get(0).isString()) {
                        Log.d(TAG, "signal as array " + bsonArray.get(0).asString().getValue());
                    }
                }


            }
        });
       /*
        Intent intent = new Intent(this, Main.class);
        startActivity(intent);
        //new Test().runTest();*/
    }

    public void onDisconnected() {
        Log.d(TAG, "Disconnected from Wish Core!");
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //light.cleanup();
        stopService(serviceIntent);
    }
}
