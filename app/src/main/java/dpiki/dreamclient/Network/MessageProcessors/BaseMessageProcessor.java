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

    @Override public void onDisconnect(Message msg) {}
    @Override public void onLostConnection(Message msg) {}
    @Override public void onConnect(Message msg) {}
    @Override public void onAuthSuccess(Message msg) {}
    @Override public void onWrongPassword(Message msg) {}
    @Override public void onSyncSuccess(Message msg) {}
    @Override public void onInvalidHash(Message msg) {}
    @Override public void onMenuGot(Message msg) {}
    @Override public void onSendOrder(Message msg) {}
    @Override public void onOrderMade(Message msg) {}
    @Override public void onTick(Message msg) {}
    @Override public void onIAmHere(Message msg) {}
    @Override public void onImageLoaded(Message msg) {}
    @Override public void onSendLoadImageRequest(Message msg) {}

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
