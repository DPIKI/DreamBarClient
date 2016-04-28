package dpiki.dreamclient.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Iterator;

import dpiki.dreamclient.MenuActivity.MenuEntry;

/**
 * Created by User on 07.03.2016.
 */
public class DatabaseMenuWorker {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Dream.db";
    public static final String MENU_TABLE = "Menu";
    public static final String MENU_COLUMN_ID = "_id";
    public static final String MENU_COLUMN_NAME = "Name";
    public static final String MENU_COLUMN_CATEGORY = "Category";
    public static final String QUERY_SELECT_ALL = "SELECT * FROM " + MENU_TABLE + " ORDER BY " +
            MENU_COLUMN_CATEGORY + "," + MENU_COLUMN_NAME + ";";
    public static final String QUERY_DROP_MENU_TABLE = "DROP TABLE IF EXISTS " + MENU_TABLE + ";";
    public static final String QUERY_CREATE_MENU_TABLE = "CREATE TABLE " + MENU_TABLE + " (" +
            MENU_COLUMN_ID + " INTEGER PRIMARY KEY, " +
            MENU_COLUMN_CATEGORY + " TEXT, " +
            MENU_COLUMN_NAME + " TEXT);";


    public static ArrayList<MenuEntry> readMenu(SQLiteDatabase db) {
        ArrayList<MenuEntry> menu = new ArrayList<>();
        Cursor c = db.rawQuery(QUERY_SELECT_ALL, null);

        while (c.moveToNext()) {
            MenuEntry e = new MenuEntry();
            e.id = c.getInt(c.getColumnIndex(MENU_COLUMN_ID));
            e.name = c.getString(c.getColumnIndex(MENU_COLUMN_NAME));
            e.category = c.getString(c.getColumnIndex(MENU_COLUMN_CATEGORY));
            menu.add(e);
        }

        c.close();

        return menu;
    }

    public static void writeMenuEntry(SQLiteDatabase db, MenuEntry e) {
        ContentValues values = new ContentValues();
        values.put(MENU_COLUMN_ID, e.id);
        values.put(MENU_COLUMN_NAME, e.name);
        values.put(MENU_COLUMN_CATEGORY, e.category);
        db.insertWithOnConflict(MENU_TABLE, "", values, SQLiteDatabase.CONFLICT_IGNORE);
    }

    public static void writeMenuEntries(SQLiteDatabase db, ArrayList<MenuEntry> entries) {
        Iterator<MenuEntry> i = entries.iterator();
        while (i.hasNext()) {
            MenuEntry e = i.next();
            ContentValues values = new ContentValues();
            values.put(MENU_COLUMN_ID, e.id);
            values.put(MENU_COLUMN_NAME, e.name);
            values.put(MENU_COLUMN_CATEGORY, e.category);
            db.insertWithOnConflict(MENU_TABLE, "", values, SQLiteDatabase.CONFLICT_IGNORE);
        }
    }

    public static void clearMenu(SQLiteDatabase db) {
        db.execSQL(QUERY_DROP_MENU_TABLE);
        db.execSQL(QUERY_CREATE_MENU_TABLE);
    }
}
