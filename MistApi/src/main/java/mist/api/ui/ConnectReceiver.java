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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.HashMap;

/**
 * Created by jeppe on 11/23/16.
 */

public class ConnectReceiver extends BroadcastReceiver {

    private HashMap<Integer, CustomUi.ConnectListener> listenerMap = new HashMap<Integer, CustomUi.ConnectListener>();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("onReceive", "++++++++");
    }

    public static void register(int id, CustomUi.ConnectListener listener) {
        //add
    }

    private void add() {

    }
}
