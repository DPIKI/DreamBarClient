package dpiki.dreamclient;

import java.util.ArrayList;

/**
 * Created by User on 06.03.2016.
 */
public class Singletone {
    private static Singletone singletone;

    public static ArrayList<MenuEntry> menu;

    public static void InitInstance() {
        if (singletone == null) {
            singletone = new Singletone();
        }
    }

    public static Singletone getInstance() {
        return singletone;
    }

    private Singletone() {
        menu = new ArrayList<>();
    }
}
