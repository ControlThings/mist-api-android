package mist.api.request;

import org.bson.BSONException;
import org.bson.BsonArray;
import org.bson.BsonBinaryWriter;
import org.bson.BsonDocument;
import org.bson.BsonValue;
import org.bson.BsonWriter;
import org.bson.RawBsonDocument;
import org.bson.io.BasicOutputBuffer;

import java.util.ArrayList;
import java.util.List;

import mist.api.MistApi;

class SandboxList {
    static int request(Sandbox.ListCb callback) {
        final String op = "sandbox.list";

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
            Sandbox.ListCb cb;

            @Override
            public void response(byte[] data) {
                List<mist.api.Sandbox> sandboxes;
                try {
                    BsonDocument bson = new RawBsonDocument(data);
                    sandboxes = new ArrayList<>();
                    BsonArray bsonIdentityList = new BsonArray(bson.getArray("data"));
                    for (BsonValue bsonValue : bsonIdentityList) {
                        mist.api.Sandbox sandbox = new mist.api.Sandbox();
                        sandbox.setName(bsonValue.asDocument().get("name").asString().getValue());
                        sandbox.setId(bsonValue.asDocument().get("id").asBinary().getData());
                        sandbox.setOnline(bsonValue.asDocument().get("online").asBoolean().getValue());
                        sandboxes.add(sandbox);
                    }
                } catch (BSONException e) {
                    cb.err(Callback.BSON_ERROR_CODE, Callback.BSON_ERROR_STRING);
                    return;
                }
                cb.cb(sandboxes);
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

            private MistApi.RequestCb init(Sandbox.ListCb callback) {
                this.cb = callback;
                return this;
            }
        }.init(callback));
    }
}
