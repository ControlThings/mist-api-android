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
