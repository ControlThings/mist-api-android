package mist.api.request;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.bson.BSONException;
import org.bson.BsonBinary;
import org.bson.BsonBinaryWriter;
import org.bson.BsonDocument;
import org.bson.BsonDocumentReader;
import org.bson.BsonString;
import org.bson.BsonWriter;
import org.bson.RawBsonDocument;
import org.bson.io.BasicOutputBuffer;

import java.util.Arrays;
import java.util.List;

import wish.Peer;
import wish.request.Connection;
import wish.request.Host;
import wish.request.Identity;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

import static mist.api.request.Callback.BSON_ERROR_CODE;
import static mist.api.request.Callback.BSON_ERROR_STRING;

class DirectoryPublishService {

    private final static String TAG = "DirectoryPublish";
    private final String op = "directory.publish";
    private final String timestamp = "time";
    private final int NORMAL_CLOSURE_STATUS = 1000;
    private final int publishId = 12;
    private final int timeId = 22;
    private final int wsErrorCode = 743;

    byte[] signedData;
    BsonDocument document;
    Peer peer;
    wish.Connection _connection;
    byte[] uid;

    Directory.PublishServiceCb callback;


    void request(final Request request, final BsonDocument document, final Peer peer,final Directory.PublishServiceCb callback) {

        this.document = document;
        this.peer = peer;
        this.callback = callback;
        this.uid = peer.getRuid();

        Host.config(new Host.ConfigCb() {
            @Override
            public void cb(String version, byte[] hid) {
                if (Arrays.equals(hid, peer.getRhid()) && Arrays.equals(peer.getLuid(), peer.getRuid())) {
                    uid = peer.getLuid();
                    _connection = null;
                    OkHttpClient client = new OkHttpClient();
                    DirectoryPublishService.SocketListener listener = new DirectoryPublishService.SocketListener();
                    client.newWebSocket(request, listener);
                    client.dispatcher().executorService().shutdown();
                } else {
                    Connection.list(new Connection.ListCb() {
                        @Override
                        public void cb(List<wish.Connection> connections) {
                            for (wish.Connection connection: connections ) {
                                if (Arrays.equals(connection.getLuid(), peer.getLuid()) && Arrays.equals(connection.getRuid(), peer.getRuid()) && Arrays.equals(connection.getRhid(), peer.getRhid())) {
                                    _connection = connection;
                                    OkHttpClient client = new OkHttpClient();
                                    DirectoryPublishService.SocketListener listener = new DirectoryPublishService.SocketListener();
                                    client.newWebSocket(request, listener);
                                    client.dispatcher().executorService().shutdown();
                                    break;
                                }
                            }
                        }
                        @Override
                        public void err(final int code, final String msg) {
                            Log.d(op, "RPC error: " + msg + " code: " + code);
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    callback.err(code, msg);
                                }
                            });
                        }

                        @Override
                        public void end() {}
                    });
                }
            }

            @Override
            public void err(int code, String msg) {

            }

