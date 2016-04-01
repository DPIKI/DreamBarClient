package dpiki.dreamclient.Network.MessageProcessors;

import android.util.Log;

import dpiki.dreamclient.Network.NetworkService;
import dpiki.dreamclient.Network.NetworkServiceHandler;

/**
 * Created by User on 30.03.2016.
 */
public abstract class Disconnectable extends BaseMessageProcessor {

    public Disconnectable(NetworkServiceHandler handler) {
        super(handler);
    }

    @Override
    public void onDisconnect() {
        Log.d("Disconnectable", "onDisconnect");

        // Очищаем ресурсы
        mHandler.clearResources();

        // Переключаемся в новое сстояние
        mHandler.changeState(
                new DisconnectedMessageProcessor(mHandler), NetworkService.MESSAGE_DISCONNECT);
    }
}
