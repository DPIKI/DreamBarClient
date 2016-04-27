package dpiki.dreamclient.MenuActivity;

import java.util.ArrayList;

/**
 * Created by User on 06.03.2016.
 */
public class MenuEntry {
    public int id;
    public String name;
    public String category;

    public MenuEntry(int a_id, String a_name, String a_category) {
        id = a_id;
        name = a_name;
        category = a_category;
    }

    public MenuEntry() {
        id = 0;
        name = "";
        category = "";
    }

}
