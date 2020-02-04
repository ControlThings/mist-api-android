package mist.api.request;

import org.bson.BsonDocument;

import java.util.ArrayList;
import java.util.List;

import wish.Peer;

/**
 * Created by jeppe on 11/30/16.
 */

public class Sandbox {

    public static int list(ListCb callback) {
        return SandboxList.request(callback);
    }

    public static int listPeers(byte[] sandboxId, ListPeersCb callback) {
       return SandboxListPeers.request(sandboxId, callback);
    }

    public static int addPeer(byte[] sandboxId, Peer peer, AddPeerCb callback) {
       return SandboxAddPeer.request(sandboxId, peer, callback);
    }


    public static int removePeer(byte[] sandboxId, Peer peer, RemovePeerCb callback) {
       return SandboxRemovePeer.request(sandboxId, peer, callback);
    }

    public static int create(byte[] sandboxId, String name, CreateCb callback) {
       return SandboxCreate.request(sandboxId, name, callback);
    }

    public static int emit(byte[] sandboxId, String hint, EmitCb callback) {
        return SandboxEmit.request(sandboxId, hint, null, callback);
    }

    public static int emit(byte[] sandboxId, String hint, BsonDocument document, EmitCb callback) {
        return SandboxEmit.request(sandboxId, hint, document, callback);
    }

    public static int logout(byte[] sandboxId, LogoutCb callback) {
       return SandboxLogout.request(sandboxId, callback);
    }

    public static int remove(byte[] sandboxId, RemoveCb callback) {
       return SandboxRemove.request(sandboxId, callback);
    }

    public static int allowRequest(byte[] sandboxId, BsonDocument hint, AllowRequestCb callback) {
       return SandboxAllowRequest.request(sandboxId, hint, callback);
    }

    public static int denyRequest(byte[] sandboxId, BsonDocument hint, DenyRequestCb callback) {
      return SandboxDenyRequest.request(sandboxId, hint, callback);
    }

    public abstract static class ListCb extends Callback {
        public abstract void cb(List<mist.api.Sandbox> sandboxes);
    }

    public abstract static class ListPeersCb extends Callback {
        public abstract void cb(List<Peer> peers);
    }

    public abstract static class AddPeerCb extends Callback {
        public abstract void cb();
    }

    public abstract static class RemovePeerCb extends Callback {
        public abstract void cb();
    }

    public abstract static class CreateCb extends Callback {
        public abstract void cb(boolean data);
    }

    public abstract static class EmitCb extends Callback {
        public abstract void cb(boolean data);
    }

    public abstract static class LogoutCb extends Callback {
        public abstract void cb(boolean data);
    }

    public abstract static class RemoveCb extends Callback {
        public abstract void cb(boolean data);
    }

    public abstract static class AllowRequestCb extends Callback {
        public abstract void cb(boolean data);
    }

    public abstract static class DenyRequestCb extends Callback {
        public abstract void cb(boolean data);
    }
}

