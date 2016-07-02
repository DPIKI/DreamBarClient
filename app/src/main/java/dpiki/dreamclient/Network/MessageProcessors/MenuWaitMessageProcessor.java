package dpiki.dreamclient.Network.MessageProcessors;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;

import dpiki.dreamclient.Network.NetworkService;
import dpiki.dreamclient.Network.NetworkServiceHandler;
import dpiki.dreamclient.Network.NetworkServiceWriter;
import dpiki.dreamclient.R;

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
    public void onMenuGot(Message msg) {

        SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(mHandler.context);
        String hash =
                preferences.getString(mHandler.context.getString(R.string.s_pref_key_hash), "");
        Bundle bundle = new Bundle();
        bundle.putInt(NetworkServiceWriter.KEY_ACTION_CODE, NetworkService.ACT_CHECK_SYNC);
        bundle.putString(NetworkServiceWriter.KEY_HASH, hash);

        // Меняем состояние
        mHandler.changeState(new SyncWaitMessageProcessor(mHandler),
                NetworkService.MESSAGE_MENU_GOT);

        // Запускаем поток, который выведет сообщение в сеть
        NetworkServiceWriter writer = new NetworkServiceWriter(mHandler.context, mHandler.socket, bundle);
        mHandler.writerHandler.post(writer);

        Log.d("MWMP", "Menu got");
    }
}
