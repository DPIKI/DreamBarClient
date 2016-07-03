package dpiki.dreamclient.Network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Message;
import android.util.Log;

public class WifiStateChangeReceiver extends BroadcastReceiver {

    private  NetworkServiceHandler mHandler;

    public WifiStateChangeReceiver(NetworkServiceHandler handler) {
        mHandler = handler;
    }

    public WifiStateChangeReceiver() {
        mHandler = null;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("WSCR", "onReceive");

        ConnectivityManager cm =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo ni = cm.getActiveNetworkInfo();
        boolean isConnected = ni != null
                && ni.getType() == ConnectivityManager.TYPE_WIFI
                && ni.isConnected();
        if (isConnected) {
            Message msg = mHandler.obtainMessage();
            msg.what = NetworkService.MESSAGE_WIFI_ENABLED;
            mHandler.sendMessage(msg);
        }
        else {
            Message msg = mHandler.obtainMessage();
            msg.what = NetworkService.MESSAGE_WIFI_DISABLED;
            mHandler.sendMessage(msg);
        }
    }
}
