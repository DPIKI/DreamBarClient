package dpiki.dreamclient.Network.MessageProcessors;

import android.util.Log;

import java.io.IOException;
import java.net.InetSocketAddress;
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
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(ip, port), 1000);
            mHandler.socket = socket;

            // Запускаем поток, который будет слушать сокет
            NetworkServiceReader inputThread = new NetworkServiceReader(mHandler, mHandler.socket);
            inputThread.start();

            // Меняем состояние
            mHandler.changeState(new AuthMessageProcessor(mHandler),
                    NetworkService.MESSAGE_CONNECT);

            // Говорим начать авторизацию
            sendMessageToHandler(NetworkService.MESSAGE_AUTH);
        }
        catch (IOException e) {
            // Говорим переподключиться
            sendMessageToHandler(NetworkService.MESSAGE_CONNECT, 1000);

            // На всякий случай :)
            mHandler.clearResources();

            Log.d("CMP", "fail to connect");
        }
    }
}
