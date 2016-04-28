package dpiki.dreamclient.Database;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by prog1 on 28.04.2016.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    public DatabaseHelper(Context context) {
        super(context, DatabaseMenuWorker.DATABASE_NAME, null, DatabaseMenuWorker.DATABASE_VERSION);
    }

    public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory,
                          int version) {
        super(context, name, factory, version);
    }

    public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory,
                          int version, DatabaseErrorHandler handler) {
        super(context, name, factory, version, handler);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DatabaseMenuWorker.QUERY_CREATE_MENU_TABLE);
        db.execSQL(DatabaseOrderWorker.QUERY_CREATE_ORDER_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DatabaseMenuWorker.QUERY_DROP_MENU_TABLE);
        db.execSQL(DatabaseMenuWorker.QUERY_CREATE_MENU_TABLE);
        db.execSQL(DatabaseOrderWorker.QUERY_DROP_ORDER_TABLE);
        db.execSQL(DatabaseOrderWorker.QUERY_CREATE_ORDER_TABLE);
    }
}
