package fi.ct.mist.ui;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import mist.api.request.Mist;
import wish.request.Identity;
import wish.request.Wish;

/**
 * Created by jeppe on 10/27/17.
 */

public class Test {

    private static final String TAG = "old TEST";

    int signals_id = 0;

    public void runTest(){

        Identity.list(new Identity.ListCb() {

            @Override
            public void cb(List<wish.Identity> identityList) {
                for (wish.Identity ide : identityList) {
                    Log.d(TAG, "list: " + ide.getAlias());
                }
            }

            @Override
            public void err(int code, String msg) {}

            @Override
            public void end() {}
        });

        Mist.version(new Mist.VersionCb() {
            @Override
            public void cb(String version) {
                Log.d(TAG, "Mist API version: " + version);
            }

            @Override
            public void err(int code, String msg) {
                Log.d(TAG, "Mist API version err : " + msg);
            }

            @Override
            public void end() {

            }
        });


        signals_id = Wish.signals(null, new Wish.SignalsCb() {
            @Override
            public void cb(String signal) {
                Log.d(TAG, "Got signal:" + signal);
                if (signals_id != 0) {
                    Wish.cancel(signals_id);
                }
            }

            @Override
            public void err(int code, String msg) {

            }

            @Override
            public void end() {

            }
        });

        Log.d(TAG, "signals_id" + signals_id);
/*
        BasicOutputBuffer buffer = new BasicOutputBuffer();
        BsonWriter writer = new BsonBinaryWriter(buffer);
        writer.writeStartDocument();
        writer.writeString("op", "listPeers");
        writer.writeStartArray("args");
        writer.writeEndArray();
        writer.writeInt32("id", 0);
        writer.writeEndDocument();
        writer.flush();

        mistNode.MistNode.getInstance().wishRequest(buffer.toByteArray(), new mistNode.MistNode.RequestCb() {
            @Override
            public void response(byte[] data) {
                Log.d(TAG, "res");
            }

            @Override
            public void end() {

            }

            @Override
            public void err(int code, String msg) {
                Log.d(TAG, "err");
            }
        });

*/
    }
}
