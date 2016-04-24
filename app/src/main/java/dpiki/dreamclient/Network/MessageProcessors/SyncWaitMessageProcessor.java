package dpiki.dreamclient.Network.MessageProcessors;

import android.os.Bundle;
import android.util.Log;

import dpiki.dreamclient.Network.NetworkService;
import dpiki.dreamclient.Network.NetworkServiceHandler;
import dpiki.dreamclient.Network.NetworkServiceWriter;

/**
 * Created by User on 30.03.2016.
 */
public class SyncWaitMessageProcessor extends LostConnectable {

    public SyncWaitMessageProcessor(NetworkServiceHandler handler) {
        super(handler);
    }

    @Override
    public int state() {
        return NetworkService.STATE_SYNC_WAIT;
    }

    @Override
    public void onSyncSuccess() {
        // Меняем состояние
        mHandler.changeState(new ReadyMessageProcessor(mHandler),
                NetworkService.MESSAGE_SYNC_SUCCESS);

        Log.d("SWMP", "Sync succeed");
    }

    @Override
    public void onInvalidHash() {
        // Формируем данные для отправки
        Bundle bundle = new Bundle();
        bundle.putInt(NetworkServiceWriter.KEY_ACTION_CODE, NetworkService.ACT_MENU);

        // Выводим их
        NetworkServiceWriter writer = new NetworkServiceWriter(mHandler.socket, bundle);
        writer.start();

        // Меняем состояние
        mHandler.changeState(new MenuWaitMessageProcessor(mHandler),
                NetworkService.MESSAGE_INVALID_HASH);

        Log.d("SWMP", "Sync invalid hash");
    }
}
