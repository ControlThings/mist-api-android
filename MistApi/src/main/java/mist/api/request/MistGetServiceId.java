package mist.api.request;

import org.bson.BSONException;
import org.bson.BsonBinaryWriter;
import org.bson.BsonDocument;
import org.bson.BsonWriter;
import org.bson.RawBsonDocument;
import org.bson.io.BasicOutputBuffer;

import mist.api.MistApi;

/**
 * Created by jan on 11/30/16.
 */

class MistGetServiceId {
    static int request(Mist.GetServiceIdCb callback) {
        final String op = "getServiceId";

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
            Mist.GetServiceIdCb cb;

            @Override
            public void response(byte[] data) {
                byte[] wsid;
                try {
                    BsonDocument bson = new RawBsonDocument(data);
                    wsid = bson.get("data").asDocument().get("wsid").asBinary().getData();
                } catch (BSONException e) {
                    cb.err(Callback.BSON_ERROR_CODE, Callback.BSON_ERROR_STRING);
                    return;
                }
                cb.cb(wsid);
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

            private MistApi.RequestCb init(Mist.GetServiceIdCb callback) {
                this.cb = callback;
                return this;
            }
        }.init(callback));
    }
}
