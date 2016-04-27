package dpiki.dreamclient.Network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class NetworkServiceMessageReceiver extends BroadcastReceiver {
    private INetworkServiceListener listener;

    public NetworkServiceMessageReceiver(INetworkServiceListener l) {
        listener = l;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        int currState = intent.getIntExtra(NetworkService.INTENT_STATE_CURR, -1);
        int reason = intent.getIntExtra(NetworkService.INTENT_STATE_CHANGE_REASON, -1);

        switch (currState) {
            case NetworkService.STATE_CONNECTING:
                listener.onConnecting();
                break;

            case NetworkService.STATE_AUTH_WRONG_PASSWORD:
                listener.onWrongPassword();
                break;

            case NetworkService.STATE_READY:
                if (reason == NetworkService.MESSAGE_SYNC_SUCCESS) {
                    listener.onReady();
                } if (reason == NetworkService.MESSAGE_ORDER_MADE) {
                    listener.onOrderMade();
                }
        }
    }
}
