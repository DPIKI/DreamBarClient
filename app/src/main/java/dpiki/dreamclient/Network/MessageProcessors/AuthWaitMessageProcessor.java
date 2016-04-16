package dpiki.dreamclient.Network.MessageProcessors;

import android.util.Log;

import dpiki.dreamclient.Network.NetworkService;
import dpiki.dreamclient.Network.NetworkServiceHandler;

/**
 * Created by User on 30.03.2016.
 */
public class AuthWaitMessageProcessor extends LostConnectable {

    public AuthWaitMessageProcessor(NetworkServiceHandler handler) {
        super(handler);
    }

    @Override
    public int state() {
        return NetworkService.STATE_AUTH_WAIT;
    }

    @Override
    public void onWrongPassword() {
        Log.d("AWMP", "onWrongPassword");

        // Меняем состояние
        mHandler.changeState(new AuthWrongPasswordMessageProcessor(mHandler),
                NetworkService.MESSAGE_WRONG_PASSWORD);
    }

    @Override
    public void onAuthSuccess() {
        Log.d("AWMP", "onAuthSuccess");

        // Меняем состояние
        mHandler.changeState(new SyncMessageProcessor(mHandler),
                NetworkService.MESSAGE_AUTH_SUCCESS);

        // Говорим начать синхронизацию
        sendMessageToHandler(NetworkService.MESSAGE_SYNC);
    }
}
