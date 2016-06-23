package dpiki.dreamclient.Network.MessageProcessors;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Message;
import android.util.Log;

import dpiki.dreamclient.Network.NetworkService;
import dpiki.dreamclient.Network.NetworkServiceHandler;

/**
 * Created by Lenovo on 23.06.2016.
 */
public class CheckWifiMessageProcessor extends Disconnectable {

    public CheckWifiMessageProcessor(NetworkServiceHandler handler) {
        super(handler);
    }

    @Override
    public int state() {
        return NetworkService.STATE_WIFI_CHECK;
    }

    @Override
    public void onCheckWifi(Message msg) {
        Log.d("CWMP", "onCheckWifi");
        ConnectivityManager cm =
                (ConnectivityManager)mHandler.context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo ni = cm.getActiveNetworkInfo();
        boolean isConnected = ni != null
                && ni.getType() == ConnectivityManager.TYPE_WIFI
                && ni.isConnected();
        if (isConnected) {
            mHandler.changeState(new ConnectingMessageProcessor(mHandler),
                    NetworkService.MESSAGE_WIFI_ENABLED);

            sendMessageToHandler(NetworkService.MESSAGE_CONNECT);
        }
        else {
            mHandler.changeState(new WifiDisabledMessageProcessor(mHandler),
                    NetworkService.MESSAGE_WIFI_DISABLED);
        }
    }
}
