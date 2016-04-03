package dpiki.dreamclient.Network.MessageProcessors;

import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.net.Socket;

import dpiki.dreamclient.Network.NetworkService;
import dpiki.dreamclient.Network.NetworkServiceHandler;
import dpiki.dreamclient.Network.NetworkServiceReader;

/**
 * Created by User on 30.03.2016.
 */
public class ConnectingMessageProcessor extends Disconnectable {

    public ConnectingMessageProcessor(NetworkServiceHandler handler) {
        super(handler);
    }

    @Override
    public int state() {
        return NetworkService.STATE_CONNECTING;
    }

    @Override
    public void onConnect() {
        Log.d("CMP", "onConnect");

        try {
            // Открываем сокет
            String ip = mHandler.settings.ip;
            int port = mHandler.settings.port;
            mHandler.socket = new Socket(ip, port);

            // Запускаем поток, который будет слушать сокет
            NetworkServiceReader inputThread = new NetworkServiceReader(mHandler, mHandler.socket);
            inputThread.start();

            // Меняем состояние
            mHandler.changeState(new AuthMessageProcessor(mHandler),
                    NetworkService.MESSAGE_CONNECT);

            // Говорим начать авторизацию
            Message msg = mHandler.obtainMessage();
            msg.what = NetworkService.MESSAGE_AUTH;
            mHandler.sendMessage(msg);
        }
        catch (IOException e) {
            // Говорим переподключиться
            Message msg = mHandler.obtainMessage();
            msg.what = NetworkService.MESSAGE_CONNECT;
            mHandler.sendMessageDelayed(msg, 1000);

            // На всякий случай :)
            mHandler.clearResources();

            Log.d("CMP", "fail to connect");
        }
    }
}
