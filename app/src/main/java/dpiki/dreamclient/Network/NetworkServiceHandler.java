package dpiki.dreamclient.Network;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.io.IOException;
import java.net.Socket;

import dpiki.dreamclient.Network.MessageProcessors.DisconnectedMessageProcessor;
import dpiki.dreamclient.Network.MessageProcessors.IMessageProcessor;

/**
 * Created by User on 26.03.2016.
 */
public class NetworkServiceHandler extends Handler {

    // Ресурсы, которые надо освобождать
    public Socket socket;

    // Контекст приложения
    public Context context;

    // Настройки сервера
    public NetworkServiceSettings settings;

    // Обработчик сообщений для текущего состояния
    private IMessageProcessor processor;

    // Счетчик тиков таймера
    public int mTimerTicks = 0;

    private Handler mUiHandler;

    NetworkServiceHandler(Looper looper, Context ctx, NetworkServiceSettings stngs, Handler uiHandler) {
        super(looper);
        context = ctx;
        settings = stngs;
        processor = new DisconnectedMessageProcessor(this);
        mUiHandler = uiHandler;
    }

    @Override
    public void handleMessage(Message message) {
        switch (message.what) {
            case NetworkService.MESSAGE_CONNECT:
                processor.onConnect(message);
                break;

            case NetworkService.MESSAGE_DISCONNECT:
                processor.onDisconnect(message);
                break;

            case NetworkService.MESSAGE_AUTH_SUCCESS:
                processor.onAuthSuccess(message);
                break;

            case NetworkService.MESSAGE_WRONG_PASSWORD:
                processor.onWrongPassword(message);
                break;

            case NetworkService.MESSAGE_SYNC_SUCCESS:
                processor.onSyncSuccess(message);
                break;

            case NetworkService.MESSAGE_INVALID_HASH:
                processor.onInvalidHash(message);
                break;

            case NetworkService.MESSAGE_MENU_GOT:
                processor.onMenuGot(message);
                break;

            case NetworkService.MESSAGE_LOST_CONNECTION:
                processor.onLostConnection(message);
                break;

            case NetworkService.MESSAGE_SEND_ORDER:
                processor.onSendOrder(message);
                break;

            case NetworkService.MESSAGE_ORDER_MADE:
                processor.onOrderMade(message);
                break;

            case NetworkService.MESSAGE_TICK:
                processor.onTick(message);
                break;

            case NetworkService.MESSAGE_I_AM_HERE:
                processor.onIAmHere(message);
                break;

            case NetworkService.MESSAGE_IMAGE_LOADED:
                processor.onImageLoaded(message);
                break;

            case NetworkService.MESSAGE_SEND_LOAD_IMAGE_REQUEST:
                processor.onSendLoadImageRequest(message);
                break;

            case NetworkService.MESSAGE_WIFI_CHECK:
                processor.onCheckWifi(message);
                break;

            case NetworkService.MESSAGE_WIFI_DISABLED:
                processor.onWifiDisabled(message);
                break;

            case NetworkService.MESSAGE_WIFI_ENABLED:
                processor.onWifiEnabled(message);
                break;

            case NetworkService.MESSAGE_STOP_MAIN_SERVICE_THREAD:
                clearResources();
                changeState(new DisconnectedMessageProcessor(this),
                        NetworkService.MESSAGE_STOP_MAIN_SERVICE_THREAD);
                getLooper().quit();
                break;
        }
    }

    // ------------------- Support ---------------------

    public void clearResources() {
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

    /* msg.what - причина перехода
     * msg.arg1 - предыдущее состояние
     * msg.arg2 - следующее состояние
     */
    public void changeState(IMessageProcessor p, int reason) {
        Message msg = mUiHandler.obtainMessage();
        msg.what = reason;
        msg.arg1 = processor.state();
        msg.arg2 = p.state();
        processor = p;
        mUiHandler.sendMessage(msg);
    }

    public void sendImageToUi(Bundle bundle) {
        Message msg = mUiHandler.obtainMessage();
        msg.what = NetworkService.MESSAGE_IMAGE_LOADED;
        msg.setData(bundle);
        mUiHandler.sendMessage(msg);
    }

    public int state() {
        return processor.state();
    }
}
