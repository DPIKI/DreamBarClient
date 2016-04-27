package dpiki.dreamclient.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Iterator;

import dpiki.dreamclient.OrderActivity.OrderEntry;

/**
 * Created by prog1 on 24.04.2016.
 */
public class DatabaseOrderHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Dream.db";
    public static final String ORDER_TABLE = "OrderTable";
    public static final String ORDER_COLUMN_ID = "_id";
    public static final String ORDER_COLUMN_COUNT = "Count";
    public static final String ORDER_COLUMN_NOTE = "Note";
    public static final String ORDER_COLUMN_NUM_TABLE = "tableNum";
    private static final String QUERY_SELECT_ALL = "SELECT * FROM " + ORDER_TABLE + ";";
    private static final String QUERY_DROP_ORDER_TABLE =
            "DROP TABLE IF EXISTS " + ORDER_TABLE + ";";
    private static final String QUERY_CREATE_ORDER_TABLE =
            "CREATE TABLE " + ORDER_TABLE + " (" +
            ORDER_COLUMN_ID + " INTEGER PRIMARY KEY, " +
            ORDER_COLUMN_COUNT + " INTEGER, " +
            ORDER_COLUMN_NOTE + " TEXT, " +
            ORDER_COLUMN_NUM_TABLE + " INTEGER);";

    public DatabaseOrderHelper(Context context, String name,
                               SQLiteDatabase.CursorFactory factory,
                               int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(QUERY_CREATE_ORDER_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(QUERY_DROP_ORDER_TABLE);
        db.execSQL(QUERY_CREATE_ORDER_TABLE);
    }

    public static ArrayList<OrderEntry> readOrder(SQLiteDatabase db){
        ArrayList<OrderEntry> orderEntries = new ArrayList<>();
        Cursor c = db.rawQuery(QUERY_SELECT_ALL, null);

        while (c.moveToNext()){
            OrderEntry orderEntry = new OrderEntry();
            orderEntry.id = c.getInt(c.getColumnIndex(ORDER_COLUMN_ID));
            orderEntry.count = c.getInt(c.getColumnIndex(ORDER_COLUMN_COUNT));
            orderEntry.numTable = c.getInt(
                    c.getColumnIndex(ORDER_COLUMN_NUM_TABLE));
            orderEntry.note = c.getString(c.getColumnIndex(ORDER_COLUMN_NOTE));
            orderEntries.add(orderEntry);
        }

        c.close();
        return orderEntries;
    }

    public static void writeOrderEntry(SQLiteDatabase db,
                                       OrderEntry orderEntry){
        ContentValues values = new ContentValues();
        values.put(ORDER_COLUMN_ID, orderEntry.id);
        values.put(ORDER_COLUMN_COUNT, orderEntry.count);
        values.put(ORDER_COLUMN_NUM_TABLE, orderEntry.numTable);

        db.insertWithOnConflict(ORDER_TABLE, "", values, SQLiteDatabase.CONFLICT_IGNORE);
    }

    public static void writeOrderEntries(SQLiteDatabase db,
                                         ArrayList<OrderEntry> orderEntries){
        Iterator<OrderEntry> i = orderEntries.iterator();
        while (i.hasNext()){
            OrderEntry orderEntry = i.next();
            ContentValues values = new ContentValues();
            values.put(ORDER_COLUMN_ID, orderEntry.id);
            values.put(ORDER_COLUMN_COUNT, orderEntry.count);
            values.put(ORDER_COLUMN_NUM_TABLE, orderEntry.numTable);
            db.insertWithOnConflict(ORDER_TABLE, "", values,
                    SQLiteDatabase.CONFLICT_IGNORE);

        }
    }

    public DatabaseOrderHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static void clearMenu(SQLiteDatabase db) {
        db.execSQL(QUERY_DROP_ORDER_TABLE);
        db.execSQL(QUERY_CREATE_ORDER_TABLE);
    }
}
