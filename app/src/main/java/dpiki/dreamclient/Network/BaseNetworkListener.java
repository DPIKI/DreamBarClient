package dpiki.dreamclient.Network;

/**
 * Created by User on 25.04.2016.
 */
public class BaseNetworkListener implements INetworkServiceListener {
    @Override public void onConnecting() {}
    @Override public void onWrongPassword() {}
    @Override public void onReady() {}
    @Override public void onOrderMade() {}
    @Override public void onDisconnected() {}
    @Override public void onImageLoaded(int id, byte[] image) {}
}
