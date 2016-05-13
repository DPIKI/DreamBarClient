package dpiki.dreamclient.Network;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;

import dpiki.dreamclient.Database.DatabaseHelper;
import dpiki.dreamclient.Database.DatabaseOrderWorker;
import dpiki.dreamclient.OrderActivity.OrderEntry;

/**
 * Created by User on 30.03.2016.
 */
public class NetworkServiceWriter extends Thread {
    public static final String KEY_ACTION_CODE = "action_code";
    public static final String KEY_PASSWORD = "password";
    public static final String KEY_NAME = "name";
    public static final String KEY_HASH = "hash";
    public static final String KEY_ORDER = "orders";
    public static final String KEY_IMAGE_ID = "image_id";

    Socket socket;
    Bundle requestData;
    Context context;

    public NetworkServiceWriter(Context c, Socket s, Bundle b) {
        socket = s;
        requestData = b;
        context = c;
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
                    StringBuilder strOrder = new StringBuilder();
                    DatabaseHelper helper = new DatabaseHelper(context);
                    SQLiteDatabase db = helper.getReadableDatabase();
                    try {
                        ArrayList<OrderEntry> order = DatabaseOrderWorker.readOrder(db);
                        if (!order.isEmpty()) {
                            for (OrderEntry entry : order) {
                                strOrder.append(Integer.toString(entry.id))
                                        .append(";")
                                        .append(Integer.toString(entry.count))
                                        .append(";")
                                        .append(Integer.toString(entry.numTable))
                                        .append(";").append(entry.note)
                                        .append("\n");
                            }
                            strOrder.deleteCharAt(strOrder.length() - 1);
                        }
                    }
                    finally {
                        db.close();
                    }

                    requestJSONData.put(KEY_ORDER, strOrder.toString());
                    break;

                case NetworkService.ACT_GET_IMAGE:
                    int id = requestData.getInt(KEY_IMAGE_ID);
                    requestJSONData.put(KEY_IMAGE_ID, id);
                    break;
            }

            // Пишем их в сокет
            byte[] strRequestJSONData = requestJSONData.toString().getBytes();
            byte[] sizeByteArray = new byte[4];
            int size = strRequestJSONData.length;
            sizeByteArray[0] = (byte)(size & 0xff);
            sizeByteArray[1] = (byte)((size >>> 8) & 0xff);
            sizeByteArray[2] = (byte)((size >>> 16) & 0xff);
            sizeByteArray[3] = (byte)((size >>> 24) & 0xff);
            os.write(sizeByteArray);
            os.write(strRequestJSONData);
            os.flush();

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }
}
