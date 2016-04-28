package dpiki.dreamclient.Network.MessageProcessors;

/**
 * Created by User on 30.03.2016.
 */
public interface IMessageProcessor {

    int state();

    // Handlers
    void onDisconnect();
    void onLostConnection();
    void onConnect();
    void onAuthSuccess();
    void onWrongPassword();
    void onSyncSuccess();
    void onInvalidHash();
    void onMenuGot();
    void onSendOrder();
    void onOrderMade();
    void onTick();
    void onIAmHere();
}
