package dpiki.dreamclient;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Created by User on 06.03.2016.
 */
public class Singleton {
    private static Singleton singleton;

    public static ArrayList<MenuEntry> menu;

    public static void InitInstance() {
        if (singleton == null) {
            singleton = new Singleton();
        }
    }

    public static ArrayList<String> menuGetCategories() {
        HashSet<String> categories = new HashSet<>();

        Iterator<MenuEntry> i = menu.iterator();
        while (i.hasNext())
            categories.add(i.next().category);

        return new ArrayList<>(categories);
    }

    public static ArrayList<String> menuGetItemNames(String category) {
        ArrayList<String> names = new ArrayList<>();

        Iterator<MenuEntry> i = menu.iterator();
        while (i.hasNext()) {
            MenuEntry e = i.next();
            if (e.category.equals(category))
                names.add(e.name);
        }

        return names;
    }

    public static void menuFetch(SQLiteDatabase db) {
        menu = DatabaseHelper.readMenu(db);
    }

    public static Singleton getInstance() {
        return singleton;
    }

    private Singleton() {
        menu = new ArrayList<>();
    }
}
