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
    void onAuth(int tryCount);
    void onAuthSuccess();
    void onWrongPassword();
    void onSync(int tryCount);
    void onSyncSuccess();
    void onInvalidHash();
    void onMenu(int tryCount);
    void onMenuGot();
    void onSendOrder();
    void onInvalidRequest();
    void onOutOfTry();
}
