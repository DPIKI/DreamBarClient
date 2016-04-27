package dpiki.dreamclient.OrderActivity;

import android.app.Dialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import java.util.ArrayList;

import dpiki.dreamclient.MenuActivity.MenuActivity;
import dpiki.dreamclient.Network.BaseNetworkListener;
import dpiki.dreamclient.Network.INetworkServiceListener;
import dpiki.dreamclient.Network.NetworkService;
import dpiki.dreamclient.Network.NetworkServiceMessageReceiver;
import dpiki.dreamclient.R;
import dpiki.dreamclient.SettingsActivity.SettingsActivity;

public class OrderActivity extends AppCompatActivity {

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
        fill();
        View headerView = getLayoutInflater().inflate(
                R.layout.activity_order_header, null);
        listView.addHeaderView(headerView);

        OrderListAdapter orderListAdapter = new OrderListAdapter(this, orderEntries);

        listView.setAdapter(orderListAdapter);

    }

    @Override
    protected void onResume(){
        super.onResume();

        receiver = new NetworkServiceMessageReceiver(listener);
        registerReceiver(receiver, new IntentFilter(NetworkService.ACTION_NETWORK_SERVICE));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    public void onClickSendOrder(View view){
        Intent intent = new Intent(this, MenuActivity.class);
        startActivity(intent);
    }
    public void  onClick(View view){
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.activity_order_dialog);
        dialog.setTitle("Стол №3");
        dialog.show();
    }

    public void fill(){
        for (int i = 1; i<20; i++){
            orderEntries.add(new OrderEntry(i,"элемент меню " + (i+7),i+3,i+12,
                    "заметки для барменаSASAsdfsdcksmdkcmsdimciksdmicmsidmcismiiimdcd"));
        }
    }
}
