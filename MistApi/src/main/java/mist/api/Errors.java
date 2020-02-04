package mist.api;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import mist.node.MistNode;


/**
 * Created by jeppe on 3/2/17.
 */
public class Errors {

    private static Errors instance;

    static Errors getInstance() {
        if (instance == null) {
            instance = new Errors();
        }
        return instance;
    }


    private int id;
    private HashMap<Integer, ListenCb> nodeCbHashMap;

    private Errors() {
        id = 1;
        nodeCbHashMap = new HashMap<>();
        MistApi.getInstance().registerRpcErrorHandler(new MistApi.Error() {
            @Override
            public void cb(int code, String msg) {
                error(code, msg);
            }
        });
    }

    private int registerCb(ListenCb cb) {
        nodeCbHashMap.put(id, cb);
        return id++;
    }

    public static int listen(ListenCb callback) {
        return getInstance().registerCb(callback);
    }

    public interface ListenCb {
        public void cb(int code, String msg);
    }


    public static void cancel(int id) {
        if (getInstance().nodeCbHashMap.containsKey(id)) {
            getInstance().nodeCbHashMap.remove(id);
            return;
        }
    }

    private static void error(int code, String msg) {
        Iterator iterator = getInstance().nodeCbHashMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry pair = (Map.Entry) iterator.next();
            ListenCb mistCb = (ListenCb) pair.getValue();
            mistCb.cb(code, msg);
            iterator.remove();
        }
    }
    
}
