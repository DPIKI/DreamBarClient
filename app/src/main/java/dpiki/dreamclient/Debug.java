package dpiki.dreamclient;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;

import dpiki.dreamclient.Network.BaseNetworkListener;
import dpiki.dreamclient.Network.INetworkServiceListener;
import dpiki.dreamclient.Network.NetworkService;
import dpiki.dreamclient.Network.NetworkServiceMessageReceiver;

public class Debug extends AppCompatActivity{
    Boolean isServiceConnected = false;
    NetworkService networkService;
    EditText editId;
    EditText editName;
    EditText editCategory;
    DatabaseHelper dbHelper;
    NetworkServiceMessageReceiver receiver;

    //
    private GestureDetector gestureDetector;
    //
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);

        //
        //
        gestureDetector = initGestureDetector();

        View view = findViewById(R.id.b_debugConnect);

        view.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                Log.d("debug: ", "On Touch");
                return gestureDetector.onTouchEvent(event);
            }
        });
        Log.d("debug: ", "SetOnTouchListener");
        //
        //

        /*editId = (EditText)findViewById(R.id.e_debugId);
        editName = (EditText)findViewById(R.id.e_debugName);
        editCategory = (EditText)findViewById(R.id.e_debugCategory);*/

        dbHelper = new DatabaseHelper(this);
    }

    //
    private GestureDetector initGestureDetector() {
        return new GestureDetector(new GestureDetector.SimpleOnGestureListener() {

            private SwipeDetector detector = new SwipeDetector();

            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                                   float velocityY) {
                try {
                    Log.d("debug: ", "on Fling");
                    if (detector.isSwipeDown(e1, e2, velocityY)) {
                        return false;
                    } else if (detector.isSwipeUp(e1, e2, velocityY)) {
                        Log.d("debug: ", "Up Swipe");
                        showToast("Up Swipe");
                    }else if (detector.isSwipeLeft(e1, e2, velocityX)) {
                        Log.d("debug: ", "Left Swipe");
                        showToast("Left Swipe");
                    } else if (detector.isSwipeRight(e1, e2, velocityX)) {
                        Log.d("debug: ", "Right Swipe");
                        showToast("Right Swipe");
                    }
                } catch (Exception e) {} //for now, ignore
                return false;
            }

            private void showToast(String phrase){
                Toast.makeText(getApplicationContext(), phrase, Toast.LENGTH_SHORT).show();
                Log.d("debug: ", phrase);
            }
        });
    }
    //

    @Override
    protected void onResume() {
        super.onResume();

        receiver = new NetworkServiceMessageReceiver(new BaseNetworkListener() {
        });
        registerReceiver(receiver,
                new IntentFilter(NetworkService.ACTION_NETWORK_SERVICE));
        Log.d("Debug", "Receiver registered");

        Intent intent = new Intent(this.getApplicationContext(), NetworkService.class);
        bindService(intent, connection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onPause() {
        super.onPause();

        unregisterReceiver(receiver);
        receiver = null;
        Log.d("Debug", "Receiver unregistered");

        unbindService(connection);
    }

 /*   public void onClickAdd(View view) {
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

    public void onClickConnect(View view) {
        if (isServiceConnected) {
            networkService.connect();
        }
        else {
            Toast.makeText(this, "Service is unbound", Toast.LENGTH_SHORT).show();
        }
    }

    public void onClickDisconnect(View view) {
        if (isServiceConnected) {
            networkService.disconnect();
        }
        else {
            Toast.makeText(this, "Service is unbound", Toast.LENGTH_SHORT).show();
        }
    }
*/
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            networkService = ((NetworkService.NetworkServiceBinder)service).getServiceInstance();
            isServiceConnected = true;
            Toast.makeText(Debug.this, "NetworkService.state=" +
                    Integer.toString(networkService.state()), Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            networkService = null;
            isServiceConnected = false;
        }
    };
}
