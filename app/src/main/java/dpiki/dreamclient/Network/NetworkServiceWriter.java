package dpiki.dreamclient.Network;

import android.os.Bundle;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by User on 30.03.2016.
 */
public class NetworkServiceWriter extends Thread {
    public static final String KEY_ACTION_CODE = "action_code";
    public static final String KEY_PASSWORD = "password";
    public static final String KEY_NAME = "name";
    public static final String KEY_HASH = "hash";

    Socket socket;
    Bundle requestData;

    public NetworkServiceWriter(Socket s, Bundle b) {
        socket = s;
        requestData = b;
    }

    @Override
    public void run() {
        try {
            // Создаем поток для вывода
            OutputStream os = socket.getOutputStream();
            JSONObject requestJSONData = new JSONObject();

            // Формируем данные в JSON формате
            int actionCode = requestData.getInt(KEY_ACTION_CODE);
            requestJSONData.put(KEY_ACTION_CODE, actionCode);
            switch (actionCode) {
                case NetworkService.ACT_AUTHORIZE:
                    String name = requestData.getString(KEY_NAME);
                    String password = requestData.getString(KEY_PASSWORD);
                    requestJSONData.put(KEY_NAME, name);
                    requestJSONData.put(KEY_PASSWORD, password);
                    break;

                case NetworkService.ACT_CHECK_SYNC:
                    String hash = requestData.getString(KEY_HASH);
                    requestJSONData.put(KEY_HASH, hash);
                    break;

                case NetworkService.ACT_MENU:
                    break;

                case NetworkService.ACT_MAKE_ORDER:
                    break;
            }

            // Пишем их в сокет
            byte[] strRequestJSONData = requestJSONData.toString().getBytes();
            ByteBuffer sizeBuffer = ByteBuffer.allocateDirect(4);
            sizeBuffer.order(ByteOrder.LITTLE_ENDIAN);
            sizeBuffer.putInt(0, strRequestJSONData.length);
            os.write(sizeBuffer.array());
            os.write(strRequestJSONData);
            os.flush();

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }
}
