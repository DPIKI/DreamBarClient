package dpiki.dreamclient.Network;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Message;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by User on 26.03.2016.
 */
public class NetworkServiceReader extends Thread {

    private static final String KEY_RESPONSE_CODE = "response_code";

    private NetworkServiceHandler handler;
    private Socket socket;

    public NetworkServiceReader(NetworkServiceHandler h, Socket s) {
        handler = h;
        socket = s;
    }

    @Override
    public void run() {

        // Хэш меню
        InputStream is;
        SharedPreferences pref =
                handler.context.getSharedPreferences("NetworkSettings", Context.MODE_PRIVATE);
        String menuHash = pref.getString("menuHash", "");

        try {
            // Открываем входной поток
            is = socket.getInputStream();
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4);
            byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
            byte[] buffer = new byte[4];

            // Запускаем цикл обработки сообщений от сервера
            while (true) {
                // Читаем размер сообщения
                if (is.read(buffer) == -1)
                    throw new IOException();
                byteBuffer.clear();
                byteBuffer.put(buffer, 0, 4);
                int msgSize = byteBuffer.getInt(0);

                // Выделяем буфер этого размера и читаем в него данные
                byte[] data = new byte[msgSize];
                if (is.read(data) == -1)
                    throw new IOException();

                // Читаем размер данных с json
                byteBuffer.clear();
                byteBuffer.put(data, 0, 4);
                int jsonSize = byteBuffer.getInt(0);

                // Читаем код ответа
                String strJsonData = new String(data, 4, jsonSize);
                JSONObject jsonRoot = new JSONObject(strJsonData);
                int responseCode = jsonRoot.getInt(KEY_RESPONSE_CODE);

                // Шлем сообщения в зависимости от кода ответа
                Message msg = handler.obtainMessage();
                switch (responseCode) {
                    case NetworkService.RESPONSE_AUTH_SUCCESS:
                        msg.what = NetworkService.MESSAGE_AUTH_SUCCESS;
                        break;

                    case NetworkService.RESPONSE_SYNC_SUCCESS:
                        msg.what = NetworkService.MESSAGE_SYNC_SUCCESS;
                        break;

                    case NetworkService.RESPONSE_ORDER_MADE:
                        // TODO: запилить обработку сообщения "заказ сделан"
                        msg.what = -1;
                        break;

                    case NetworkService.RESPONSE_MENU:
                        // TODO: запилить обработку меню
                        msg.what = -1;
                        break;

                    case NetworkService.RESPONSE_ERROR_INVALID_HASH:
                        msg.what = NetworkService.MESSAGE_INVALID_HASH;
                        break;

                    case NetworkService.RESPONSE_ERROR_INVALID_REQUEST:
                        msg.what = NetworkService.MESSAGE_LOST_CONNECTION;
                        break;

                    case NetworkService.RESPONSE_ERROR_INVALID_PASSWORD:
                        msg.what = NetworkService.MESSAGE_WRONG_PASSWORD;
                        break;

                    case NetworkService.RESPONSE_ERROR_INVALID_COURSE_ID:
                        // TODO: запилить обработку неправильного id блюда
                        msg.what = -1;
                        break;

                    case NetworkService.RESPONSE_ERROR_ACCESS_DENIED_AUTH:
                        // TODO: запилить обработку ошибки авторизации
                        msg.what = -1;
                        break;

                    case NetworkService.RESPONSE_ERROR_ACCESS_DENIED_SYNC:
                        // TODO: запилить обработку ошибки синхронизации
                        msg.what = -1;
                        break;
                }
                handler.sendMessage(msg);
            }
        }
        catch (IOException | OutOfMemoryError | JSONException e) {
            // Если что-то пошло не так говорим что мы отключились
            Message msg = handler.obtainMessage();
            msg.what = NetworkService.MESSAGE_LOST_CONNECTION;
            handler.sendMessage(msg);
        }
        finally {
            is = null;
            SharedPreferences.Editor editor = pref.edit();
            editor.putString("menuHash", menuHash);
            editor.commit();
        }
    }
}
