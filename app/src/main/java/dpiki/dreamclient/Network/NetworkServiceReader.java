package dpiki.dreamclient.Network;

import android.database.sqlite.SQLiteDatabase;
import android.os.Message;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import dpiki.dreamclient.DatabaseHelper;
import dpiki.dreamclient.MenuEntry;

/**
 * Created by User on 26.03.2016.
 */
public class NetworkServiceReader extends Thread {

    class ReceivedData {
        public JSONObject root;
        public byte[] appendix;
    }

    private static final String KEY_RESPONSE_CODE = "response_code";
    private static final String KEY_MENU = "menu";

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
                        saveMenu(receivedData);
                        msg.what = NetworkService.MESSAGE_MENU_GOT;
                        break;

                    case NetworkService.RESPONSE_ERROR_INVALID_HASH:
                        msg.what = NetworkService.MESSAGE_INVALID_HASH;
                        break;

                    case NetworkService.RESPONSE_ERROR_INVALID_PASSWORD:
                        msg.what = NetworkService.MESSAGE_WRONG_PASSWORD;
                        break;

                    case NetworkService.RESPONSE_ERROR_INVALID_COURSE_ID: // TODO: запилить обработку неправильного id блюда
                    case NetworkService.RESPONSE_ERROR_INVALID_REQUEST:
                    case NetworkService.RESPONSE_ERROR_ACCESS_DENIED_AUTH:
                    case NetworkService.RESPONSE_ERROR_ACCESS_DENIED_SYNC:
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
        }
        finally {
            is = null;
        }
    }

    ReceivedData readMessage() throws IOException {
        try {
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4);
            byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
            byte[] buffer = new byte[4];

            // Читаем размер сообщения
            if (is.read(buffer) == -1)
                throw new IOException();
            byteBuffer.clear();
            byteBuffer.put(buffer, 0, 4);
            int msgSize = byteBuffer.getInt(0);

            // Если размер пакета больше 100 Мб или меньше нуля то скорее всего произошла ошибка
            if (msgSize < 4 || msgSize > 1000000)
                throw new IOException();

            // Выделяем буфер этого размера и читаем в него данные
            byte[] data = new byte[msgSize];
            if (is.read(data) == -1)
                throw new IOException();

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
            DatabaseHelper helper = new DatabaseHelper(handler.context);
            SQLiteDatabase db = helper.getWritableDatabase();
            try {
                DatabaseHelper.clearMenu(db);
                DatabaseHelper.writeMenuEntries(db, menuEntries);
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
            handler.settings.hash = sb.toString();
        }
        catch (JSONException | NoSuchAlgorithmException e) {
            throw new IOException();
        }
    }
}
