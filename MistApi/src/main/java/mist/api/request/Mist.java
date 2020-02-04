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
import java.util.List;

import mist.api.MistApi;
import wish.Peer;

public class Mist {

    public static int signals(SignalsCb callback) {
        return MistSignals.request(callback);
    }

    public static int listServices(ListServicesCb callback) {
       return MistListServices.request(callback);
    }

    public static int getServiceId(GetServiceIdCb callback) {
       return MistGetServiceId.request(callback);
    }

    public static int ready(ReadyCb callback) {
       return MistReady.request(callback);
    }

    public static int version(VersionCb callback) {
       return MistVersion.request(callback);
    }

    public abstract static class SignalsCb extends Callback {
        public abstract void cb(String signal, BsonDocument document);
    }

    public abstract static class ListServicesCb extends Callback {
        public abstract void cb(List<Peer> peers);
    }

    public abstract static class GetServiceIdCb extends Callback {
        public abstract void cb(byte[] wsid);
    }

    public abstract static class ReadyCb extends Callback {
        public abstract void cb(boolean state);
    }

    public abstract static class VersionCb extends Callback {
        public abstract void cb(String version);
    }

    public static void cancel(int id) {
        MistApi.getInstance().requestCancel(id);
    }
}

