package mist.api.request;

import android.util.Log;

import org.bson.BSONException;
import org.bson.BsonBinaryWriter;
import org.bson.BsonDocument;
import org.bson.BsonWriter;
import org.bson.RawBsonDocument;
import org.bson.io.BasicOutputBuffer;

import mist.api.MistApi;

class MistReady {
    static int request(Mist.ReadyCb callback) {
        final String op = "ready";

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
            Mist.ReadyCb cb;

            @Override
            public void response(byte[] data) {
                boolean state;
                try {
                    BsonDocument bson = new RawBsonDocument(data);
                    state = bson.get("data").asBoolean().getValue();
                } catch (BSONException e) {
                    cb.err(Callback.BSON_ERROR_CODE, Callback.BSON_ERROR_STRING);
                    return;
                }
                cb.cb(state);
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

            private MistApi.RequestCb init(Mist.ReadyCb callback) {
                this.cb = callback;
                return this;
            }
        }.init(callback));
    }
}
