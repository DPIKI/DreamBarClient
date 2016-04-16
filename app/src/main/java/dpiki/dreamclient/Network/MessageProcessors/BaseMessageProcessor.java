package dpiki.dreamclient.Network.MessageProcessors;

import android.os.Message;

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
    @Override public void onAuth() {}
    @Override public void onAuthSuccess() {}
    @Override public void onWrongPassword() {}
    @Override public void onSync() {}
    @Override public void onSyncSuccess() {}
    @Override public void onInvalidHash() {}
    @Override public void onMenu() {}
    @Override public void onMenuGot() {}
    @Override public void onSendOrder() {}

    protected void sendMessageToHandler(int message) {
        Message msg = mHandler.obtainMessage();
        msg.what = message;
        mHandler.sendMessage(msg);
    }

    protected void sendMessageToHandler(int message, int delay) {
        Message msg = mHandler.obtainMessage();
        msg.what = message;
        mHandler.sendMessageDelayed(msg, delay);
    }
}
