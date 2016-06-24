package dpiki.dreamclient.Network;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.TreeSet;

import dpiki.dreamclient.R;

public class NetworkService extends Service {
    public static final String ACTION_NETWORK_SERVICE = "dpiki.dreamclient.action.networkService";

    // Имена полей в intent'ах ресиверу
    public static final String INTENT_STATE_CURR= "currState";
    public static final String INTENT_STATE_CHANGE_REASON = "reason";

    // Сообщения для рабочего потока
    public static final int MESSAGE_STOP_MAIN_SERVICE_THREAD = 0;
    public static final int MESSAGE_CONNECT = 1;
    public static final int MESSAGE_DISCONNECT = 2;
    public static final int MESSAGE_LOST_CONNECTION = 3;
    public static final int MESSAGE_AUTH_SUCCESS = 4;
    public static final int MESSAGE_WRONG_PASSWORD = 5;
    public static final int MESSAGE_SYNC_SUCCESS = 6;
    public static final int MESSAGE_INVALID_HASH = 7;
    public static final int MESSAGE_MENU_GOT = 8;
    public static final int MESSAGE_SEND_ORDER = 9;
    public static final int MESSAGE_ORDER_MADE = 10;
    public static final int MESSAGE_TICK = 11;
    public static final int MESSAGE_I_AM_HERE = 12;
    public static final int MESSAGE_IMAGE_LOADED = 13;
    public static final int MESSAGE_SEND_LOAD_IMAGE_REQUEST = 14;
    public static final int MESSAGE_NO_IMAGE = 15;
    public static final int MESSAGE_WIFI_DISABLED = 16;
    public static final int MESSAGE_WIFI_ENABLED = 17;
    public static final int MESSAGE_WIFI_CHECK = 18;
    public static final int MESSAGE_CALL = 19;

    // Состояния
    public static final int STATE_DISCONNECTED = 0;
    public static final int STATE_CONNECTING = 1;
    public static final int STATE_AUTH_WAIT = 2;
    public static final int STATE_AUTH_WRONG_PASSWORD = 4;
    public static final int STATE_SYNC_WAIT = 5;
    public static final int STATE_MENU_WAIT = 6;
    public static final int STATE_READY = 7;
    public static final int STATE_READY_WAIT = 8;
    public static final int STATE_WIFI_CHECK = 9;
    public static final int STATE_WIFI_DISABLED = 10;

    // Коды действий
    public static final int ACT_AUTHORIZE = 1;
    public static final int ACT_CHECK_SYNC = 2;
    public static final int ACT_MENU = 3;
    public static final int ACT_MAKE_ORDER = 4;
    public static final int ACT_CHECK_CONNECTION = 5;
    public static final int ACT_GET_IMAGE = 6;

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
    public static final int RESPONSE_I_AM_HERE = 11;
    public static final int RESPONSE_IMAGE = 12;
    public static final int RESPONSE_NO_IMAGE = 13;
    public static final int RESPONSE_CALL = 14;

    private NetworkServiceHandler handler;
    private NetworkServiceBinder binder;
    private NetworkServiceSettings settings;
    private Handler mUiHandler;
    private WifiStateChangeReceiver mWifiReceiver;

    // Список слушателей
    private HashSet<INetworkServiceListener> subscribers = new HashSet<>();

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
        mUiHandler = new Handler(this.getMainLooper()) {

            @Override
            public void handleMessage(Message msg) {
                for (INetworkServiceListener i : subscribers) {
                    if (msg.what == NetworkService.MESSAGE_IMAGE_LOADED) {
                        Bundle data = msg.getData();
                        byte[] image = data.getByteArray(NetworkServiceReader.KEY_IMAGE);
                        int id = data.getInt(NetworkServiceReader.KEY_IMAGE_ID);
                        i.onImageLoaded(id, image);
                    } else {
                        switch (msg.arg2) {
                            case NetworkService.STATE_CONNECTING:
                                i.onConnecting();
                                break;

                            case NetworkService.STATE_AUTH_WRONG_PASSWORD:
                                i.onWrongPassword();
                                break;

                            case NetworkService.STATE_WIFI_DISABLED:
                                i.onWifiDisabled();
                                break;

                            case NetworkService.STATE_DISCONNECTED:
                                i.onDisconnected();
                                break;

                            case NetworkService.STATE_READY:
                                if (msg.what == NetworkService.MESSAGE_SYNC_SUCCESS) {
                                    i.onReady();
                                }
                                if (msg.what == NetworkService.MESSAGE_ORDER_MADE) {
                                    i.onOrderMade();
                                }
                        }
                    }
                }
            }
        };

        // Читаем сохраненные настройки
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        settings.ip = pref.getString(getString(R.string.s_pref_key_ip), "192.168.0.1");
        settings.port = Integer.parseInt(pref.getString(getString(R.string.s_pref_key_port), "13563"));
        settings.isServiceRunning = pref.getBoolean(getString(R.string.s_pref_key_running), false);
        settings.password = pref.getString(getString(R.string.s_pref_key_password), "password");
        settings.name = pref.getString(getString(R.string.s_pref_key_name), "");

        // Вешаем слушателя изменения SharedPreferences
        pref.registerOnSharedPreferenceChangeListener(listener);

        // Создаем рабочий поток сервиса, который будет обрабатывать
        // сообщения от UI потока и дочерних потоков
        HandlerThread thread = new HandlerThread("Working Thread");
        thread.start();
        handler = new NetworkServiceHandler(thread.getLooper(),
                getBaseContext(), settings, mUiHandler);

