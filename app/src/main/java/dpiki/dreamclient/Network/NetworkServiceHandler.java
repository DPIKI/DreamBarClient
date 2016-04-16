package dpiki.dreamclient.Network;

import android.content.Context;
import android.content.Intent;
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

    NetworkServiceHandler(Looper looper, Context ctx, NetworkServiceSettings stngs) {
        super(looper);
        context = ctx;
        settings = stngs;
        processor = new DisconnectedMessageProcessor(this);
    }

    @Override
    public void handleMessage(Message message) {
        switch (message.what) {
            case NetworkService.MESSAGE_CONNECT:
                processor.onConnect();
                break;

            case NetworkService.MESSAGE_DISCONNECT:
                processor.onDisconnect();
                break;

            case NetworkService.MESSAGE_AUTH:
                processor.onAuth();
                break;

            case NetworkService.MESSAGE_AUTH_SUCCESS:
                processor.onAuthSuccess();
                break;

            case NetworkService.MESSAGE_WRONG_PASSWORD:
                processor.onWrongPassword();
                break;

            case NetworkService.MESSAGE_SYNC:
                processor.onSync();
                break;

            case NetworkService.MESSAGE_SYNC_SUCCESS:
                processor.onSyncSuccess();
                break;

            case NetworkService.MESSAGE_INVALID_HASH:
                processor.onInvalidHash();
                break;

            case NetworkService.MESSAGE_MENU:
                processor.onMenu();
                break;

            case NetworkService.MESSAGE_MENU_GOT:
                processor.onMenuGot();
                break;

            case NetworkService.MESSAGE_LOST_CONNECTION:
                processor.onLostConnection();
                break;

            case NetworkService.MESSAGE_SEND_ORDER:
                processor.onSendOrder();
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

    public void changeState(IMessageProcessor p, int reason) {
        Intent intent = new Intent(NetworkService.ACTION_NETWORK_SERVICE);
        intent.putExtra("prevState", processor.state());
        intent.putExtra("currState", p.state());
        intent.putExtra("reason", reason);
        processor = p;
        context.sendBroadcast(intent);
    }

    public int state() {
        return processor.state();
    }
}
