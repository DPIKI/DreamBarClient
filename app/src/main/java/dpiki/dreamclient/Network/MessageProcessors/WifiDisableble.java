package dpiki.dreamclient.Network.MessageProcessors;

import android.os.Message;
import android.util.Log;

import dpiki.dreamclient.Network.NetworkService;
import dpiki.dreamclient.Network.NetworkServiceHandler;

/**
 * Created by Lenovo on 23.06.2016.
 */
public abstract class WifiDisableble extends Disconnectable {

    public WifiDisableble(NetworkServiceHandler handler) {
        super(handler);
    }

    @Override
    public void onWifiDisabled(Message msg) {
        Log.d("WD", "onWifiDisabled");

        mHandler.clearResources();

        mHandler.changeState(new WifiDisabledMessageProcessor(mHandler),
                NetworkService.MESSAGE_WIFI_DISABLED);
    }
}
