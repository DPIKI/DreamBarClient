package dpiki.dreamclient.Network;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class NetworkService extends Service {
    public static final String ACTION_NETWORK_SERVICE = "dpiki.dreamclient.action.networkService";

    // Имена полей в intent'ах ресиверу
    public static final String INTENT_STATE_CURR= "currState";
    public static final String INTENT_STATE_PREV = "prevState";
    public static final String INTENT_STATE_CHANGE_REASON = "reason";

    // Сообщения для рабочего потока
    public static final int MESSAGE_STOP_MAIN_SERVICE_THREAD = 0;
    public static final int MESSAGE_CONNECT = 1;
    public static final int MESSAGE_DISCONNECT = 2;
    public static final int MESSAGE_LOST_CONNECTION = 3;
    public static final int MESSAGE_AUTH = 4;
    public static final int MESSAGE_AUTH_SUCCESS = 5;
    public static final int MESSAGE_WRONG_PASSWORD = 6;
    public static final int MESSAGE_SYNC = 7;
    public static final int MESSAGE_SYNC_SUCCESS = 8;
    public static final int MESSAGE_INVALID_HASH = 9;
    public static final int MESSAGE_MENU = 10;
    public static final int MESSAGE_MENU_GOT = 11;
    public static final int MESSAGE_SEND_ORDER = 12;
    public static final int MESSAGE_INVALID_REQUEST = 13;
    public static final int MESSAGE_OUT_OF_TRY = 15;

    // Состояния
    public static final int STATE_DISCONNECTED = 0;
    public static final int STATE_CONNECTING = 1;
    public static final int STATE_AUTH = 2;
    public static final int STATE_AUTH_WAIT = 3;
    public static final int STATE_SYNC = 4;
    public static final int STATE_SYNC_WAIT = 5;
    public static final int STATE_MENU = 6;
    public static final int STATE_MENU_WAIT = 7;
    public static final int STATE_READY = 8;
    public static final int STATE_READY_WAIT = 9;

    // Коды действий
    public static final int ACT_AUTHORIZE = 1;
    public static final int ACT_CHECK_SYNC = 2;
    public static final int ACT_MENU = 3;
    public static final int ACT_MAKE_ORDER = 4;

    // Коды ответов
    public static final int RESPONSE_AUTH_SUCCESS = 1;
    public static final int RESPONSE_SYNC_SUCCESS = 2;
    public static final int RESPONSE_MENU = 3;
    public static final int RESPONSE_ORDER_MADE = 4;
    public static final int RESPONSE_ERROR_INVALID_REQUEST = 5;
    public static final int RESPONSE_ERROR_INVALID_PASSWORD = 6;
    public static final int RESPONSE_ERROR_INVALID_HASH = 7;
    public static final int RESPONSE_ERROR_INVALID_COURSE_ID = 8;
    public static final int RESPONSE_ERROR_ACCESS_DENIED_AUTH = 9;
    public static final int RESPONSE_ERROR_ACCESS_DENIED_SYNC = 10;

    private NetworkServiceHandler handler;
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
        settings.name = pref.getString("name", "name");

        // Создаем рабочий поток сервиса, который будет обрабатывать
        // сообщения от UI потока и дочерних потоков
        HandlerThread thread = new HandlerThread("Working Thread");
        thread.start();
        handler = new NetworkServiceHandler(thread.getLooper(), getApplicationContext(), settings);

        // Говорим этому потоку создать соединение, если пользователь разрешил
        if (settings.isServiceRunning)
            sendConnectMessage();

        Toast.makeText(this, "NetworkService created", Toast.LENGTH_SHORT).show();
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
        editor.putString("name", settings.name);
        editor.putString("password", settings.password);
        editor.apply();

        Toast.makeText(this, "NetworkService destroyed", Toast.LENGTH_SHORT).show();
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

        disconnect();
        while (handler.state() != STATE_DISCONNECTED);
        settings.ip = ip;
        connect();
        return true;
    }

    public Boolean setPort(int port) {
        // Проверяем правильный ли порт
        if (port < 0 || port > 0xFFFF)
            return false;

        disconnect();
        while (handler.state() != STATE_DISCONNECTED);
        settings.port = port;
        connect();
        return true;
    }

    public Boolean setPassword(String password) {
        // Проверяем длину строки
        if (password.length() > 30)
            return false;

        disconnect();
        while (handler.state() != STATE_DISCONNECTED);
        settings.password = password;
        connect();
        return true;
    }

    public Boolean setName(String name) {
        // Проверяем длину строки
        if (name.length() > 30)
            return false;

        disconnect();
        while (handler.state() != STATE_DISCONNECTED);
        settings.name = name;
        connect();
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

    public String getName() {
        return settings.name;
    }

    public int state() {
        return handler.state();
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

    public class NetworkServiceBinder extends Binder {
        public NetworkService getServiceInstance() {
            return NetworkService.this;
        }
    }
}
