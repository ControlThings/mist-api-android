package mist.api.request;

import org.bson.BSONException;
import org.bson.BsonArray;
import org.bson.BsonBinaryWriter;
import org.bson.BsonDocument;
import org.bson.BsonWriter;
import org.bson.RawBsonDocument;
import org.bson.io.BasicOutputBuffer;

import mist.api.MistApi;
import wish.WishApp;

import static mist.api.request.Callback.BSON_ERROR_CODE;
import static mist.api.request.Callback.BSON_ERROR_STRING;

/**
 * Created by jeppe on 11/30/16.
 */

class SignalsReady {


    static int request(Signals.ReadyCb callback) {
        final String op = "signals";
        final String signalsType = "ready";

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
            Signals.ReadyCb cb;

            @Override
            public void response(byte[] data) {
                boolean value;
                try {
                    BsonDocument bson = new RawBsonDocument(data);
                    if (bson.isArray("data")) {
                        BsonArray bsonArray = bson.getArray("data");
                        if (bsonArray.get(0).asString().getValue().equals(signalsType) && bsonArray.size() > 1 && bsonArray.get(1).isBoolean()) {
                            value = (bsonArray.get(1).asBoolean().getValue());
                        } else {
                            cb.err(BSON_ERROR_CODE, BSON_ERROR_STRING);
                            return;
                        }
                    } else {
                        return;
                    }
                } catch (BSONException e) {
                    cb.err(BSON_ERROR_CODE, BSON_ERROR_STRING);
                    return;
                }
                cb.cb(value);
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

            private MistApi.RequestCb init(Signals.ReadyCb callback) {
                this.cb = callback;
                return this;
            }
        }.init(callback));
    }
}
