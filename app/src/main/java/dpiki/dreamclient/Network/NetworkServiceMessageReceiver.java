package dpiki.dreamclient.Network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class NetworkServiceMessageReceiver extends BroadcastReceiver {
    private INetworkServiceListener listener;

    public NetworkServiceMessageReceiver(INetworkServiceListener l) {
        listener = l;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        int currState = intent.getIntExtra(NetworkService.INTENT_STATE_CURR, -1);
        int prevState = intent.getIntExtra(NetworkService.INTENT_STATE_PREV, -1);
        int reason = intent.getIntExtra(NetworkService.INTENT_STATE_CHANGE_REASON, -1);

        Log.d("Receiver", "current=" + Integer.toString(currState) +
                          " prev=" + Integer.toString(prevState) +
                          " reason=" + Integer.toString(reason));
    }
}
