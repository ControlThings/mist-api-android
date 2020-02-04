package mist.api.request;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.bson.BSONException;
import org.bson.BsonArray;
import org.bson.BsonBinaryWriter;
import org.bson.BsonDocument;
import org.bson.BsonValue;
import org.bson.BsonWriter;
import org.bson.RawBsonDocument;
import org.bson.io.BasicOutputBuffer;

import java.util.ArrayList;

import wish.Cert;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

import static mist.api.request.Callback.BSON_ERROR_CODE;
import static mist.api.request.Callback.BSON_ERROR_STRING;

class DirectoryFind {

    private final static String TAG = "DirectoryFind";
    private final String op = "directory.find";
    private final int NORMAL_CLOSURE_STATUS = 1000;
    private final int id = 11;
    private final int wsErrorCode = 743;
    private String alias = "";
    private final String typeContact = "bson/wish-contact";
    private final String typeService = "bson/mist-node";

    Directory.FindCb callback;

    void request(Request request, String alias, Directory.FindCb callback) {

        this.callback = callback;
        this.alias = alias;

        OkHttpClient client = new OkHttpClient();
        SocketListener listener = new SocketListener();
        client.newWebSocket(request, listener);
        client.dispatcher().executorService().shutdown();
    }


    final class SocketListener extends WebSocketListener {
        @Override
        public void onOpen(WebSocket webSocket, Response response) {
            super.onOpen(webSocket, response);

            BasicOutputBuffer buffer = new BasicOutputBuffer();
            BsonWriter writer = new BsonBinaryWriter(buffer);
            writer.writeStartDocument();
            writer.writeString("op", op);
            writer.writeStartArray("args");
            writer.writeString(alias);
            writer.writeEndArray();
            writer.writeInt32("id", id);
            writer.writeEndDocument();
            writer.flush();

            Log.d(TAG, "onOpen " + response);
            webSocket.send(ByteString.of(buffer.toByteArray()));

        }

        @Override
        public void onMessage(WebSocket webSocket, okio.ByteString bytes) {
            super.onMessage(webSocket, bytes);
            Log.d(TAG, "onMessage b " + bytes);

            ArrayList<Cert> certs = new ArrayList<>();
            try {
                BsonDocument bson = new RawBsonDocument(bytes.toByteArray());
                BsonArray bsonArray = bson.get("data").asArray();
                int id = bson.get("ack").asInt32().getValue();
                for (BsonValue listValue : bsonArray) {
                    if (listValue.asDocument().containsKey("alias") && listValue.asDocument().containsKey("type") && listValue.asDocument().containsKey("cert")) {
                        String type = listValue.asDocument().get("type").asString().getValue();
                        if (type.equals(typeContact) || type.equals(typeService)) {
                            Cert cert = new Cert();
                            cert.setAlias(listValue.asDocument().get("alias").asString().getValue());
                            cert.setCert(listValue.asDocument().getDocument("cert"));
                            certs.add(cert);
                        }
                    }
                }
            } catch (final BSONException e) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        callback.err(BSON_ERROR_CODE, BSON_ERROR_STRING);
                    }
                });
                webSocket.close(NORMAL_CLOSURE_STATUS, "Goodbye !");
            }

            new Handler(Looper.getMainLooper()).post(new Runnable() {
                private ArrayList<Cert> certs;

                @Override
                public void run() {
                    callback.cb(certs);
                }
                private Runnable init(ArrayList<Cert> certs) {
                    this.certs = certs;
                    return this;
                }
            }.init(certs));


            webSocket.close(NORMAL_CLOSURE_STATUS, "Goodbye !");
        }

        @Override
        public void onClosing(WebSocket webSocket, int code, String reason) {
            super.onClosing(webSocket, code, reason);
            Log.d(TAG, "onClosing code " + code + " reson  " + reason);
        }

        @Override
        public void onFailure(WebSocket webSocket,final Throwable t, Response response) {
            super.onFailure(webSocket, t, response);
            Log.d(TAG, "onFailure msg " + t.getMessage() + " res " + response);
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
            Log.d(TAG, "onClosed code " + " reason " + reason);
        }


    }

}
