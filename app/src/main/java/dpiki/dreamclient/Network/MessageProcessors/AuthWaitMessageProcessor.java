package dpiki.dreamclient.Network.MessageProcessors;

import android.os.Bundle;
import android.util.Log;

import dpiki.dreamclient.Network.NetworkService;
import dpiki.dreamclient.Network.NetworkServiceHandler;
import dpiki.dreamclient.Network.NetworkServiceWriter;

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
        mHandler.changeState(new SyncWaitMessageProcessor(mHandler),
                NetworkService.MESSAGE_AUTH_SUCCESS);

        Bundle bundle = new Bundle();
        bundle.putInt(NetworkServiceWriter.KEY_ACTION_CODE, NetworkService.ACT_CHECK_SYNC);
        bundle.putString(NetworkServiceWriter.KEY_HASH, mHandler.settings.hash);

        // Запускаем поток, который выведет сообщение в сеть
        NetworkServiceWriter writer = new NetworkServiceWriter(mHandler.context, mHandler.socket, bundle);
        writer.start();

    }
}
