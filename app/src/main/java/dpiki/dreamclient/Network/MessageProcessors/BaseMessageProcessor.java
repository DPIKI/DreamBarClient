package dpiki.dreamclient.Network.MessageProcessors;

import dpiki.dreamclient.Network.NetworkServiceHandler;

/**
 * Created by User on 30.03.2016.
 */
public abstract class BaseMessageProcessor implements IMessageProcessor {

    protected NetworkServiceHandler mHandler;

    public BaseMessageProcessor(NetworkServiceHandler handler) {
        mHandler = handler;
    }

    @Override public void onDisconnect() {}
    @Override public void onLostConnection() {}
    @Override public void onConnect() {}
    @Override public void onAuth(int tryCount) {}
    @Override public void onAuthSuccess() {}
    @Override public void onWrongPassword() {}
    @Override public void onSync(int tryCount) {}
    @Override public void onSyncSuccess() {}
    @Override public void onInvalidHash() {}
    @Override public void onMenu(int tryCount) {}
    @Override public void onMenuGot() {}
    @Override public void onSendOrder() {}
    @Override public void onInvalidRequest() {}
    @Override public void onOutOfTry() {}
}
