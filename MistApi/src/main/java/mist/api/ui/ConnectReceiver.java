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
