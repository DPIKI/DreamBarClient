package dpiki.dreamclient.Network.MessageProcessors;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutCompat;
import android.util.Log;

import dpiki.dreamclient.Network.NetworkService;
import dpiki.dreamclient.Network.NetworkServiceHandler;
import dpiki.dreamclient.Network.NetworkServiceWriter;

/**
 * Created by User on 30.03.2016.
 */
public class MenuWaitMessageProcessor extends LostConnectable {

    public MenuWaitMessageProcessor(NetworkServiceHandler handler) {
        super(handler);
    }

    @Override
    public int state() {
        return NetworkService.STATE_MENU_WAIT;
    }

    @Override
    public void onMenuGot() {
        Bundle bundle = new Bundle();
        bundle.putInt(NetworkServiceWriter.KEY_ACTION_CODE, NetworkService.ACT_CHECK_SYNC);
        bundle.putString(NetworkServiceWriter.KEY_HASH, mHandler.settings.hash);

        // Меняем состояние
        mHandler.changeState(new SyncWaitMessageProcessor(mHandler),
                NetworkService.MESSAGE_MENU_GOT);

        // Запускаем поток, который выведет сообщение в сеть
        NetworkServiceWriter writer = new NetworkServiceWriter(mHandler.context, mHandler.socket, bundle);
        writer.start();

        Log.d("MWMP", "Menu got");
    }
}
