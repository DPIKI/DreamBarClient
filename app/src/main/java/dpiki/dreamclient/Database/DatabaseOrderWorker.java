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
public class DatabaseOrderWorker {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Dream.db";
    public static final String ORDER_TABLE = "OrderTable";
    public static final String ORDER_COLUMN_ID = "_id";
    public static final String ORDER_COLUMN_COUNT = "Count";
    public static final String ORDER_COLUMN_NOTE = "Note";
    public static final String ORDER_COLUMN_NUM_TABLE = "tableNum";
    public static final String QUERY_DROP_ORDER_TABLE =
            "DROP TABLE IF EXISTS " + ORDER_TABLE + ";";
    public static final String QUERY_CREATE_ORDER_TABLE =
            "CREATE TABLE " + ORDER_TABLE + " (" +
                    ORDER_COLUMN_ID + " INTEGER, " +
                    ORDER_COLUMN_COUNT + " INTEGER, " +
                    ORDER_COLUMN_NOTE + " TEXT, " +
                    ORDER_COLUMN_NUM_TABLE + " INTEGER);";

    public static final String QUERY_SELECT_ALL = "SELECT " +
            ORDER_TABLE + ".rowid, " +
            ORDER_TABLE + "." + ORDER_COLUMN_ID + ", " +
            ORDER_TABLE + "." + ORDER_COLUMN_COUNT + ", " +
            ORDER_TABLE + "." + ORDER_COLUMN_NOTE + ", " +
            ORDER_TABLE + "." + ORDER_COLUMN_NUM_TABLE + ", " +
            DatabaseMenuWorker.MENU_TABLE + "." + DatabaseMenuWorker.MENU_COLUMN_NAME +
            " FROM " + ORDER_TABLE + " INNER JOIN " + DatabaseMenuWorker.MENU_TABLE +
            " WHERE " + ORDER_TABLE + "." + ORDER_COLUMN_ID + " == " +
            DatabaseMenuWorker.MENU_TABLE + "." + DatabaseMenuWorker.MENU_COLUMN_ID + ";";


    public static ArrayList<OrderEntry> readOrder(SQLiteDatabase db){
        ArrayList<OrderEntry> orderEntries = new ArrayList<>();
        Cursor c = db.rawQuery(QUERY_SELECT_ALL, null);

        while (c.moveToNext()){
            OrderEntry orderEntry = new OrderEntry();
            orderEntry.rowId = c.getInt(0);
            orderEntry.id = c.getInt(1);
            orderEntry.count = c.getInt(2);
            orderEntry.note = c.getString(3);
            orderEntry.numTable = c.getInt(4);
            orderEntry.name = c.getString(5);
            orderEntries.add(orderEntry);
        }

        c.close();
        return orderEntries;
    }

    public static void writeOrderEntry(SQLiteDatabase db,
                                       OrderEntry orderEntry){
        ContentValues values = new ContentValues();
        values.put(ORDER_COLUMN_ID, orderEntry.id);
        values.put(ORDER_COLUMN_NOTE, orderEntry.note);
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

    public static void clearOrder(SQLiteDatabase db) {
        db.execSQL(QUERY_DROP_ORDER_TABLE);
        db.execSQL(QUERY_CREATE_ORDER_TABLE);
    }
}
