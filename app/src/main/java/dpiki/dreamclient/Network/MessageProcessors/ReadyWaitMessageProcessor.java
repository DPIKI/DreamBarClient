package dpiki.dreamclient.Network.MessageProcessors;

import dpiki.dreamclient.Network.NetworkService;
import dpiki.dreamclient.Network.NetworkServiceHandler;

/**
 * Created by User on 30.03.2016.
 */
public class ReadyWaitMessageProcessor extends LostConnectable {

    public ReadyWaitMessageProcessor(NetworkServiceHandler handler) {
        super(handler);
    }

    @Override
    public int state() {
        return NetworkService.STATE_READY_WAIT;
    }

    @Override
    public void onOrderMade() {
        mHandler.changeState(new ReadyMessageProcessor(mHandler),
                NetworkService.MESSAGE_ORDER_MADE);
    }
}
