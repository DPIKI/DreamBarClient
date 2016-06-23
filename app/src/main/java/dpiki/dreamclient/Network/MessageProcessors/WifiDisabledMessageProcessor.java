package dpiki.dreamclient.Network.MessageProcessors;

import android.os.Message;
import android.util.Log;

import dpiki.dreamclient.Network.NetworkService;
import dpiki.dreamclient.Network.NetworkServiceHandler;

/**
 * Created by Lenovo on 23.06.2016.
 */
public class WifiDisabledMessageProcessor extends Disconnectable {

    public WifiDisabledMessageProcessor(NetworkServiceHandler handler) {
        super(handler);
    }

    @Override
    public int state() {
        return NetworkService.STATE_WIFI_DISABLED;
    }

    @Override
    public void onWifiEnabled(Message msg) {
        Log.d("WDMP", "onWifiEnabled");

        mHandler.changeState(new ConnectingMessageProcessor(mHandler),
                NetworkService.MESSAGE_WIFI_ENABLED);

        sendMessageToHandler(NetworkService.MESSAGE_CONNECT);
    }
}
