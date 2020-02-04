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

import java.util.ArrayList;

import wish.Cert;
import wish.Peer;
import okhttp3.Request;

/**
 * Created by jeppe on 8/14/17.
 */



public class Directory {

    private static final String url = "wss://mist.controlthings.fi:3030";

    public static void find(String alias, Directory.FindCb callback) {
        Request request = new Request.Builder().url(url).build();
        DirectoryFind directoryFind = new DirectoryFind();
        directoryFind.request(request, alias, callback);
    }

    public static void publishIdentity(byte[] uid, PublishIdentityCb callback) {
        Request request = new Request.Builder().url(url).build();
        DirectoryPublishIdentity directoryPublishIdentity = new DirectoryPublishIdentity();
        directoryPublishIdentity.request(request, null, uid, callback);
    }


    public static void publishIdentity(byte[] uid, BsonDocument document, PublishIdentityCb callback) {
        Request request = new Request.Builder().url(url).build();
        DirectoryPublishIdentity directoryPublishIdentity = new DirectoryPublishIdentity();
        directoryPublishIdentity.request(request, document, uid, callback);
    }

    public static void publishService(Peer peer, PublishServiceCb callback) {
        Request request = new Request.Builder().url(url).build();
        DirectoryPublishService directoryPublishService = new DirectoryPublishService();
        directoryPublishService.request(request, null, peer, callback);
    }


    public static void publishService(Peer peer, BsonDocument document, PublishServiceCb callback) {
        Request request = new Request.Builder().url(url).build();
        DirectoryPublishService directoryPublishService = new DirectoryPublishService();
        directoryPublishService.request(request, document, peer, callback);
    }

    public abstract static class FindCb extends Callback {
        public abstract void cb(ArrayList<Cert> certs);
    }

    public abstract static class PublishIdentityCb extends Callback {
        public abstract void cb(boolean state);
    }

    public abstract static class PublishServiceCb extends Callback {
        public abstract void cb(boolean state);
    }
}
