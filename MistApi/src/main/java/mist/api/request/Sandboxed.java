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
package mist.api.request;

import android.util.Log;

import org.bson.BSONException;
import org.bson.BsonBinaryWriter;
import org.bson.BsonDocument;
import org.bson.BsonDocumentReader;
import org.bson.BsonInt32;
import org.bson.BsonString;
import org.bson.BsonWriter;
import org.bson.RawBsonDocument;
import org.bson.io.BasicOutputBuffer;

import java.util.HashMap;

import mist.api.MistApi;

public class Sandboxed {

    private final static String TAG = "Sandboxed";

    private final static HashMap<Integer, Integer> cancelMap = new HashMap<>();

    public static void request(byte[] sandboxId, byte[] bson, SandboxedCb callback) {

        String op;
        int requestId;
        int cancelId;
        BsonDocument argsDocument = new BsonDocument();

        final BsonDocument bsonDocument = new RawBsonDocument(bson);
        try {

            if (bsonDocument.containsKey("end")) {
                int endId = bsonDocument.getInt32("end").getValue();
                MistApi.getInstance().sandboxedRequestCancel(sandboxId, endId);
                cancelMap.remove(endId);
            } else {
                requestId = bsonDocument.getInt32("id").getValue();
                op = bsonDocument.getString("op").getValue();

                argsDocument.append("args", bsonDocument.getArray("args"));
                argsDocument.append("op", new BsonString("sandboxed."+op));
                argsDocument.append("id", new BsonInt32(0));

                BasicOutputBuffer buffer = new BasicOutputBuffer();
                try {
                    BsonWriter writer = new BsonBinaryWriter(buffer);
                    BsonDocumentReader bsonDocumentReader = new BsonDocumentReader(argsDocument);
                    writer.pipe(bsonDocumentReader);
                    writer.flush();
                } catch (Exception e) {
                    //// TODO: send error msg callback
                    Log.d(TAG, "error creating bson: " + e.getMessage());
                }

                MistApi.RequestCb requestCb = new MistApi.RequestCb() {
                    private int requestId;
                    private SandboxedCb callback;

                    @Override
                    public void ack(byte[] data) {
                        BsonDocument document  = new BsonDocument();
                        try {
                            document.append("ack", new BsonInt32(requestId));
                            if (new RawBsonDocument(data).containsKey("data")) {
                                document.append("data", new RawBsonDocument(data).get("data"));
                            }
                        } catch (BSONException e) {
                            Log.d(TAG, "error parsing ack: " + e.getMessage());
                        }
                        response(document);
                        cancelMap.remove(requestId);
                    }

                    @Override
                    public void sig(byte[] data) {
                        BsonDocument document  = new BsonDocument();
                        try {
                            document.append("sig", new BsonInt32(requestId));
                            if (new RawBsonDocument(data).containsKey("data")) {
                                document.append("data", new RawBsonDocument(data).get("data"));
                            }
                        } catch (BSONException e) {
                            Log.d(TAG, "error parsing ack: " + e.getMessage());
                        }
                        response(document);
                    }

                    @Override
                    public void err(int code, String msg) {
                        BsonDocument document  = new BsonDocument();
                        BsonDocument errMsg  = new BsonDocument();
                        errMsg.append("msg", new BsonString(msg));
                        errMsg.append("code", new BsonInt32(code));

                        try {
                            document.append("err", new BsonInt32(requestId));
                            document.append("data", errMsg);
                        } catch (BSONException e) {
                            Log.d(TAG, "error parsing ack: " + e.getMessage());
                        }
                        response(document);
                        cancelMap.remove(requestId);
                    }

                    private void response(BsonDocument document) {
                        BasicOutputBuffer buffer = new BasicOutputBuffer();
                        BsonWriter writer = new BsonBinaryWriter(buffer);
                        BsonDocumentReader bsonDocumentReader = new BsonDocumentReader(document);
                        writer.pipe(bsonDocumentReader);
                        writer.flush();
                        callback.cb(buffer.toByteArray());
                    }

                    @Override
                    public void response(byte[] data) {}

                    @Override
                    public void end() {}

                    private MistApi.RequestCb init(int requestId, SandboxedCb callback) {
                        this.requestId = requestId;
                        this.callback = callback;
                        return this;
                    }
                }.init(requestId, callback);
                cancelId = MistApi.getInstance().sandboxedRequest(sandboxId, buffer.toByteArray(), requestCb);
                cancelMap.put(requestId, cancelId);
            }


        } catch (Exception e) {
            //// TODO: send error msg callback
            Log.d(TAG, "error parsing bson: " + e.getMessage());
        }
    }

    public interface SandboxedCb {
        public void cb(byte[] bson);
    }
}

