package dpiki.dreamclient.Network.MessageProcessors;

import android.util.Log;

import dpiki.dreamclient.Network.NetworkService;
import dpiki.dreamclient.Network.NetworkServiceHandler;

/**
 * Created by User on 30.03.2016.
 */
public abstract class LostConnectable extends Disconnectable {

    public LostConnectable(NetworkServiceHandler handler) {
        super(handler);
    }

    @Override
    public void onLostConnection() {
        Log.d("LostConnectable", "onLostConnection");

        // Чистим ресурсы
        mHandler.clearResources();

        // Говорим себе переподключиться
        sendMessageToHandler(NetworkService.MESSAGE_CONNECT);

        // Переключаем состояние
        mHandler.changeState(new ConnectingMessageProcessor(mHandler),
                NetworkService.MESSAGE_LOST_CONNECTION);
    }
}
