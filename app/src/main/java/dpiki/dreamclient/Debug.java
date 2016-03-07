package dpiki.dreamclient;

import android.database.sqlite.SQLiteDatabase;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.sql.SQLException;
import java.util.ArrayList;

public class Debug extends AppCompatActivity {
    EditText editId;
    EditText editName;
    EditText editCategory;
    DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);

        editId = (EditText)findViewById(R.id.e_debugId);
        editName = (EditText)findViewById(R.id.e_debugName);
        editCategory = (EditText)findViewById(R.id.e_debugCategory);

        dbHelper = new DatabaseHelper(this);
    }

    public void onClickAdd(View view) {
        try {
            MenuEntry me = new MenuEntry();
            me.id = Integer.parseInt(editId.getText().toString());
            me.name = editName.getText().toString();
            me.category = editCategory.getText().toString();

            Singleton s = Singleton.getInstance();
            s.menu.add(me);
        }
        catch (NumberFormatException e) {
            Toast toast = Toast.makeText(this, "Неверный формат id", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    public void onClickAddToDatabase(View view) {
        try {
            MenuEntry me = new MenuEntry();
            me.id = Integer.parseInt(editId.getText().toString());
            me.name = editName.getText().toString();
            me.category = editCategory.getText().toString();

            SQLiteDatabase db = dbHelper.getWritableDatabase();
            DatabaseHelper.writeMenuEntry(db, me);
            db.close();
        }
        catch (NumberFormatException e) {
            Toast toast = Toast.makeText(this, "Неверный формат id", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    public void onClickClearDatabase(View view) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        DatabaseHelper.clearMenu(db);
        db.close();
    }

    public void onClickFillDatabase(View view) {
        final int N_NAMES = 10;
        final int N_CATEGORIES = 10;
        ArrayList<MenuEntry> entries = new ArrayList<>();

        for (int i = 0; i < N_CATEGORIES; i++) {
            for (int j = 0; j < N_NAMES; j++) {
                MenuEntry e = new MenuEntry();
                e.id = i * N_CATEGORIES + j;
                e.name = "name" + Integer.toString(j);
                e.category = "category" + Integer.toString(i);
                entries.add(e);
            }
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        DatabaseHelper.writeMenuEntries(db, entries);
        db.close();
    }

    public void onClickFetchMenu(View view) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Singleton.menuFetch(db);
        db.close();
    }
}
