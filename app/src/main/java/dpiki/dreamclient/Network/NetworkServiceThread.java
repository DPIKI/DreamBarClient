package dpiki.dreamclient.Network;

import android.content.Context;
import android.os.Looper;

/**
 * Created by User on 26.03.2016.
 */
public class NetworkServiceThread extends Thread {
    public NetworkServiceHandler handler;
    public Boolean ready = false;
    Context context;
    NetworkServiceSettings settings;

    NetworkServiceThread(Context ctx, NetworkServiceSettings stngs) {
        context = ctx;
        settings = stngs;
    }

    @Override
    public void run() {
    /*    Looper.prepare();
        handler = new NetworkServiceHandler(context, settings);
        ready = true;
        Looper.loop();*/
    }
}
