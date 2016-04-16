package dpiki.dreamclient.Network.MessageProcessors;

import dpiki.dreamclient.Network.NetworkService;
import dpiki.dreamclient.Network.NetworkServiceHandler;

/**
 * Created by User on 30.03.2016.
 */
public class MenuMessageProcessor extends LostConnectable {

    public MenuMessageProcessor(NetworkServiceHandler handler) {
        super(handler);
    }

    @Override
    public int state() {
        return NetworkService.STATE_MENU;
    }
}
