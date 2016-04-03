package dpiki.dreamclient;

import android.app.Application;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;

import dpiki.dreamclient.Network.NetworkService;

/**
 * Created by User on 06.03.2016.
 */
public class InitApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Singleton.InitInstance();

        DatabaseHelper dbHelper = new DatabaseHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Singleton.menuFetch(db);

        // На всякий случай запускаем сетевой сервис
        Intent intent = new Intent(getApplicationContext(), NetworkService.class);
        startService(intent);
    }
}
