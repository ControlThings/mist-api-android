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
import org.bson.BsonBinaryWriter;
import org.bson.BsonDocument;
import org.bson.BsonValue;
import org.bson.BsonWriter;
import org.bson.RawBsonDocument;
import org.bson.io.BasicOutputBuffer;

import java.util.ArrayList;
import java.util.List;

import mist.api.MistApi;
import wish.Peer;

/**
 * Created by jeppe on 11/29/16.
 */

class MistListServices {

    static int request(Mist.ListServicesCb callback) {
        final String op = "listPeers";

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
            Mist.ListServicesCb cb;

            @Override
            public void response(byte[] data) {
                List<Peer> peers;
                try {
                    peers  = new ArrayList<Peer>();
                    BsonDocument bson = new RawBsonDocument(data);

                    BsonDocument bsonListServices = bson.getDocument("data");
                    for (java.util.Map.Entry<String, BsonValue> entry : bsonListServices.entrySet()) {
                        String key = entry.getKey();
                        BsonDocument peerDocument = entry.getValue().asDocument();
                        Peer peer = Peer.fromBson(peerDocument);
                        peers.add(peer);
                    }
                } catch (BSONException e) {
                    cb.err(Callback.BSON_ERROR_CODE, Callback.BSON_ERROR_STRING);
                    return;
                }
                cb.cb(peers);
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

            private MistApi.RequestCb init(Mist.ListServicesCb callback) {
                this.cb = callback;
                return this;
            }
        }.init(callback));
    }
}
