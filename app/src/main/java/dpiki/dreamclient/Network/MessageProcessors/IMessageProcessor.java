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
    void onAuth();
    void onAuthSuccess();
    void onWrongPassword();
    void onSync();
    void onSyncSuccess();
    void onInvalidHash();
    void onMenu();
    void onMenuGot();
    void onSendOrder();
    void onInvalidRequest();
}
