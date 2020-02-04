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

public class Commission {

    public static int add(Add.Hint hint, String ssid, AddCb callback) {
        return CommissionAdd.request(hint, ssid, callback);
    }

    public abstract static class AddCb extends Callback {
        public abstract void cb(boolean value);
    }

    public static class Add {
        public enum Hint {
            wifi("wifi");

            private String type;

            private Hint(String type) {
                this.type = type;
            }

            public String getType() {
                return type;
            }
        }
    }

    public static int getState(GetStateCb cb) {
        return CommissionGetState.request(cb);
    }

    public abstract static class GetStateCb extends Callback {
        public abstract void cb(String currentState);
    }


}
