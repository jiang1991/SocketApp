package com.viatom.socketapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Parcelable;

import com.blankj.utilcode.util.StringUtils;

public class WifiUtils {

    public int wifiConnectCount = 0;
    public int wifiDisconnectCount = 0;

    /***
     wifi 当前状态
     0-初始状态
     1-连接成功
     2-断开
     *****/
    private int currentStatus = 0;

    public void addOneWifiConnectCount() {
        if (currentStatus == 1) {
            return;
        }
        wifiConnectCount += 1;
    }

    public void addOneWifiDisconnectCount() {
        if (currentStatus == 2) {
            return;
        }
        wifiDisconnectCount += 1;
    }

    public String getString() {
        return "wifi connect->" + wifiConnectCount + "  disconnect->" + wifiDisconnectCount;
    }

    private WifiReceiver wifiReceiver;

    public void register(Context context) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        intentFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        wifiReceiver = new WifiReceiver();
        context.registerReceiver(wifiReceiver, intentFilter);
    }

    public void unRegister(Context context) {
        if (wifiReceiver != null) {
           context.unregisterReceiver(wifiReceiver);
        }
    }

    class WifiReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && !StringUtils.isEmpty(intent.getAction())) {
                String action = intent.getAction();
                if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {
                    Parcelable parcelable = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                    if (parcelable != null && parcelable instanceof NetworkInfo) {
                        NetworkInfo networkInfo = (NetworkInfo) parcelable;
                        NetworkInfo.State state = networkInfo.getState();
                        if (state == NetworkInfo.State.CONNECTED) {

                            addOneWifiConnectCount();
                            currentStatus = 1;
                        } else if (state == NetworkInfo.State.DISCONNECTED) {

                            addOneWifiDisconnectCount();
                            currentStatus = 2;
                        }
                    }
                }
            }
        }
    }

}
