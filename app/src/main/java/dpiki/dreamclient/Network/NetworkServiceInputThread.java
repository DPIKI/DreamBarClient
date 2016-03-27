package dpiki.dreamclient.Network;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Created by User on 26.03.2016.
 */
public class NetworkServiceInputThread extends Thread {
    private Handler handler;
    private Socket socket;
    private DataInputStream is;

    NetworkServiceInputThread(Handler h, Socket s) {
        handler = h;
        socket = s;
    }

    @Override
    public void run() {
        try {
            // Открываем входной поток
            is = new DataInputStream(socket.getInputStream());

            // Запускаем цикл обработки сообщений от сервера
            while (true) {
                // Читаем размер сообщения
                int size = is.readInt();

                // Выделяем бужер этого размера и читаем в него данные
                byte[] data = new byte[size];
                is.read(data);

                // TODO: Обрабатываем полученные данные
            }
        }
        catch (IOException e) {
            // Если что-то пошло не так говорим что мы отключились
            Message msg1 = handler.obtainMessage();
            msg1.what = NetworkService.MESSAGE_CONNECTION_LOST;
            handler.sendMessage(msg1);

            // Печатаем лог
            e.printStackTrace();
        }
        finally {
            is = null;
        }
    }
}
