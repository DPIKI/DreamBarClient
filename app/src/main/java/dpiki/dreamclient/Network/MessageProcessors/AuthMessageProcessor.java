package dpiki.dreamclient.Network.MessageProcessors;

import android.os.Bundle;
import android.util.Log;

import dpiki.dreamclient.Network.NetworkService;
import dpiki.dreamclient.Network.NetworkServiceHandler;
import dpiki.dreamclient.Network.NetworkServiceWriter;

/**
 * Created by User on 30.03.2016.
 */
public class AuthMessageProcessor extends LostConnectable {

    public AuthMessageProcessor(NetworkServiceHandler handler) {
        super(handler);
    }

    @Override
    public int state() {
        return NetworkService.STATE_AUTH;
    }

    @Override
    public void onAuth() {
        Log.d("AMP", "onAuth");

        // Формируем данные для потока который будет писать в сокет
        Bundle bundle = new Bundle();
        bundle.putInt(NetworkServiceWriter.KEY_ACTION_CODE, NetworkService.ACT_AUTHORIZE);
        bundle.putString(NetworkServiceWriter.KEY_NAME, mHandler.settings.name);
        bundle.putString(NetworkServiceWriter.KEY_PASSWORD, mHandler.settings.password);

        // Запускаем поток
        NetworkServiceWriter writer = new NetworkServiceWriter(mHandler.socket, bundle);
        writer.start();

        // Меняем состояние
        mHandler.changeState(new AuthWaitMessageProcessor(mHandler),
                NetworkService.MESSAGE_AUTH);
    }
}