            @Override
            public void end() {

            }
        });







    }


    final class SocketListener extends WebSocketListener {
        @Override
        public void onOpen(final WebSocket webSocket, Response response) {
            super.onOpen(webSocket, response);

            Identity.export(_connection, uid, new Identity.ExportCb() {

                @Override
                public void cb(byte[] data, byte[] raw) {

                    BsonDocument bson = new RawBsonDocument(raw);
                    BsonDocument dataDocument = bson.getDocument("data");



                    BsonDocument dataDataDocument = new RawBsonDocument(dataDocument.get("data").asBinary().getData());
                    BsonDocumentReader dataReader = new BsonDocumentReader(dataDataDocument);

                    //create new data object
                    BasicOutputBuffer buffer = new BasicOutputBuffer();
                    BsonWriter writer = new BsonBinaryWriter(buffer);
                    writer.writeStartDocument();
                    writer.writeName("data");
                    writer.pipe(dataReader);
                    writer.writeEndDocument();
                    writer.flush();

                    //create tmporary bson object
                    BsonDocument tmpDocument = new RawBsonDocument(buffer.toByteArray());
                    BsonDocument tmpDataDocument = tmpDocument.get("data").asDocument();

                    tmpDataDocument.append("hid", new BsonBinary(peer.getRhid()));
                    tmpDataDocument.append("sid", new BsonBinary(peer.getRsid()));
                    tmpDataDocument.append("protocol", new BsonString(peer.getProtocol()));

                    //add "meta" field to object
                    if (document != null) {
                        tmpDataDocument.append("meta", document);
                    }

                    //create binary buffer of object
                    BsonDocumentReader reader = new BsonDocumentReader(tmpDataDocument);
                    BasicOutputBuffer buffe = new BasicOutputBuffer();
                    BsonWriter write = new BsonBinaryWriter(buffe);
                    write.pipe(reader);
                    writer.flush();

                    //replace old data white new data including "meta"
                    dataDocument.put("data", new BsonBinary(buffe.toByteArray()));

                    Identity.sign(_connection, uid, dataDocument, new Identity.SignCb() {
                        @Override
                        public void cb(byte[] data) {
                            signedData = data;

                            BasicOutputBuffer buffer = new BasicOutputBuffer();
                            BsonWriter writer = new BsonBinaryWriter(buffer);
                            writer.writeStartDocument();
                            writer.writeString("op", timestamp);
                            writer.writeStartArray("args");
                            writer.writeEndArray();
                            writer.writeInt32("id", timeId);
                            writer.writeEndDocument();
                            writer.flush();

                            webSocket.send(ByteString.of(buffer.toByteArray()));
                        }

                        @Override
                        public void err(final int code, final String msg) {
                            Log.d(op, "RPC error: " + msg + " code: " + code);
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    callback.err(code, msg);
                                }
                            });

                        }

                        @Override
                        public void end() {}
                    });

                }

                @Override
                public void err(final int code, final String msg) {
                    Log.d(op, "RPC error: " + msg + " code: " + code);
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            callback.err(code, msg);
                        }
                    });
                }

                @Override
                public void end() {}
            });
        }

        @Override
        public void onMessage(WebSocket webSocket, ByteString bytes) {
            super.onMessage(webSocket, bytes);
            try {
                BsonDocument bson = new RawBsonDocument(bytes.toByteArray());
                if (bson.containsKey("ack")) {
                    int ack = bson.get("ack").asInt32().getValue();
                    if (ack == timeId) {
                        if (bson.containsKey("data")) {
                            double timestamp = bson.get("data").asDouble().getValue();

                            BasicOutputBuffer claimBuffer = new BasicOutputBuffer();
                            BsonWriter cWriter = new BsonBinaryWriter(claimBuffer);

                            cWriter.writeStartDocument();
                            cWriter.writeBinaryData("uid", new BsonBinary(uid));
                            cWriter.writeDouble("timestamp", timestamp);
                            cWriter.writeEndDocument();

                            BasicOutputBuffer buffer = new BasicOutputBuffer();
                            BsonWriter writer = new BsonBinaryWriter(buffer);

                            writer.writeStartDocument();
                            writer.writeString("op", op);
                            writer.writeStartArray("args");

                            BsonDocumentReader reader = new BsonDocumentReader(new RawBsonDocument(signedData));
                            writer.pipe(reader);

                            writer.writeBinaryData(new BsonBinary(claimBuffer.toByteArray()));

                            writer.writeEndArray();
                            writer.writeInt32("id", publishId);
                            writer.writeEndDocument();

                            writer.flush();

                            webSocket.send(ByteString.of(buffer.toByteArray()));
                        }
                    }
                    else if (ack == publishId) {

                        if(bson.containsKey("data")) {
                            webSocket.close(NORMAL_CLOSURE_STATUS, "Goodbye !");
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                private BsonDocument bson;

                                @Override
                                public void run() {
                                    callback.cb(bson.get("data").asBoolean().getValue());
                                }

                                private Runnable init(BsonDocument bson) {
                                    this.bson = bson;
                                    return this;
                                }
                            }.init(bson));
                        } else {
                            webSocket.close(NORMAL_CLOSURE_STATUS, "Goodbye !");
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    callback.cb(false);
                                }
                            });
                        }
                    }
                    else {
                        webSocket.close(NORMAL_CLOSURE_STATUS, "Goodbye !");
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                callback.cb(false);
                            }
                        });
                    }
                } else {
                    webSocket.close(NORMAL_CLOSURE_STATUS, "Goodbye !");
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            callback.cb(false);
                        }
                    });
                }

            } catch (final BSONException e) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        callback.err(BSON_ERROR_CODE, BSON_ERROR_STRING);

                    }
                });
            }
        }

        @Override
        public void onClosing(WebSocket webSocket, int code, String reason) {
            super.onClosing(webSocket, code, reason);
        }

        @Override
        public void onFailure(WebSocket webSocket, final Throwable t, Response response) {
            super.onFailure(webSocket, t, response);
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    callback.err(wsErrorCode, t.getMessage());
                }
            });

        }

        @Override
        public void onClosed(WebSocket webSocket, int code, String reason) {
            super.onClosed(webSocket, code, reason);
        }


    }

}
