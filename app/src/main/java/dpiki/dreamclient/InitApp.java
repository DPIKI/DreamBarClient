package dpiki.dreamclient;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * Created by User on 06.03.2016.
 */
public class InitApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Singleton.InitInstance();

        // Debug
        // Singleton.menu.add(new MenuEntry(1, "name1", "category1"));
        // Singleton.menu.add(new MenuEntry(1, "name2", "category1"));
        // Singleton.menu.add(new MenuEntry(1, "name1", "category2"));
        // Singleton.menu.add(new MenuEntry(1, "name2", "category2"));
        // Debug

        DatabaseHelper dbHelper = new DatabaseHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Singleton.menuFetch(db);
    }
}
