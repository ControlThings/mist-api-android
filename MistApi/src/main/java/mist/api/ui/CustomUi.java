package mist.api.ui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import wish.Peer;


/**
 * Created by jeppe on 11/16/16.
 */

public class CustomUi {

    private Context _context;
    private Activity _activity;
    private MistCustomApi api;
    private int _requestCode;

    private HashMap<String, File> fileMap;
    private String dir;
    private String infoDir;
    private List<Peer> filter;
    private int id = -1;
    private static final String connect_filter = "CUSTOM_UI_CONNECT";
    private ConnectListener connectListener = null;

    public CustomUi(Activity activity, int requestCode) {
        this._activity = activity;
        this._context = activity;
        this._requestCode = requestCode;
        api = MistCustomApi.getInstance();
        dir = _context.getFilesDir() + "/Ui/";
        infoDir = _context.getFilesDir() + "/UiInfo/";
        filter = new ArrayList<Peer>();
    }

    public void load(String md5) {
        initiate(md5);
    }

    public void load(Peer peer) {
        String[] md5 = api.getCustomUi(peer);
        if (md5.length == 1) {
            initiate(md5[0]);
        } else {

        }

    }

    public void load(Peer peer, ArrayList<Peer> filter) {
        String[] md5 = api.getCustomUi(peer);
        this.filter = filter;
        if (md5.length == 1) {
            initiate(md5[0]);
        } else {

        }
    }

    private void initiate(String md5) {
        File directory = new File(dir + md5);
        if (!directory.exists()) {
            if (hasUi(md5)) {
                InputStream stream = api.getRawCustomUi(md5);
                Install install = new Install(_context, new Install.Response() {
                    @Override
                    public void loadUi(String md5) {
                        startSandbox(md5);
                    }
                });
                install.execute(stream, dir, md5);
            } else {
                Toast.makeText(_context, "Couldn't find ui", Toast.LENGTH_LONG).show();
                return;
            }
        } else {
            startSandbox(md5);
        }
    }

    public boolean hasUi(String md5) {
        String[] list = api.listCustomUis(md5);
        if (Arrays.asList(list).contains(md5)) {
            return true;
        } else {
            return false;
        }
    }

    public void addUi(Uri fileUri, AddUiResponse res) {
        SaveInfo saveInfo = new SaveInfo(_context, new SaveInfo.Response() {

            @Override
            public void onInfoExists(AddUiResponse addUiResponse) {
                addUiResponse.exists();
            }

            @Override
            public void onInfo(Uri uri, byte[] bson, AddUiResponse addUiResponse) {
                try {
                    ContentResolver cr = _context.getContentResolver();
                    InputStream inputStream = cr.openInputStream(uri);
                    api.addCustomUi(inputStream, bson, addUiResponse);
                } catch (FileNotFoundException e) {
                    return;
                }
            }
        }, res);
        saveInfo.execute(fileUri, infoDir);
    }

    public void removeUi(String md5) {
        File file = new File(dir + md5);
        if (file.exists()) {
            Install.deleteDirectory(file);
        }
        api.removeCustomUi(md5);
    }

    public List<Ui> listUis() {
        String[] list = api.listCustomUis("test");
        List<Ui> uiList = new ArrayList<Ui>();
        for (String md5 : list) {
            Ui ui = new Ui();
            ui.setMd5(md5);
            File directory = new File(infoDir + md5);
            if (directory.exists()) {
                File jsonFile = new File(directory + "/package.json");
                if (jsonFile.exists()) {
                    try {
                        InputStream is = new FileInputStream(jsonFile);
                        int size = is.available();
                        byte[] buffer = new byte[size];
                        is.read(buffer);
                        is.close();
                        String json = new String(buffer, "UTF-8");
                        JSONObject jsonObject = new JSONObject(json);
                        ui.setInfo(jsonObject);
                        ui.setName(jsonObject.getString("name"));
                    } catch (Exception e) {

                    }
                }
                File imgFile = new File(directory + "/logo.img");
                if (imgFile.exists()) {
                    ui.setLogo(imgFile);
                }
            }
            uiList.add(ui);
        }
        return uiList;
    }

    public void registerConnectListener(ConnectListener listener) {

        _context.registerReceiver(receiver, new IntentFilter(connect_filter));
        this.connectListener = listener;
        //  ConnectReceiver

    }

    public void clean() {
        try {
            _context.unregisterReceiver(receiver);
        } catch (IllegalArgumentException e) {
        }
    }

    private void startSandbox(String md5) {
        id = api.loadCustomUi(md5, filter);
        Log.d("startBox", id + "");
        if (id == -1) {
            Toast.makeText(_context, "Couldn't start ui", Toast.LENGTH_LONG).show();
            return;
        }
        Intent intent = new Intent(_context, Sandbox.class);
        intent.putExtra("dir", dir + md5);
        intent.putExtra("id", id);
        _activity.startActivityForResult(intent, _requestCode);
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equalsIgnoreCase(connect_filter) & intent.getIntExtra("id", -2) == id) {
                String args = intent.getStringExtra("args");
                if (args != null & connectListener != null) {
                    connectListener.connect(args);
                }
            }
        }
    };

    public interface AddUiResponse {
        public void added();
        public void exists();
    }

    public interface ConnectListener {
        public void connect(String args);
    }
}

