package mist.api;


import android.content.Context;
import android.util.Log;

import org.bson.BsonDocument;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import addon.AddonException;
import addon.WishFile;
import mist.api.request.*;
import mist.node.MistNode;

/**
 * Created by jeppe on 10/27/17.
 */

/* Re-generate interface:
javah -classpath ../../../../MistApi/build/intermediates/classes/debug/:/home/jan/Android/Sdk/platforms/android-16/android.jar -o mist_api_jni.h mist.api.MistApi
 */

public class MistApi {
    public final String TAG = "MistApi";
    /**
     * startMistApi return value for success
     */
    private static final int MIST_API_SUCCESS = 0;
    /**
     * startMistApi return error return if started multiple times
     */
    private static final int MIST_API_ERROR_MULTIPLE_TIMES = -1;
    /**
     * startMistApi return error return for other errors
     */
    private static final int MIST_API_ERROR_UNSPECIFIED = -10;

    private static List<Error> errorHandleList = new ArrayList<>();

    private int signalsId = 0;

    static {
        System.loadLibrary("mist");
    }

    private MistApi() {
    }

    private Timer wishAppPeriodicTimer;

    private static class MistApiHolder {
        private static final MistApi INSTANCE = new MistApi();
    }

    public static MistApi getInstance() {
        return MistApiHolder.INSTANCE;
    }

    private Context context;

    public synchronized void startMistApi(final Context context) {
        this.context = context;

        String appName = context.getPackageName();
        if (appName.length() > 32) {
            appName = appName.substring(0, 32);
        }

        int ret = startMistApi(appName, MistNode.getInstance(), new WishFile(context));

        if (ret != MIST_API_SUCCESS) {
            if (ret == MIST_API_ERROR_MULTIPLE_TIMES) {
                //throw new AddonException("MistApi cannot be started multiple times.");
            } else {
                throw new AddonException("Unspecified MistApi start error.");
            }
        } else {
           signalsId = Signals.sandbox(new Signals.SandboxCb() {
                @Override
                public void cb(byte[] id, String hint) {
                    super.cb(id, hint);
                    if (hint.equals("commission.refresh")) {
                        Wifi.listWifi(context);
                    }
                }
            });
        }

        wishAppPeriodicTimer = new Timer("wish_app periodic timer", true);
        /*
        wishAppPeriodicTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                signalWishAppPeriodicTick();
            }
        }, 1000, 1000);
        */

        Thread mistPeriodicTimerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    signalWishAppPeriodicTick();
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ie) {
                        Log.d(TAG, "mistPeriodicTimerThread was interrupted while waiting for the interval.");
                    }
                }
            }
        }, "mistPeriodicTimerThread");
        mistPeriodicTimerThread.setDaemon(true);
        mistPeriodicTimerThread.start();
    }

    /**
     * Initialise a Mist app that will use the Mist API interface
     * <p>
     * Note: MistNode instance is currently needed, because it has the read, write, invoke callbacks which mist-c99 will
     * invoke when handling an incoming mist request.
     *
     * @param appName The app name
     * @return MIST_API_SUCCESS, for a successful start, or MIST_API_ERROR_MULTIPLE_TIMES for error
     */
    native int startMistApi(String appName, MistNode mistNode, WishFile wishFile);

    //todo fix native implementation
    public void stopMistApi() {
        if (signalsId != 0) {
            Mist.cancel(signalsId);
        }
        nativeStopMistApi();
    }

    native void nativeStopMistApi();

    /**
     * Make a MistApi request
     *
     * @param req
     * @param cb
     * @return the RPC id for the request, or 0 for an error.
     */
    public native int request(byte[] req, RequestCb cb);

    public native void requestCancel(int id);

    /**
     * Make a Sandboxed request
     *
     * @param req
     * @param cb
     * @return the RPC id for the request, or 0 for an error.
     */
    public native int sandboxedRequest(byte[] sandboxId, byte[] req, RequestCb cb);

    public native void sandboxedRequestCancel(byte[] sandboxId, int id);

    static void registerRpcErrorHandler(Error error) {
        synchronized (errorHandleList) {
            errorHandleList.add(error);
        }
    }

    interface Error {
        public void cb(int code, String msg);
    }

    public abstract static class RequestCb {

        /**
         * The callback invoked when "ack" is received for a RPC request
         *
         * @param data a document containing RPC return value as 'data' element
         */
        public void ack(byte[] data) {
            response(data);
            end();
        }

        ;

        /**
         * The callback invoked when "sig" is received for a RPC request
         *
         * @param data the contents of 'data' element of the RPC reply
         */
        public void sig(byte[] data) {
            response(data);
        }

        ;

        public abstract void response(byte[] data);

        public abstract void end();

        /**
         * The callback invoked when "err" is received for a failed RPC request
         *
         * @param code the error code
         * @param msg  a free-text error message
         */
        public void err(int code, String msg) {
            synchronized (errorHandleList) {
                for (Error error : errorHandleList) {
                    error.cb(code, msg);
                }
            }
        }
    }


    public void joinWifi(String ssid, String password) {
        Wifi.joinWifi(context, ssid, password);
    }


    public native void wifiJoinResultCb(int status);

    public native void signalWishAppPeriodicTick();

}
