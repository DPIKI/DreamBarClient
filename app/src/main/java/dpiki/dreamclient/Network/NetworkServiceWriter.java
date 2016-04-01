package dpiki.dreamclient.Network;

import android.os.Bundle;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.logging.Handler;

/**
 * Created by User on 30.03.2016.
 */
public class NetworkServiceWriter extends Thread {
    Handler handler;
    Socket socket;
    Bundle requestData;

    NetworkServiceWriter(Handler h, Socket s, Bundle b) {
        handler = h;
        socket = s;
        requestData = b;
    }

    @Override
    public void run() {
        // Создаем поток для вывода
        DataOutputStream os;

        try {
            os = new DataOutputStream(socket.getOutputStream());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
