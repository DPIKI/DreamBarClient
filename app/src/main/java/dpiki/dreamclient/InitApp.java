package dpiki.dreamclient;

import android.app.Application;

/**
 * Created by User on 06.03.2016.
 */
public class InitApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Singletone.InitInstance();
        // Debug
        Singletone.menu.add(new MenuEntry(1, "name1", "category1"));
        Singletone.menu.add(new MenuEntry(1, "name2", "category1"));
        Singletone.menu.add(new MenuEntry(1, "name1", "category2"));
        Singletone.menu.add(new MenuEntry(1, "name2", "category2"));
        // Debug
    }
}
