package dpiki.dreamclient.Network;

/**
 * Created by User on 25.03.2016.
 */
public interface INetworkServiceListener {
    void onDisconnected();
    void onConnecting();
    void onWrongPassword();
    void onReady();
    void onOrderMade();
}
