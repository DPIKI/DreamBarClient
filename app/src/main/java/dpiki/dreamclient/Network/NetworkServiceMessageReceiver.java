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
        int state = intent.getIntExtra("state", -1);

        // Поле state не указано (ошибка программы)
        if (state == -1) {
            Log.e("Receiver", "state id undefined");
            return;
        }

        switch (state) {
            case NetworkService.STATE_DISCONNECTED:
                Log.d("Receiver", "STATE_DISCONNECTED");
                break;

            case NetworkService.STATE_CONNECTED:
                Log.d("Receiver", "STATE_CONNECTED");
                break;

            case NetworkService.STATE_AUTHORIZED:
                Log.d("Receiver", "STATE_AUTHORIZED");
                break;

            case NetworkService.STATE_SYNCHRONIZED:
                Log.d("Receiver", "STATE_SYNCHRONIZED");
                break;
        }
    }
}
