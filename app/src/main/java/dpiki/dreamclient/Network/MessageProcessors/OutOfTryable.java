package dpiki.dreamclient.Network.MessageProcessors;

import android.util.Log;

import dpiki.dreamclient.Network.NetworkService;
import dpiki.dreamclient.Network.NetworkServiceHandler;

/**
 * Created by User on 04.04.2016.
 */
public abstract class OutOfTryable extends LostConnectable {

    public OutOfTryable(NetworkServiceHandler handler) {
        super(handler);
    }

    @Override
    public void onOutOfTry() {
        Log.d("OutOfTryable", "onOutOfTry");

        // Чистим ресурсы
        mHandler.clearResources();

        // Меняем состояние
        mHandler.changeState(new DisconnectedMessageProcessor(mHandler),
                NetworkService.MESSAGE_OUT_OF_TRY);
    }
}
