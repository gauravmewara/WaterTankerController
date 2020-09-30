package com.example.watertankercontroller.Utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkUtils {
    public static int NOT_CONNECTED = 0;
    public static int WIFI_INTERNET = 1;
    public static int NETWORK_INTERNET = 2;
    public static int WIFI_NO_INTERNET = 3;
    public static int NETWORK_NO_INTERNET = 4;

    public static int getConnectivityStatus(Context context){
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if(activeNetwork!=null) {
            if (cm.isActiveNetworkMetered()) {
                if (activeNetwork.isConnected()) {
                    return NETWORK_INTERNET;
                }else{
                    return NETWORK_NO_INTERNET;
                }
            } else {
                if (activeNetwork.isConnected()) {
                    return WIFI_INTERNET;
                }else{
                    return WIFI_NO_INTERNET;
                }
            }
        }
        return NOT_CONNECTED;
    }
}
