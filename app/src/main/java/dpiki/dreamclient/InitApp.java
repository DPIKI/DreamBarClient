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
    }
}
