package mist.api.request;

import android.util.Log;

import org.bson.BSONException;
import org.bson.BsonArray;
import org.bson.BsonBinaryWriter;
import org.bson.BsonDocument;
import org.bson.BsonValue;
import org.bson.BsonWriter;
import org.bson.RawBsonDocument;
import org.bson.io.BasicOutputBuffer;

import mist.api.MistApi;

/**
 * Created by jeppe on 11/30/16.
 */

class MistSignals {
    static int request(Mist.SignalsCb callback) {
        final String op = "signals";

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
            private Mist.SignalsCb cb;

            @Override
            public void response(byte[] data) {
                BsonValue bsonValue;
                BsonDocument bson;
                try {
                    bson = new RawBsonDocument(data);
                    bsonValue = bson.get("data");
                } catch (BSONException e) {
                    cb.err(Callback.BSON_ERROR_CODE, Callback.BSON_ERROR_STRING);
                    return;
                }
                if (bsonValue.isString()) {
                    cb.cb(bsonValue.asString().getValue(), bson);
                }
                if (bsonValue.isArray()) {
                    BsonArray bsonArray = bsonValue.asArray();
                    if (bsonArray.get(0).isString()) {
                        cb.cb(bsonArray.get(0).asString().getValue(), bson);
                    }
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

            private MistApi.RequestCb init(Mist.SignalsCb callback) {
                this.cb = callback;
                return this;
            }
        }.init(callback));
    }
}
