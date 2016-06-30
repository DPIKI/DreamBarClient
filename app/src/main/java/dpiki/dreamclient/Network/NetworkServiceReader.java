package dpiki.dreamclient.Network;

import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import dpiki.dreamclient.Database.DatabaseHelper;
import dpiki.dreamclient.Database.DatabaseMenuWorker;
import dpiki.dreamclient.Database.DatabaseOrderWorker;
import dpiki.dreamclient.MenuActivity.MenuEntry;
import dpiki.dreamclient.R;

/**
 * Created by User on 26.03.2016.
 */
public class NetworkServiceReader extends Thread {

    class ReceivedData {
        public JSONObject root;
        public byte[] appendix;
    }

    public static final String KEY_RESPONSE_CODE = "response_code";
    public static final String KEY_MENU = "menu";
    public static final String KEY_IMAGE = "image";
    public static final String KEY_IMAGE_ID = "image_id";

    private NetworkServiceHandler handler;
    private Socket socket;
    private InputStream is;

    public NetworkServiceReader(NetworkServiceHandler h, Socket s) {
        handler = h;
        socket = s;
    }

    @Override
    public void run() {
        try {
            // Открываем входной поток
            is = socket.getInputStream();

            // Запускаем цикл обработки сообщений от сервера
            while (true) {
                ReceivedData receivedData = readMessage();
                int responseCode = receivedData.root.getInt(KEY_RESPONSE_CODE);

                Log.d("NetworkService", "responseCode = " + Integer.toString(responseCode));

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
                        msg.what = NetworkService.MESSAGE_ORDER_MADE;
                        break;

                    case NetworkService.RESPONSE_MENU:
                        saveMenu(receivedData);
                        msg.what = NetworkService.MESSAGE_MENU_GOT;
                        break;

                    case NetworkService.RESPONSE_ERROR_INVALID_HASH:
                        msg.what = NetworkService.MESSAGE_INVALID_HASH;
                        break;

                    case NetworkService.RESPONSE_ERROR_INVALID_PASSWORD:
                        msg.what = NetworkService.MESSAGE_WRONG_PASSWORD;
                        break;

                    case NetworkService.RESPONSE_I_AM_HERE:
                        msg.what = NetworkService.MESSAGE_I_AM_HERE;
                        break;

                    case NetworkService.RESPONSE_CALL:
                        msg.what = NetworkService.MESSAGE_CALL;
                        break;

                    case NetworkService.RESPONSE_IMAGE:
                    case NetworkService.RESPONSE_NO_IMAGE:
                        msg.what = NetworkService.MESSAGE_IMAGE_LOADED;
                        Bundle bundle = new Bundle();
                        bundle.putByteArray(KEY_IMAGE, receivedData.appendix);
                        bundle.putInt(KEY_IMAGE_ID, receivedData.root.getInt(KEY_IMAGE_ID));
                        msg.setData(bundle);
                        break;

                    default:
                        msg.what = NetworkService.MESSAGE_LOST_CONNECTION;
                        break;
                }
                handler.sendMessage(msg);
            }
        }
        catch (IOException | JSONException e) {
            // Если что-то пошло не так говорим что мы отключились
            Message msg = handler.obtainMessage();
            msg.what = NetworkService.MESSAGE_LOST_CONNECTION;
            handler.sendMessage(msg);
            e.printStackTrace();
        }
        finally {
            is = null;
        }
    }

    ReceivedData readMessage() throws IOException {
        try {
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4);
            byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

            // Читаем размер сообщения
            byte[] buffer = new byte[4];
            readByteArray(buffer);
            byteBuffer.clear();
            byteBuffer.put(buffer, 0, 4);
            int msgSize = byteBuffer.getInt(0);

            // Если размер пакета больше 100 Мб или меньше нуля то скорее всего произошла ошибка
            if (msgSize < 4 || msgSize > 100000000)
                throw new IOException();

            // Выделяем буфер этого размера и читаем в него данные
            byte[] data = new byte[msgSize];
            readByteArray(data);

            // Читаем размер данных с json
            byteBuffer.clear();
            byteBuffer.put(data, 0, 4);
            int jsonSize = byteBuffer.getInt(0);

            // Проверяем размер на валидность
            if (jsonSize < 0 || jsonSize > msgSize - 4)
                throw new IOException();

            // Парсим JSON
            ReceivedData receivedData = new ReceivedData();
            String strJsonData = new String(data, 4, jsonSize);
            receivedData.root = new JSONObject(strJsonData);

            // Если есть аппендикс, читаем м его
            if (jsonSize < msgSize - 4) {
                int appendixSize = msgSize - 4 - jsonSize;
                receivedData.appendix = new byte[appendixSize];
                System.arraycopy(data, jsonSize + 4, receivedData.appendix, 0, appendixSize);
            }
            else {
                receivedData.appendix = null;
            }

            return receivedData;
        }
        catch (JSONException e) {
            throw new IOException();
        }
    }


    void saveMenu(ReceivedData receivedData) throws IOException {
        try {
            String strMenu = receivedData.root.getString(NetworkServiceReader.KEY_MENU);

            // Парсим меню
            ArrayList<MenuEntry> menuEntries = new ArrayList<>();
            if (!strMenu.isEmpty()) {
                String[] menuItems = strMenu.split("\n");
                for (String menuItem : menuItems) {
                    String[] menuItemColumns = menuItem.split(";");
                    if (menuItemColumns.length != 3)
                        throw new IOException();
                    MenuEntry entry = new MenuEntry();
                    entry.id = Integer.parseInt(menuItemColumns[0]);
                    entry.name = menuItemColumns[1];
                    entry.category = menuItemColumns[2];
                    menuEntries.add(entry);
                }
            }

            // Записываем меню в базу
            DatabaseHelper databaseHelper = new DatabaseHelper(handler.context);
            SQLiteDatabase db = databaseHelper.getWritableDatabase();
            try {
                DatabaseMenuWorker.clearMenu(db);
                DatabaseMenuWorker.writeMenuEntries(db, menuEntries);
                DatabaseOrderWorker.clearOrder(db);
            }
            finally {
                db.close();
            }

            // Вычисляем хэш
            MessageDigest messageDigest = MessageDigest.getInstance("SHA256");
            byte[] digest = messageDigest.digest(strMenu.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte aDigest : digest) {
                sb.append(String.format("%02X", aDigest));
            }
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(handler.context);
            SharedPreferences.Editor editor = pref.edit();
            editor.putString(handler.context.getString(R.string.s_pref_key_hash), sb.toString());
            editor.commit();
        }
        catch (JSONException | NoSuchAlgorithmException e) {
            throw new IOException();
        }
    }

    void readByteArray(byte[] dst) throws IOException {
        int readBytes = 0;
        while (readBytes < dst.length) {
            int readNow = is.read(dst, readBytes, dst.length - readBytes);
            if (readNow == -1)
                throw new IOException();
            readBytes += readNow;
        }
    }
}
