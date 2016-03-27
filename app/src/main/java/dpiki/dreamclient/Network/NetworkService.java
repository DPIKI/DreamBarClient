package dpiki.dreamclient.Network;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

public class NetworkService extends Service {
    public static final String ACTION_NETWORK_SERVICE = "dpiki.dreamclient.action.networkService";

    // Сообщения для рабочего потока
    public static final int MESSAGE_STOP_MAIN_SERVICE_THREAD = 0;
    public static final int MESSAGE_CONNECT = 1;
    public static final int MESSAGE_DISCONNECT = 2;
    public static final int MESSAGE_CONNECTION_LOST = 3;

    // Состояния
    public static final int STATE_DISCONNECTED = 0;
    public static final int STATE_CONNECTED = 1;
    public static final int STATE_AUTHORIZED = 2;
    public static final int STATE_SYNCHRONIZED = 3;

    private NetworkServiceThreadHandler handler;
    private NetworkServiceBinder binder;
    private NetworkServiceSettings settings;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        // Создаем биндер и объект настроек
        binder = new NetworkServiceBinder();
        settings = new NetworkServiceSettings();

        // Читаем сохраненные настройки
        SharedPreferences pref = getSharedPreferences("NetworkSettings", Context.MODE_PRIVATE);
        settings.ip = pref.getString("ip", "192.168.0.29");
        settings.port = pref.getInt("port", 13563);
        settings.isServiceRunning = pref.getBoolean("is_running", false);
        settings.password = pref.getString("password", "password");

        // Создаем рабочий поток сервиса, который будет обрабатывать
        // сообщения от UI потока и дочерних потоков
        NetworkServiceThread thread = new NetworkServiceThread(getApplicationContext(), settings);
        thread.start();
        while (!thread.ready);
        handler = thread.handler;

        // Говорим этому потоку создать соединение, если пользователь разрешил
        if (settings.isServiceRunning)
            sendConnectMessage();

        Log.d("NetworkService", "Created");
    }

    @Override
    public void onDestroy() {
        // Говорим рабочему потоку завершиться
        sendDisconnectMessage();

        // Сохраняем настройки
        SharedPreferences pref = getSharedPreferences("NetworkSettings", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("ip", settings.ip);
        editor.putInt("port", settings.port);
        editor.putBoolean("is_running", settings.isServiceRunning);

        Log.d("NetworkService", "Destroyed");
    }

    // ---------------------------- Interface to application ----------------------------

    public void connect() {
        // Запоминаем, что при следующем запуске надо сразу коннектиться
        settings.isServiceRunning = true;

        // Коннектимся
        sendConnectMessage();
    }

    public void disconnect() {
        // Запоминаем, что при коннектиться при запуске не надо
        settings.isServiceRunning = false;

        // Отключаемся
        sendDisconnectMessage();
    }

    public Boolean setIp(String ip) {
        // Проверяем переданную строку
        if (!parseIp(ip))
            return false;

        // Отключаемся
        sendDisconnectMessage();

        // Ждем пока отключимся
        while (handler.state != STATE_DISCONNECTED);

        // Меняем ip
        settings.ip = ip;

        // Коннектимся
        sendConnectMessage();

        return true;
    }

    public Boolean setPort(int port) {
        // Проверяем правильный ли порт
        if (port < 0 || port > 0xFFFF)
            return false;

        // Отключаемся
        sendDisconnectMessage();

        // Ждем пока отключимся
        while (handler.state != STATE_DISCONNECTED);

        // Меняем порт
        settings.port = port;

        // Коннектимся
        sendConnectMessage();

        return true;
    }

    public Boolean setPassword(String password) {
        // Отключаемся
        sendDisconnectMessage();

        // Ждем пока отключимся
        while (handler.state != STATE_DISCONNECTED);

        // Меняем пароль
        settings.password = password;

        // Коннектимся
        sendConnectMessage();

        return true;
    }

    public String getIp() {
        return settings.ip;
    }

    public int getPort() {
        return settings.port;
    }

    public String getPassword() {
        return settings.password;
    }

    public class NetworkServiceBinder extends Binder {
        public NetworkService getServiceInstance() {
            return NetworkService.this;
        }
    }

    // -------------- Support -------------------

    Boolean parseIp(String ip) {
        // TODO запилить проверку ip на валидность
        return true;
    }

    void sendConnectMessage() {
        Message msg = handler.obtainMessage();
        msg.what = MESSAGE_CONNECT;
        handler.sendMessage(msg);
    }

    void sendDisconnectMessage() {
        Message msg = handler.obtainMessage();
        msg.what = MESSAGE_DISCONNECT;
        handler.sendMessage(msg);
    }
}
