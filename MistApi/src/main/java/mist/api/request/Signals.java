/**
 * Copyright (C) 2020, ControlThings Oy Ab
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * @license Apache-2.0
 */
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

