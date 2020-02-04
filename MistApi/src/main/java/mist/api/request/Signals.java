package mist.api.request;

import org.bson.BsonDocument;

import mist.api.MistApi;

public class Signals {


    public static int ready(ReadyCb callback) {
        return SignalsReady.request(callback);
    }

    public static int sandbox(SandboxCb callback) {
        return SignalsSandboxedSettings.request(callback);
    }

    public abstract static class ReadyCb extends Callback {
        public abstract void cb(boolean state);
    }

    public abstract static class SandboxCb extends Callback {
        public void cb(byte[] id, String hint) {};
        public void cb(byte[] id, String hint, BsonDocument opts) {};
    }


    public static void cancel(int id) {
        MistApi.getInstance().requestCancel(id);
    }
}

