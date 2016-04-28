package dpiki.dreamclient.OrderActivity;

import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.sqlite.SQLiteDatabase;
import android.os.IBinder;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import dpiki.dreamclient.Database.DatabaseHelper;
import dpiki.dreamclient.Database.DatabaseOrderWorker;
import dpiki.dreamclient.MenuActivity.MenuActivity;
import dpiki.dreamclient.Network.BaseNetworkListener;
import dpiki.dreamclient.Network.INetworkServiceListener;
import dpiki.dreamclient.Network.NetworkService;
import dpiki.dreamclient.Network.NetworkServiceMessageReceiver;
import dpiki.dreamclient.R;
import dpiki.dreamclient.SettingsActivity.SettingsActivity;

public class OrderActivity extends AppCompatActivity {

    NetworkService networkService;
    Boolean isServiceConnected;
    ListView listView;
    RelativeLayout orderLayout;
    RelativeLayout progressBarLayout;
    TextView textView;

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_action_bar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem){
        int id = menuItem.getItemId();

        switch (id){
            case R.id.back:
                startActivity(new Intent(getApplicationContext(),SettingsActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(menuItem);
        }
    }

    ArrayList<OrderEntry> orderEntries = new ArrayList<>();

    NetworkServiceMessageReceiver receiver;
    private INetworkServiceListener listener = new BaseNetworkListener() {
        @Override
        public void onConnecting() {
            textView.setText("Connecting...");
            viewProgress();
            Log.d("OrderActivity", "onConnecting");
        }

        @Override
        public void onReady() {
            updateAdapter();
            viewOrder();
            Log.d("OrderActivity", "onReady");
        }

        @Override
        public void onWrongPassword() {
            Log.d("OrderActivity", "onWrongPassword");
        }

        @Override
        public void onOrderMade() {
            DatabaseHelper helper = new DatabaseHelper(OrderActivity.this);
            SQLiteDatabase db = helper.getWritableDatabase();
            try {
                DatabaseOrderWorker.clearOrder(db);
            }
            finally {
                db.close();
            }

            updateAdapter();
            viewOrder();

            Toast.makeText(OrderActivity.this, "Заказ отправлен", Toast.LENGTH_SHORT).show();
            Log.d("OrderActivity", "onOrderMade");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);

        orderLayout = (RelativeLayout) findViewById(R.id.ov_order_layout);
        progressBarLayout = (RelativeLayout) findViewById(R.id.ov_pb_layout);
        textView = (TextView) findViewById(R.id.ov_pb_text_view);
        listView = (ListView) findViewById(R.id.lv_orders);

        final View headerView = getLayoutInflater().inflate(
            R.layout.activity_order_header, null);
        listView.addHeaderView(headerView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0){
                    startActivity(new Intent(OrderActivity.this, MenuActivity.class));
                }
                else {
                    DatabaseHelper helper = new DatabaseHelper(OrderActivity.this);
                    SQLiteDatabase db = helper.getWritableDatabase();
                    OrderEntry entry = orderEntries.get(position - 1);
                    try {
                        DatabaseOrderWorker.updateCount(db, entry.rowId, entry.count + 1);
                    }
                    finally {
                        db.close();
                    }
                    entry.count++;
                    OrderListAdapter adapter = new OrderListAdapter(OrderActivity.this, orderEntries);
                    listView.setAdapter(adapter);
                }
            }
        });

        isServiceConnected = false;
        Log.d("OrderActivity", "onCreate");
    }

    @Override
    protected void onResume(){
        super.onResume();
        receiver = new NetworkServiceMessageReceiver(listener);
        registerReceiver(receiver, new IntentFilter(NetworkService.ACTION_NETWORK_SERVICE));
        Intent intent = new Intent(this, NetworkService.class);
        bindService(intent, connection, BIND_AUTO_CREATE);
        Log.d("OrderActivity", "onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
        unbindService(connection);
        Log.d("OrderActivity", "onPause");
    }

    public void onClickSendOrder(View view){
        if (isServiceConnected) {
            networkService.sendOrder();
            textView.setText("Отправляем заказ...");
            viewProgress();
        }
    }

    public void  onClickNewOrder(View view){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Новый заказ");
        alertDialog.setMessage("Вы дейтсвительно хотите создать новый заказ? (Прримечание: Текущий заказ будет удален)");
        alertDialog.setPositiveButton("Ок", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
                DatabaseHelper databaseHelper = new DatabaseHelper(OrderActivity.this);
                SQLiteDatabase database = databaseHelper.getWritableDatabase();
                try {
                    DatabaseOrderWorker.clearOrder(database);
                }finally {
                    database.close();
                }
            }
        });

        alertDialog.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
            }
        });
        alertDialog.show();
    }

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            networkService = ((NetworkService.NetworkServiceBinder) service).getServiceInstance();
            isServiceConnected = true;

            if (networkService.state() == NetworkService.STATE_READY ||
                networkService.state() == NetworkService.STATE_READY_WAIT) {
                updateAdapter();
                viewOrder();
            }
            else {
                textView.setText("Connecting...");
                viewProgress();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            networkService = null;
            isServiceConnected = false;
            textView.setText("Connecting...");
            viewProgress();
        }
    };

    void updateAdapter() {
        DatabaseHelper databaseHelper = new DatabaseHelper(OrderActivity.this);
        SQLiteDatabase database = databaseHelper.getReadableDatabase();
        try {
            orderEntries = DatabaseOrderWorker.readOrder(database);
        } finally {
            database.close();
        }

        OrderListAdapter orderListAdapter = new OrderListAdapter(OrderActivity.this, orderEntries);
        listView.setAdapter(orderListAdapter);
    }

    void viewProgress() {
        orderLayout.setVisibility(View.GONE);
        progressBarLayout.setVisibility(View.VISIBLE);
    }

    void viewOrder() {
        orderLayout.setVisibility(View.VISIBLE);
        progressBarLayout.setVisibility(View.GONE);
    }
};
