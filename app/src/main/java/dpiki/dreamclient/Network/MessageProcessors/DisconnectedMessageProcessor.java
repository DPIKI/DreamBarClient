package dpiki.dreamclient.Network.MessageProcessors;

import android.os.Message;
import android.util.Log;

import dpiki.dreamclient.Network.NetworkService;
import dpiki.dreamclient.Network.NetworkServiceHandler;

/**
 * Created by User on 30.03.2016.
 */
public class DisconnectedMessageProcessor extends BaseMessageProcessor {

    public DisconnectedMessageProcessor(NetworkServiceHandler handler) {
        super(handler);
    }

    @Override
    public int state() {
        return NetworkService.STATE_DISCONNECTED;
    }

    @Override
    public void onConnect() {
        Log.d("DMP", "onConnect");

        if (mHandler.settings.isServiceRunning) {

            // Говорим себе начать коннектиться
            Message msg = mHandler.obtainMessage();
            msg.what = NetworkService.MESSAGE_CONNECT;
            mHandler.sendMessage(msg);

            // Меняем состояние
            mHandler.changeState(new ConnectingMessageProcessor(mHandler),
                    NetworkService.MESSAGE_CONNECT);
        } else {
            Log.d("DMP", "Service is stopped");
        }
    }
}
