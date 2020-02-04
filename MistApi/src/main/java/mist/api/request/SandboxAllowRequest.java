package mist.api.request;

import org.bson.BSONException;
import org.bson.BsonBinary;
import org.bson.BsonBinaryWriter;
import org.bson.BsonDocument;
import org.bson.BsonDocumentReader;
import org.bson.BsonWriter;
import org.bson.RawBsonDocument;
import org.bson.io.BasicOutputBuffer;

import mist.api.MistApi;
class SandboxAllowRequest {
    static int request(byte[] sandboxId, BsonDocument document, Sandbox.AllowRequestCb callback) {
        final String op = "sandbox.allowRequest";

        BasicOutputBuffer buffer = new BasicOutputBuffer();
        BsonWriter writer = new BsonBinaryWriter(buffer);
        writer.writeStartDocument();

        writer.writeString("op", op);

        writer.writeStartArray("args");
        writer.writeBinaryData(new BsonBinary(sandboxId));
        writer.pipe(new BsonDocumentReader(document));
        writer.writeEndArray();

        writer.writeInt32("id", 0);

        writer.writeEndDocument();
        writer.flush();

        return MistApi.getInstance().request(buffer.toByteArray(), new MistApi.RequestCb() {
            Sandbox.AllowRequestCb cb;

            @Override
            public void response(byte[] data) {
                boolean value;
                try {
                    BsonDocument bson = new RawBsonDocument(data);
                    value = bson.getBoolean("data").getValue();
                } catch (BSONException e) {
                    cb.err(Callback.BSON_ERROR_CODE, Callback.BSON_ERROR_STRING);
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

            private MistApi.RequestCb init(Sandbox.AllowRequestCb callback) {
                this.cb = callback;
                return this;
            }
        }.init(callback));
    }
}
