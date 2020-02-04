package fi.ct.mist.ui;


import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.bson.BsonDocument;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import mist.api.request.Mist;
import mist.api.request.Sandbox;
import mist.node.request.Control;
import wish.Identity;
import wish.LocalDiscovery;
import wish.Peer;
import wish.request.Wld;


public class Main extends ListActivity {

    private Identity userIdentity;

    private ArrayList<String> listItems = new ArrayList<String>();
    private ArrayAdapter<String> adapter;
    private WifiManager wifiManager;
    private WifiScanReceiver wifiReciever;

    private SparseArray<ScanResult> wifis;
    private List<String> ssidList;

    private SparseArray<LocalDiscovery> localList;

    private TextView user;

    private int signalsId = 0;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_list);

        user = (TextView) findViewById(R.id.main_user);
        adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,
                listItems);
        setListAdapter(adapter);
        wifis = new SparseArray<>();
        ssidList = new ArrayList<>();
        localList = new SparseArray<>();

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiReciever = new WifiScanReceiver();

        wish.request.Identity.list(new wish.request.Identity.ListCb() {
            @Override
            public void cb(List<Identity> list) {
                for (Identity identity : list) {
                    if (identity.isPrivkey()) {
                        userIdentity = identity;
                        user.setText(userIdentity.getAlias());
                        break;
                    }
                }
            }
        });

        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // selected item
                String ssid = ((TextView) view).getText().toString();

                ScanResult wifi = wifis.get(position);
                LocalDiscovery localDiscovery = localList.get(position);

                if (wifis.indexOfKey(position) >= 0) {
                    onWifi(wifis.get(position));
                }
                if (localList.indexOfKey(position) >= 0) {
                    onWld(localList.get(position));
                }
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        listItems.clear();
        wifis.clear();
        ssidList.clear();
        localList.clear();

        wifiManager.startScan();

        if (userIdentity != null) {
            user.setText(userIdentity.getAlias());
        }

        listLocal();
    }

    private int addItems(String type, String name) {
        listItems.add(type + " : " + name);
        adapter.notifyDataSetChanged();
        return listItems.size() - 1;
    }


    protected void onPause() {
        super.onPause();
        unregisterReceiver(wifiReciever);
    }

    protected void onResume() {
        super.onResume();
        registerReceiver(wifiReciever, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
    }

    private void onWifi(ScanResult result) {
        Toast.makeText(getApplicationContext(), "wifi: " + result.SSID, Toast.LENGTH_SHORT).show();
    }

    private void onWld(final LocalDiscovery localDiscovery) {
        Toast.makeText(getApplicationContext(), "local: " + localDiscovery.getAlias(), Toast.LENGTH_SHORT).show();


        Wld.friendRequest(userIdentity.getUid(), localDiscovery, new Wld.FriendRequestCb() {
            @Override
            public void cb(boolean value) {

                if (signalsId != 0) {
                    Mist.cancel(signalsId);
                    signalsId = 0;
                }
                signalsId = Mist.signals(new Mist.SignalsCb() {
                    @Override
                    public void cb(final String signal, BsonDocument document) {
                        if (signal.equals("peers")) {
                            Mist.listServices(new Mist.ListServicesCb() {
                                @Override
                                public void cb(List<Peer> peers) {
                                    for (Peer peer : peers) {
                                        if (Arrays.equals(peer.getRuid(), localDiscovery.getRuid()) &&
                                                Arrays.equals(peer.getRhid(), localDiscovery.getRhid())) {
                                            Control.read(peer, "mist.name", new Control.ReadCb() {
                                                @Override
                                                public void cbString(String data) {
                                                    super.cbString(data);
                                                    if (data.equals("ESC")) {
                                                        Mist.cancel(signalsId);
                                                        signalsId = 0;
                                                        //todo add peer to sandbox
                                                        Toast.makeText(getApplicationContext(), "found Peer name: " + data, Toast.LENGTH_SHORT).show();
                                                    }
                                                }

                                                @Override
                                                public void err(int code, String msg) {
                                                    super.err(code, msg);
                                                    Log.d("test",  "err control read msg: " + msg);
                                                }
                                            });
                                        }
                                    }
                                }

                                @Override
                                public void err(int code, String msg) {

                                }

                                @Override
                                public void end() {

                                }
                            });
                        }
                    }

                    @Override
                    public void err(int code, String msg) {

                    }

                    @Override
                    public void end() {

                    }
                });
            }
        });
    }

    private void listLocal() {
        Wld.list(new Wld.ListCb() {
            @Override
            public void cb(List<LocalDiscovery> localDiscoveries) {
                for (LocalDiscovery localDiscovery : localDiscoveries) {
                    if (!Arrays.equals(localDiscovery.getRuid(), userIdentity.getUid())) {
                        int id = addItems("local", localDiscovery.getAlias());
                        localList.append(id, localDiscovery);
                    }
                }
            }
        });
    }

    class WifiScanReceiver extends BroadcastReceiver {
        public void onReceive(Context c, Intent intent) {
            List<ScanResult> wifiScanList = wifiManager.getScanResults();
            for (ScanResult scanResult : wifiScanList) {
                String ssid = scanResult.SSID;
                if (ssid.contains("Sjo") && !ssidList.contains(ssid)) {
                    ssidList.add(ssid);
                    int id = addItems("wifi", ssid);
                    wifis.append(id, scanResult);
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (signalsId != 0) {
            Mist.cancel(signalsId);
        }
    }
}
