package dpiki.dreamclient.Network.MessageProcessors;

import dpiki.dreamclient.Network.NetworkService;
import dpiki.dreamclient.Network.NetworkServiceHandler;

/**
 * Created by User on 30.03.2016.
 */
public class SyncWaitMessageProcessor extends Waitable {

    public SyncWaitMessageProcessor(NetworkServiceHandler handler, int tryCount) {
        super(handler, tryCount);
    }

    @Override
    public int state() {
        return NetworkService.STATE_SYNC_WAIT;
    }
}
