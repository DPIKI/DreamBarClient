package dpiki.dreamclient.Network.MessageProcessors;

import android.os.Message;
import android.util.Log;

import dpiki.dreamclient.Network.NetworkService;
import dpiki.dreamclient.Network.NetworkServiceHandler;

/**
 * Created by User on 30.03.2016.
 */
public class AuthWaitMessageProcessor extends Waitable {

    public AuthWaitMessageProcessor(NetworkServiceHandler handler, int tryCount) {
        super(handler, tryCount);
    }

    @Override
    public int state() {
        return NetworkService.STATE_AUTH_WAIT;
    }

    @Override
    public void onWrongPassword() {
        Log.d("AWMP", "onWrongPassword");

        // Меняем состояние
        mHandler.changeState(new DisconnectedMessageProcessor(mHandler),
                NetworkService.MESSAGE_WRONG_PASSWORD);
    }

    @Override
    public void onAuthSuccess() {
        Log.d("AWMP", "onAuthSuccess");

        // Меняем состояние
        mHandler.changeState(new SyncMessageProcessor(mHandler),
                NetworkService.MESSAGE_AUTH_SUCCESS);

        // Говорим начать синхронизацию
        Message msg = mHandler.obtainMessage();
        msg.what = NetworkService.MESSAGE_SYNC;
        msg.arg1 = 0;
        mHandler.sendMessage(msg);
    }

}
