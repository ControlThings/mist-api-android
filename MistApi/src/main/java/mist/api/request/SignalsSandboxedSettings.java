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

import org.bson.BSONException;
import org.bson.BsonArray;
import org.bson.BsonBinaryWriter;
import org.bson.BsonDocument;
import org.bson.BsonWriter;
import org.bson.RawBsonDocument;
import org.bson.io.BasicOutputBuffer;

import mist.api.MistApi;

import static mist.api.request.Callback.BSON_ERROR_CODE;
import static mist.api.request.Callback.BSON_ERROR_STRING;
import static mist.api.request.Callback.SIGNALS_ERROR_CODE;

/**
 * Created by jeppe on 11/30/16.
 */

class SignalsSandboxedSettings {

    static int request(Signals.SandboxCb callback) {
        final String op = "signals";
        final String signalsType = "sandboxed.settings";

        BasicOutputBuffer buffer = new BasicOutputBuffer();
        BsonWriter writer = new BsonBinaryWriter(buffer);
        writer.writeStartDocument();

        writer.writeString("op", op);

        writer.writeStartArray("args");
        writer.writeEndArray();

        writer.writeInt32("id", 0);

        writer.writeEndDocument();
        writer.flush();

        return MistApi.getInstance().request(buffer.toByteArray(), new MistApi.RequestCb() {
            Signals.SandboxCb cb;

            @Override
            public void response(byte[] data) {
                byte[] id;
                String hint = null;
                BsonDocument hintDocument = null;
                try {
                    BsonDocument bson = new RawBsonDocument(data);
                    if (bson.isArray("data")) {
                        BsonArray bsonArray = bson.getArray("data");
                        if (bsonArray.get(0).asString().getValue().equals(signalsType) && bsonArray.get(1).isDocument()) {
                            BsonDocument bsonDocument = bsonArray.get(1).asDocument();
                            if (bsonDocument.containsKey("id") && bsonDocument.get("id").isBinary()) {
                                id = bsonDocument.get("id").asBinary().getData();
                                if (bsonDocument.containsKey("hint")) {
                                    if (bsonDocument.get("hint").isString()) {
                                        hint = bsonDocument.get("hint").asString().getValue();
                                    } else {
                                        cb.err(SIGNALS_ERROR_CODE, "unknown hint" ,bson);
                                        return;
                                    }
                                    if (bsonDocument.containsKey("opts")) {
                                        if (bsonDocument.isDocument("opts")) {
                                            hintDocument = bsonDocument.get("opts").asDocument();
                                        }
                                    }
                                } else {
                                    cb.err(SIGNALS_ERROR_CODE, "missing hint" ,bson);
                                    return;
                                }
                            } else {
                                cb.err(SIGNALS_ERROR_CODE, "no id" ,bson);
                                return;
                            }
                        } else {
                            return;
                        }
                    } else {
                        cb.err(SIGNALS_ERROR_CODE, "data not array" ,bson);
                        return;
                    }
                } catch (BSONException e) {
                    cb.err(BSON_ERROR_CODE, BSON_ERROR_STRING);
                    return;
                }
                if (hintDocument != null) {
                    cb.cb(id,hint, hintDocument);
                } else {
                    cb.cb(id, hint);
                }

            }

            @Override
            public void end() {
                cb.end();
            }

            @Override
            public void err(int code, String msg) {
                super.err(code, msg);
                cb.err(code, msg);
            }

            private MistApi.RequestCb init(Signals.SandboxCb callback) {
                this.cb = callback;
                return this;
            }
        }.init(callback));
    }
}
