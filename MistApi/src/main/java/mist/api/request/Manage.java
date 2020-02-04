package mist.api.request;

import wish.Peer;

/**
 * Created by jeppe on 11/30/16.
 */

public class Manage {

    public static void claim(Peer peer, ClaimCb callback) {
        ManageClaim.request(peer, callback);
    }

    public abstract static class ClaimCb extends Callback {
        public void cb() {};
    }
}
