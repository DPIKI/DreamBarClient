package dpiki.dreamclient.Network;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Created by User on 26.03.2016.
 */
public class NetworkServiceThreadHandler extends Handler {
    // Текущее состояние
    public int state = NetworkService.STATE_DISCONNECTED;

    // Ресурсы, которые надо освобождать
    private Socket socket;
    private NetworkServiceInputThread inputThread;

    // Контекст приложения
    private Context context;

    // Настройки сервера
    private NetworkServiceSettings settings;

    NetworkServiceThreadHandler(Context ctx, NetworkServiceSettings stngs) {
        context = ctx;
        settings = stngs;
    }

    @Override
    public void handleMessage(Message message) {
        switch (message.what) {
            case NetworkService.MESSAGE_STOP_MAIN_SERVICE_THREAD:
                onStopMainServiceThread();
                break;

            case NetworkService.MESSAGE_CONNECT:
                onConnect();
                break;

            case NetworkService.MESSAGE_DISCONNECT:
                onDisconnect();
                break;

            case NetworkService.MESSAGE_CONNECTION_LOST:
                onConnectionLost();
                break;
        }
    }

    // -------------------- Handlers --------------------

    void onConnect() {
        if (settings.isServiceRunning) {
            if (socket == null || !socket.isConnected()) {
                try {
                    // Пытаемся создать соединение
                    InetAddress serverAddress = InetAddress.getByName(settings.ip);
                    socket = new Socket(serverAddress, settings.port);

                    // Запускаем поток, который слушает сокет
                    inputThread = new NetworkServiceInputThread(this, socket);
                    inputThread.start();

                    // Изменяем текущее состояние
                    changeState(NetworkService.STATE_CONNECTED);

                    Log.d("NSTH", "MESSAGE_CONNECT handled");
                } catch (IOException e) {
                    // Чистим ресурсы
                    clearResources();

                    // Говорим себе повторить попытку через 1 секунду
                    connectAgain(1000);

                    // Изменяем текущее состояние
                    changeState(NetworkService.STATE_DISCONNECTED);

                    Log.d("NSTH", "MESSAGE_CONNECT failed");
                }
            } else {
                Log.d("NSTH", "MESSAGE_CONNECT connection already exists");
            }
        } else {
            Log.d("NSTH", "MESSAGE_CONNECT it does not need to connect");
        }
    }

    void onDisconnect() {
        // Изменяем текущее состояние
        changeState(NetworkService.STATE_DISCONNECTED);

        // Чистим ресурсы
        clearResources();

        Log.d("NSTH", "MESSAGE_DISCONNECT handled");
    }

    void onConnectionLost() {
        // Изменяем текущее состояние
        changeState(NetworkService.STATE_DISCONNECTED);

        // Чистим ресурсы
        clearResources();

        // Говорим себе переподключиться немедленно
        connectAgain(0);

        Log.d("NSTH", "MESSAGE_DISCONNECTED handled");
    }

    void onStopMainServiceThread() {
        // Шлем уведомление о разрыве соединения
        changeState(NetworkService.STATE_DISCONNECTED);

        // Чистим ресурсы
        clearResources();

        // Завершаем цикл обработки сообщений
        this.getLooper().quit();
    }

    // ------------------- Support ---------------------

    void clearResources() {
        try {
            // Если сокет еще не закрыт то закрываем его
            if (socket != null) {
                if (!socket.isClosed())
                    socket.close();
                socket = null;
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    void connectAgain(long delay) {
        // Формируем сообщение
        Message msg = this.obtainMessage();
        msg.what = NetworkService.MESSAGE_CONNECT;

        // Отправляем самому себе
        if (delay != 0)
            this.sendMessageDelayed(msg, delay);
        else
            this.sendMessage(msg);
    }

    void changeState(int aState) {
        state = aState;
        Intent intent = new Intent(NetworkService.ACTION_NETWORK_SERVICE);
        intent.putExtra("state", state);
        context.sendBroadcast(intent);
    }
}
