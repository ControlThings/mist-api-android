package mist.api.request;

import org.bson.BSONException;
import org.bson.BsonArray;
import org.bson.BsonBinary;
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

import static mist.api.request.Callback.BSON_ERROR_CODE;
import static mist.api.request.Callback.BSON_ERROR_STRING;

class SandboxListPeers {
    static int request(byte[] id, Sandbox.ListPeersCb callback) {
        final String op = "sandbox.listPeers";

        BasicOutputBuffer buffer = new BasicOutputBuffer();
        BsonWriter writer = new BsonBinaryWriter(buffer);
        writer.writeStartDocument();

        writer.writeString("op", op);

        writer.writeStartArray("args");
        writer.writeBinaryData(new BsonBinary(id));
        writer.writeEndArray();

        writer.writeInt32("id", 0);

        writer.writeEndDocument();
        writer.flush();


        return MistApi.getInstance().request(buffer.toByteArray(), new MistApi.RequestCb() {
            Sandbox.ListPeersCb cb;

            @Override
            public void response(byte[] data) {
                List<Peer> peers;
                try {
                    peers = new ArrayList<Peer>();
                    BsonDocument bson = new RawBsonDocument(data);
                    BsonArray bsonArray = bson.getArray("data");
                    for (BsonValue entry : bsonArray) {
                        BsonDocument peerDocument = entry.asDocument();
                        Peer peer = Peer.fromBson(peerDocument);
                        peers.add(peer);
                    }
                } catch (BSONException e) {
                    cb.err(BSON_ERROR_CODE, BSON_ERROR_STRING);
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

            private MistApi.RequestCb init(Sandbox.ListPeersCb callback) {
                this.cb = callback;
                return this;
            }
        }.init(callback));
    }
}
