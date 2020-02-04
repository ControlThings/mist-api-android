package mist.api.request;

import org.bson.BSONException;
import org.bson.BsonArray;
import org.bson.BsonBinary;
import org.bson.BsonBinaryWriter;
import org.bson.BsonDocument;
import org.bson.BsonDocumentReader;
import org.bson.BsonReader;
import org.bson.BsonValue;
import org.bson.BsonWriter;
import org.bson.RawBsonDocument;
import org.bson.io.BasicOutputBuffer;

import java.util.ArrayList;
import java.util.List;

import mist.api.MistApi;

class SandboxEmit {
    static int request(byte[] sandboxId, String hint, BsonDocument document, Sandbox.EmitCb callback) {
        final String op = "sandbox.emit";

        BasicOutputBuffer buffer = new BasicOutputBuffer();
        BsonWriter writer = new BsonBinaryWriter(buffer);
        writer.writeStartDocument();

        writer.writeString("op", op);

        writer.writeStartArray("args");
        writer.writeBinaryData(new BsonBinary(sandboxId));
        writer.writeString(hint);
        if (document != null) {
            BsonReader reader = new BsonDocumentReader(document);
            writer.pipe(reader);
        }
        writer.writeEndArray();

        writer.writeInt32("id", 0);

        writer.writeEndDocument();
        writer.flush();

       return MistApi.getInstance().request(buffer.toByteArray(), new MistApi.RequestCb() {
             Sandbox.EmitCb cb;

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

            private MistApi.RequestCb init(Sandbox.EmitCb callback) {
                this.cb = callback;
                return this;
            }
        }.init(callback));
    }
}
