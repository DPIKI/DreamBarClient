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
public class ReadyMessageProcessor extends ImageLoadable {

    public ReadyMessageProcessor(NetworkServiceHandler handler) {
        super(handler);
    }

    @Override
    public int state() {
        return NetworkService.STATE_READY;
    }

    @Override
    public void onSendOrder(Message msg) {
        // Меняем состояние
        mHandler.changeState(new ReadyWaitMessageProcessor(mHandler),
                NetworkService.MESSAGE_SEND_ORDER);

        // Формируем данные для отправки
        Bundle bundle = new Bundle();
        bundle.putInt(NetworkServiceWriter.KEY_ACTION_CODE, NetworkService.ACT_MAKE_ORDER);

        // Выводим их
        NetworkServiceWriter writer = new NetworkServiceWriter(mHandler.context, mHandler.socket, bundle);
        writer.start();

        Log.d("RMP", "Order sent");
    }
}
