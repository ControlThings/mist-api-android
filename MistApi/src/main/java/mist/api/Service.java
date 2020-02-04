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
package mist.api;

import addon.AddonService;
import mist.api.MistApi;

/**
 * Created by jan on 11/7/17.
 */

public class Service extends AddonService {
    public void startAddon() {
        MistApi.getInstance().startMistApi(getBaseContext());
    }
}
