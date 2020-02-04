package mist.api.request;

import android.util.Log;

import org.bson.BSONException;
import org.bson.BsonBinaryWriter;
import org.bson.BsonDocument;
import org.bson.BsonDocumentReader;
import org.bson.BsonInt32;
import org.bson.BsonString;
import org.bson.BsonWriter;
import org.bson.RawBsonDocument;
import org.bson.io.BasicOutputBuffer;

import java.util.HashMap;

import mist.api.MistApi;

public class RawRequest {

    private final static String TAG = "MistRawRequest";

    private final static HashMap<Integer, Integer> requestMap = new HashMap<>();

    public static void request(byte[] bson, RawRequestCb callback) {

        String op;
        int passthruId = 0;
        int requestId;
        BsonDocument argsDocument = new BsonDocument();

        final BsonDocument bsonDocument = new RawBsonDocument(bson);
        try {

            if (bsonDocument.containsKey("end")) {
                int cancelId = bsonDocument.getInt32("end").getValue();
                MistApi.getInstance().requestCancel(cancelId);
                requestMap.remove(cancelId);
            } else {
                passthruId = bsonDocument.getInt32("id").getValue();
                op = bsonDocument.getString("op").getValue();

                argsDocument.append("args", bsonDocument.getArray("args"));
                argsDocument.append("op", new BsonString(op));
                argsDocument.append("id", new BsonInt32(0));

                BasicOutputBuffer requestBuffer = new BasicOutputBuffer();
                try {
                    BsonWriter writer = new BsonBinaryWriter(requestBuffer);
                    BsonDocumentReader bsonDocumentReader = new BsonDocumentReader(argsDocument);
                    writer.pipe(bsonDocumentReader);
                    writer.flush();
                } catch (Exception e) {
                    error(callback, passthruId, e.getMessage(), 35);
                }

                MistApi.RequestCb requestCb = new MistApi.RequestCb() {
                    private int passthruId;
                    private RawRequestCb callback;

                    @Override
                    public void ack(byte[] data) {
                        BsonDocument document  = new BsonDocument();
                        try {
                            document.append("ack", new BsonInt32(passthruId));
                            if (new RawBsonDocument(data).containsKey("data")) {
                                document.append("data", new RawBsonDocument(data).get("data"));
                            }
                        } catch (BSONException e) {
                            err(37, "error parsing ack: " + e.getMessage());
                            return;
                        }
                        response(document);
                        requestMap.remove(passthruId);
                    }

                    @Override
                    public void sig(byte[] data) {
                        BsonDocument document  = new BsonDocument();
                        try {
                            document.append("sig", new BsonInt32(passthruId));
                            if (new RawBsonDocument(data).containsKey("data")) {
                                document.append("data", new RawBsonDocument(data).get("data"));
                            }
                        } catch (BSONException e) {
                            err(36, "error parsing sig: " + e.getMessage());
                            return;
                        }
                        response(document);
                    }

                    @Override
                    public void err(int code, String msg) {
                        BsonDocument document  = new BsonDocument();
                        BsonDocument errMsg  = new BsonDocument();
                        errMsg.append("msg", new BsonString(msg));
                        errMsg.append("code", new BsonInt32(code));

                        try {
                            document.append("err", new BsonInt32(passthruId));
                            document.append("data", errMsg);
                        } catch (BSONException e) {
                            Log.d(TAG, "error parsing ack: " + e.getMessage());
                        }
                        response(document);
                        requestMap.remove(passthruId);
                    }

                    private void response(BsonDocument document) {
                        BasicOutputBuffer buffer = new BasicOutputBuffer();
                        BsonWriter writer = new BsonBinaryWriter(buffer);
                        BsonDocumentReader bsonDocumentReader = new BsonDocumentReader(document);
                        writer.pipe(bsonDocumentReader);
                        writer.flush();
                        callback.cb(buffer.toByteArray());
                    }

                    @Override
                    public void response(byte[] data) {}

                    @Override
                    public void end() {}

                    private MistApi.RequestCb init(int requestId, RawRequestCb callback) {
                        this.passthruId = requestId;
                        this.callback = callback;
                        return this;
                    }
                }.init(passthruId, callback);
                requestId = MistApi.getInstance().request(requestBuffer.toByteArray(), requestCb);
                requestMap.put(passthruId, requestId);
            }


        } catch (Exception e) {
            error(callback, passthruId, e.getMessage(), 34);
        }
    }

    private static void error(RawRequestCb callback, int passthruId, String msg, int code ) {
        if (passthruId == 0) {
            Log.d(TAG, "error: " + msg);
            return;
        }
        BsonDocument document  = new BsonDocument();
        BsonDocument errMsg  = new BsonDocument();
        errMsg.append("msg", new BsonString(msg));
        errMsg.append("code", new BsonInt32(code));
        document.append("err", new BsonInt32(passthruId));
        document.append("data", errMsg);

        BasicOutputBuffer buffer = new BasicOutputBuffer();
        BsonWriter writer = new BsonBinaryWriter(buffer);
        BsonDocumentReader bsonDocumentReader = new BsonDocumentReader(document);
        writer.pipe(bsonDocumentReader);
        writer.flush();
        callback.cb(buffer.toByteArray());

        requestMap.remove(passthruId);
    }

    public interface RawRequestCb {
        void cb(byte[] bson);
    }
}

