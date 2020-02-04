package mist.api;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.util.Log;

import java.util.List;

import mist.api.request.Commission;

class Wifi {

    private final static String ssidPrefix = "mist-";
    static final String TAG="Wifi";

    static void listWifi(Context context) {

        final WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        context.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                context.unregisterReceiver(this);
                List<ScanResult> wifiScanList = wifiManager.getScanResults();
                for (ScanResult scanResult : wifiScanList) {
                    String ssid = scanResult.SSID;
                    if (ssid.contains(ssidPrefix)) {
                        Commission.add(Commission.Add.Hint.wifi, ssid, new Commission.AddCb() {
                            @Override
                            public void cb(boolean value) {
                            }
                        });
                    }
                }
            }
        }, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

        wifiManager.startScan();
    }

    /* These constants must match those in mist_api_commission.h */
    public static int WIFI_JOIN_OK = 0;
    public static int WIFI_JOIN_FAILED = 1;
    public static int WIFI_OFF = 2;
    public static int WIFI_JOIN_UNEXPECTED  = 3;

    private static int targetWifiId = -1;
    private static int previousWifiId = -1;

    static void joinWifi(Context context, String ssid, String password) {
        final WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();

        if (ssid == null) {
            /* Join the the original wifi, if we had one */
            if (targetWifiId != previousWifiId) {
                /* Joining null network causes the target network to be forgotten */
                wifiManager.removeNetwork(targetWifiId);
                wifiManager.enableNetwork(previousWifiId, true);
            }

            MistApi.getInstance().wifiJoinResultCb(WIFI_JOIN_OK);

            bindAppToCurrentNetwork(context, false);

            targetWifiId = -1;
            previousWifiId = -1;
            return;
        }
        else {
            previousWifiId = wifiInfo.getNetworkId();
        }

        if (!wifiManager.isWifiEnabled()) {
            Log.d(TAG, "Wifi is disabled!");
            MistApi.getInstance().wifiJoinResultCb(WIFI_OFF);
            return;
        }

        List<WifiConfiguration> configuredNetworks = wifiManager.getConfiguredNetworks();
        boolean found = false;
        for (WifiConfiguration wifiConfiguration : configuredNetworks) {
            if (wifiConfiguration.SSID.equals("\"" + ssid + "\"")) {
                targetWifiId = wifiConfiguration.networkId;
                found = true;
                Log.d(TAG, "Found the network already in list wifi id " + targetWifiId);
                break;
            }
        }

        if (!found) {
            WifiConfiguration wifiConfiguration = buildWifiConfiguration(ssid, password);
            targetWifiId = wifiManager.addNetwork(wifiConfiguration);
            Log.d(TAG, "Network was not found, added as networkId: " + targetWifiId);
        }

        if (targetWifiId == -1) {
            MistApi.getInstance().wifiJoinResultCb(WIFI_JOIN_FAILED);
            return;
        }

        if (previousWifiId == targetWifiId) {
            MistApi.getInstance().wifiJoinResultCb(WIFI_JOIN_OK);
            return;
        } else {
            wifiManager.disconnect();
            wifiManager.enableNetwork(targetWifiId, true);
            wifiManager.reconnect();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
            intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
            context.registerReceiver(broadcastReceiver, intentFilter);
        }
    }


    static private WifiConfiguration buildWifiConfiguration(String ssid, String password) {
        WifiConfiguration wifiConfig = new WifiConfiguration();
        wifiConfig.SSID = String.format("\"%s\"", ssid);
        if (password != null) {
            wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
            wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
            wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            wifiConfig.preSharedKey = password;
        } else {
            wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
            wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            wifiConfig.allowedAuthAlgorithms.clear();
            wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
            wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
        }
        return wifiConfig;
    }

    static private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (isInitialStickyBroadcast()) {
                Log.d(TAG, "Initial sticky broadcast for " +  intent.getAction());
                return;
            }
            String action = intent.getAction();

            if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();

                int newNetworkId = wifiInfo.getNetworkId();
                Log.d(TAG, "ConnectivityManager.CONNECTIVITY_ACTION detected, newNetworkId = " + newNetworkId);

                if (newNetworkId != -1 && isNetworkAvailable(context)) {
                    if (targetWifiId != -1 && newNetworkId == targetWifiId) {

                        Log.d(TAG, "Successfully joined to expected wifi");
                        bindAppToCurrentNetwork(context, true);
                        MistApi.getInstance().wifiJoinResultCb(WIFI_JOIN_OK);
                        context.unregisterReceiver(broadcastReceiver);

                    } else if (targetWifiId != -1 && newNetworkId != targetWifiId) {

                        Log.d(TAG, "Unexpected wifi join event");
                        MistApi.getInstance().wifiJoinResultCb(WIFI_JOIN_FAILED);
                        context.unregisterReceiver(broadcastReceiver);

                    }  else {
                        Log.d(TAG, "Some other connectivity action, current wifi network id " + newNetworkId);
                    }
                }
            }

            if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
                Log.d(TAG, "NETWORK_STATE_CHANGED_ACTION detected ");
            }

        }
    };

    static private boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    static private void bindAppToCurrentNetwork(Context context, boolean bind) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        Log.d(TAG, "bindAppToCurrentNetwork, " + bind);
        if (!bind) {
            // clear current binding
            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP) {
                ConnectivityManager.setProcessDefaultNetwork(null);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                connectivityManager.bindProcessToNetwork(null);
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Log.d(TAG, "Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP");
            Network[] networks = connectivityManager.getAllNetworks();
            Log.d(TAG, "Network[] networks.length is " + networks.length);
            if (connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().getType() == ConnectivityManager.TYPE_WIFI) {
                Log.d(TAG, "connectivityManager.getActiveNetwork says wifi, isConnected " + connectivityManager.getActiveNetworkInfo().isConnected() + " isConnectedOrConnecting " + connectivityManager.getActiveNetworkInfo().isConnectedOrConnecting());
            }
            for (Network network : networks) {
                NetworkInfo networkInfo = connectivityManager.getNetworkInfo(network);
                //This is still a bit weak, why can't we just find out the network directly?
                if (networkInfo == null) {
                    continue;
                }
                Log.d(TAG, "We see network type " + networkInfo.getType() + " isConnected " + networkInfo.isConnected() + " isConnectedOrConnecting " + networkInfo.isConnectedOrConnecting());
                if (networkInfo.isConnectedOrConnecting() && networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                    if (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP) {
                        Log.d(TAG, "setProcessDefaultNetwork called as we are on Api level Lollipop");
                        ConnectivityManager.setProcessDefaultNetwork(network);
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        Log.d(TAG, "bindProcessToNetwork called as we are on API level M or greater");
                        connectivityManager.bindProcessToNetwork(network);
                    }
                    break;
                }

            }
        }
    }
}
