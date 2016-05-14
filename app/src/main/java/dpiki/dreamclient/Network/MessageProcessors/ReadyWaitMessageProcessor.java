package dpiki.dreamclient.Network.MessageProcessors;

import android.os.Message;

import dpiki.dreamclient.Network.NetworkService;
import dpiki.dreamclient.Network.NetworkServiceHandler;

/**
 * Created by User on 30.03.2016.
 */
public class ReadyWaitMessageProcessor extends ImageLoadable {

    public ReadyWaitMessageProcessor(NetworkServiceHandler handler) {
        super(handler);
    }

    @Override
    public int state() {
        return NetworkService.STATE_READY_WAIT;
    }

    @Override
    public void onOrderMade(Message msg) {
        mHandler.changeState(new ReadyMessageProcessor(mHandler),
                NetworkService.MESSAGE_ORDER_MADE);
    }
}
