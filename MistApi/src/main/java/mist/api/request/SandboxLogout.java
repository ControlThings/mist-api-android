package mist.api.request;

import org.bson.BSONException;
import org.bson.BsonBinaryWriter;
import org.bson.BsonDocument;
import org.bson.BsonWriter;
import org.bson.RawBsonDocument;
import org.bson.io.BasicOutputBuffer;

import mist.api.MistApi;

import static mist.api.request.Callback.BSON_ERROR_CODE;
import static mist.api.request.Callback.BSON_ERROR_STRING;

class SandboxLogout {
    static int request(byte[] id, Sandbox.LogoutCb callback) {
        final String op = "sandboxed.logout";

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
            Sandbox.LogoutCb cb;

            @Override
            public void response(byte[] data) {
                boolean value;
                try {
                    BsonDocument bson = new RawBsonDocument(data);
                    value = bson.getBoolean("data").getValue();
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

            private MistApi.RequestCb init(Sandbox.LogoutCb callback) {
                this.cb = callback;
                return this;
            }
        }.init(callback));
    }
}
