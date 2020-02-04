package mist.api.request;

import android.util.Log;

import org.bson.BsonDocument;

/**
 * Created by jeppe on 3/29/17.
 */

abstract class Callback {

    public static final int SIGNALS_ERROR_CODE = 243;
    public static final int BSON_ERROR_CODE = 836;
    public static final String BSON_ERROR_STRING = "Bad BSON structure";

    public void err(int code, String msg) {
        Log.d("Error", msg + ", code: " + code);
    };

    public void err(int code, String msg, BsonDocument bson) {
        Log.d("Error", msg + ", code: " + code + ", raw bson: " + bson.toJson());
    };

    public void end(){};
}
