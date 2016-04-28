package dpiki.dreamclient.OrderActivity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.sqlite.SQLiteDatabase;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

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
            // TODO : progress bar
        }

        @Override
        public void onReady() {
            // TODO : update data
        }

        @Override
        public void onWrongPassword() {
            // TODO : надпись
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);

        ListView listView = (ListView) findViewById(R.id.lv_orders);
        View headerView = getLayoutInflater().inflate(
                R.layout.activity_order_header, null);
        listView.addHeaderView(headerView);

        DatabaseHelper databaseHelper = new DatabaseHelper(this);
        SQLiteDatabase database = databaseHelper.getReadableDatabase();
        try {
            orderEntries = DatabaseOrderWorker.readOrder(database);
        }finally {
            database.close();
        }

        OrderListAdapter orderListAdapter = new OrderListAdapter(this, orderEntries);

        listView.setAdapter(orderListAdapter);

        isServiceConnected = false;
    }

    @Override
    protected void onResume(){
        super.onResume();

        receiver = new NetworkServiceMessageReceiver(listener);
        registerReceiver(receiver, new IntentFilter(NetworkService.ACTION_NETWORK_SERVICE));

        Intent intent = new Intent(this, NetworkService.class);
        bindService(intent, connection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);

        unbindService(connection);
    }

    public void onClickSendOrder(View view){
        Intent intent = new Intent(this, MenuActivity.class);
        startActivity(intent);
    }

    public void  onClick(View view){
        /*final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.activity_order_dialog);
        dialog.setTitle("Стол №3");
        dialog.show();*/

        if (isServiceConnected) {
            networkService.sendOrder();
        }
    }

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            networkService = ((NetworkService.NetworkServiceBinder) service).getServiceInstance();
            isServiceConnected = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            networkService = null;
            isServiceConnected = false;
        }
    };

};
