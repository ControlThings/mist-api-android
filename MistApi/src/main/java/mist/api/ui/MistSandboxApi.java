package mist.api.ui;

/**
 * Created by jeppe on 11/18/16.
 */

class MistSandboxApi {


    private MistSandboxApi() {}


    private static class MistSandboxApiHolder {
        private static final MistSandboxApi INSTANCE = new MistSandboxApi();
    }

    public static MistSandboxApi getInstance() {
        return MistSandboxApi.MistSandboxApiHolder.INSTANCE;
    }

    synchronized native void register(int id, Response response);
    synchronized native void sandboxSouth(int id, byte[] bson);

    interface Response {
        public void sandboxNorth(byte[] bson);
    }
}
