package dpiki.dreamclient.Network.MessageProcessors;

import android.util.Log;

import dpiki.dreamclient.Network.NetworkService;
import dpiki.dreamclient.Network.NetworkServiceHandler;

/**
 * Created by User on 30.03.2016.
 */
public class AuthMessageProcessor extends LostConnectable {

    public AuthMessageProcessor(NetworkServiceHandler handler) {
        super(handler);
    }

    @Override
    public int state() {
        return NetworkService.STATE_AUTH;
    }

    @Override
    public void onAuth() {
        Log.d("AMP", "onAuth");
    }
}
