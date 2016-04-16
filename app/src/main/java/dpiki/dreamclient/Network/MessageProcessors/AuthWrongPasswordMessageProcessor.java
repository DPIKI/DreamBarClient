package dpiki.dreamclient.Network.MessageProcessors;

import dpiki.dreamclient.Network.NetworkService;
import dpiki.dreamclient.Network.NetworkServiceHandler;

/**
 * Created by User on 16.04.2016.
 */
public class AuthWrongPasswordMessageProcessor extends LostConnectable {

    AuthWrongPasswordMessageProcessor(NetworkServiceHandler handler) {
        super(handler);
    }

    @Override
    public int state() {
        return NetworkService.STATE_AUTH_WRONG_PASSWORD;
    }
}
