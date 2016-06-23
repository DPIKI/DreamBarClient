package dpiki.dreamclient.Network.MessageProcessors;

import android.os.Bundle;
import android.os.Message;
import android.util.Log;

import dpiki.dreamclient.Network.NetworkService;
import dpiki.dreamclient.Network.NetworkServiceHandler;
import dpiki.dreamclient.Network.NetworkServiceWriter;

/**
 * Created by User on 30.03.2016.
 */
public abstract class LostConnectable extends WifiDisableble {

    public LostConnectable(NetworkServiceHandler handler) {
        super(handler);
    }

    @Override
    public void onLostConnection(Message msg) {
        Log.d("LostConnectable", "onLostConnection");

        // Чистим ресурсы
        mHandler.clearResources();

        // Говорим себе переподключиться
        sendMessageToHandler(NetworkService.MESSAGE_CONNECT, 1000);

        // Переключаем состояние
        mHandler.changeState(new ConnectingMessageProcessor(mHandler),
                NetworkService.MESSAGE_LOST_CONNECTION);
    }

    @Override
    public void onTick(Message msg) {
        Log.d("NetworkService", "onTick" + Integer.toString(mHandler.mTimerTicks));
        if (mHandler.mTimerTicks < 5) {
            Bundle bundle = new Bundle();
            bundle.putInt(NetworkServiceWriter.KEY_ACTION_CODE, NetworkService.ACT_CHECK_CONNECTION);

            NetworkServiceWriter writer =
                    new NetworkServiceWriter(mHandler.context, mHandler.socket, bundle);
            writer.start();

            mHandler.mTimerTicks++;

            sendMessageToHandler(NetworkService.MESSAGE_TICK, 1000);
        } else {
            sendMessageToHandler(NetworkService.MESSAGE_LOST_CONNECTION);
        }
    }

    @Override
    public void onIAmHere(Message msg) {
        Log.d("NetworkService", "I am here" + Integer.toString(mHandler.mTimerTicks));
        mHandler.mTimerTicks = 0;
    }
}
