package mist.api.request;

import org.bson.BsonBinary;
import org.bson.BsonBinaryWriter;
import org.bson.BsonWriter;
import org.bson.io.BasicOutputBuffer;

import mist.api.MistApi;
import wish.Peer;

/**
 * Created by jeppe on 8/23/16.
 */
class SandboxAddPeer {
    static int request(byte[] id, Peer peer, Sandbox.AddPeerCb callback) {
        final String op = "sandbox.addPeer";

        BasicOutputBuffer buffer = new BasicOutputBuffer();
        BsonWriter writer = new BsonBinaryWriter(buffer);
        writer.writeStartDocument();

        writer.writeString("op", op);

        writer.writeStartArray("args");

        writer.writeBinaryData(new BsonBinary(id));

        writer.writeStartDocument();
        writer.writeBinaryData("luid", new BsonBinary(peer.getLuid()));
        writer.writeBinaryData("ruid", new BsonBinary(peer.getRuid()));
        writer.writeBinaryData("rhid", new BsonBinary(peer.getRhid()));
        writer.writeBinaryData("rsid", new BsonBinary(peer.getRsid()));
        writer.writeString("protocol", peer.getProtocol());
        writer.writeBoolean("online", peer.isOnline());
        writer.writeEndDocument();
        writer.writeEndArray();
        writer.writeInt32("id", 0);
        writer.writeEndDocument();
        writer.flush();

        return MistApi.getInstance().request(buffer.toByteArray(), new MistApi.RequestCb() {
            Sandbox.AddPeerCb cb;

            @Override
            public void response(byte[] data) {
                cb.cb();
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

            private MistApi.RequestCb init(Sandbox.AddPeerCb callback) {
                this.cb = callback;
                return this;
            }

        }.init(callback));
    }
}
