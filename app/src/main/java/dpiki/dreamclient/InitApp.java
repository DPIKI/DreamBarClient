package dpiki.dreamclient;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;

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
    }
}
