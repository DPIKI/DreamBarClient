package dpiki.dreamclient.Network.MessageProcessors;

import android.util.Log;

import dpiki.dreamclient.Network.NetworkService;
import dpiki.dreamclient.Network.NetworkServiceHandler;

/**
 * Created by User on 30.03.2016.
 */
public class SyncMessageProcessor extends OutOfTryable {

    public SyncMessageProcessor(NetworkServiceHandler handler) {
        super(handler);
    }

    @Override
    public int state() {
        return NetworkService.STATE_SYNC;
    }

    @Override
    public void onSync(int tryCount) {
        Log.d("SMP", "onSync");
    }
}
