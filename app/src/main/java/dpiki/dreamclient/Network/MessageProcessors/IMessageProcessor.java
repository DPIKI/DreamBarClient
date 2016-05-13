package dpiki.dreamclient.Network.MessageProcessors;

import android.os.Message;

/**
 * Created by User on 30.03.2016.
 */
public interface IMessageProcessor {

    int state();

    // Handlers
    void onDisconnect(Message msg);
    void onLostConnection(Message msg);
    void onConnect(Message msg);
    void onAuthSuccess(Message msg);
    void onWrongPassword(Message msg);
    void onSyncSuccess(Message msg);
    void onInvalidHash(Message msg);
    void onMenuGot(Message msg);
    void onSendOrder(Message msg);
    void onOrderMade(Message msg);
    void onTick(Message msg);
    void onIAmHere(Message msg);
    void onImageLoaded(Message msg);
    void onSendLoadImageRequest(Message msg);
}