        // Вешаем слушателя на изменение состояния WiFi
        mWifiReceiver = new WifiStateChangeReceiver(handler);
        registerReceiver(mWifiReceiver, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));

        // Говорим этому потоку создать соединение, если пользователь разрешил
        if (settings.isServiceRunning)
            sendConnectMessage();
    }

    @Override
    public void onDestroy() {
        // Говорим рабочему потоку завершиться
        sendDisconnectMessage();

        // Убираем слушателя изменения SharedPreferences
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        pref.unregisterOnSharedPreferenceChangeListener(listener);

        // Убираем слушателя состояния Wifi
        unregisterReceiver(mWifiReceiver);
    }

    // ---------------------------- Interface to application ----------------------------

    public int state() {
        return handler.state();
    }

    public static Boolean parseIp(String ip) {
        return ip.matches("^(((25[0-5])|(2[0-4][0-9])|(1[0-9]{2})|([1-9]?[0-9]))\\.){3}((25[0-5])|(2[0-4][0-9])|(1[0-9]{2})|([1-9]?[0-9]))$");
    }

    public void sendOrder() {
        Message msg = handler.obtainMessage();
        msg.what = NetworkService.MESSAGE_SEND_ORDER;
        handler.sendMessage(msg);
    }

    public class NetworkServiceBinder extends Binder {
        public NetworkService getServiceInstance() {
            return NetworkService.this;
        }
    }

    public void subscribe(INetworkServiceListener listener) {
        subscribers.add(listener);
        int state = handler.state();
        switch (state) {
            case NetworkService.STATE_AUTH_WRONG_PASSWORD:
                listener.onWrongPassword();
                break;

            case NetworkService.STATE_DISCONNECTED:
                listener.onDisconnected();
                break;

            case NetworkService.STATE_READY:
                listener.onReady();
                break;

            case NetworkService.STATE_WIFI_DISABLED:
                listener.onWifiDisabled();
                break;

            default:
                listener.onConnecting();
                break;
        }
    }

    public void unsubscribe(INetworkServiceListener listener) {
        subscribers.remove(listener);
    }

    public void downloadImage(int id) {
        Message msg = handler.obtainMessage();
        msg.what = MESSAGE_SEND_LOAD_IMAGE_REQUEST;
        msg.arg1 = id;
        handler.sendMessage(msg);
    }

    // -------------- Support -------------------

    private void sendConnectMessage() {
        Message msg = handler.obtainMessage();
        msg.what = MESSAGE_CONNECT;
        handler.sendMessage(msg);
    }

    private void sendDisconnectMessage() {
        Message msg = handler.obtainMessage();
        msg.what = MESSAGE_DISCONNECT;
        handler.sendMessage(msg);
    }

    private void connect() {
        // Запоминаем, что при следующем запуске надо сразу коннектиться
        settings.isServiceRunning = true;

        // Коннектимся
        sendConnectMessage();

        Log.d("Network Service", "connect");
    }

    private void disconnect() {
        // Запоминаем, что при коннектиться при запуске не надо
        settings.isServiceRunning = false;

        // Отключаемся
        sendDisconnectMessage();

        Log.d("Network Service", "disconnect");
    }

    private Boolean setIp(String ip) {
        // Проверяем переданную строку
        if (!parseIp(ip))
            return false;

        Boolean running = settings.isServiceRunning;
        disconnect();
        while (handler.state() != STATE_DISCONNECTED);
        settings.ip = ip;
        if (running)
            connect();

        Log.d("Network Service", "setIP");

        return true;
    }

    private Boolean setPort(int port) {
        // Проверяем правильный ли порт
        if (port < 0 || port > 0xFFFF)
            return false;

        Boolean running = settings.isServiceRunning;
        disconnect();
        while (handler.state() != STATE_DISCONNECTED);
        settings.port = port;
        if (running)
            connect();

        Log.d("Network Service", "setPort");

        return true;
    }

    private Boolean setPassword(String password) {
        // Проверяем длину строки
        if (password.length() > 30)
            return false;

        Boolean running = settings.isServiceRunning;
        disconnect();
        while (handler.state() != STATE_DISCONNECTED);
        settings.password = password;
        if (running)
            connect();

        Log.d("Network Service", "setPassword");

        return true;
    }

    private Boolean setName(String name) {
        // Проверяем длину строки
        if (name.length() > 30)
            return false;

        Boolean running = settings.isServiceRunning;
        disconnect();
        while (handler.state() != STATE_DISCONNECTED);
        settings.name = name;
        if (running)
            connect();

        Log.d("Network Service", "setName");

        return true;
    }

    private SharedPreferences.OnSharedPreferenceChangeListener listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.equals(getString(R.string.s_pref_key_ip))) {
                setIp(sharedPreferences.getString(key, "127.0.0.1"));
            } else if (key.equals(getString(R.string.s_pref_key_port))) {
                setPort(Integer.parseInt(sharedPreferences.getString(key, "0")));
            } else if (key.equals(getString(R.string.s_pref_key_password))) {
                setPassword(sharedPreferences.getString(key, "password"));
            } else if (key.equals(getString(R.string.s_pref_key_name))) {
                setName(sharedPreferences.getString(key, "name"));
            } else if (key.equals(getString(R.string.s_pref_key_running))) {
                Boolean is_running = sharedPreferences.getBoolean(key, false);
                if (is_running)
                    connect();
                else
                    disconnect();
            }
        }
    };
}
