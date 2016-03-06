package dpiki.dreamclient;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

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

    public static ArrayList<String> menuGetCategories() {
        HashSet<String> categories = new HashSet<>();

        Iterator<MenuEntry> i = menu.iterator();
        while (i.hasNext())
            categories.add(i.next().category);

        return new ArrayList<String>(categories);
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

    public static Singletone getInstance() {
        return singletone;
    }

    private Singletone() {
        menu = new ArrayList<>();
    }
}
