package dpiki.dreamclient.MenuActivity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import java.util.ArrayList;

import dpiki.dreamclient.Network.BaseNetworkListener;
import dpiki.dreamclient.Network.INetworkServiceListener;
import dpiki.dreamclient.Network.NetworkService;
import dpiki.dreamclient.Network.NetworkServiceMessageReceiver;
import dpiki.dreamclient.R;
import dpiki.dreamclient.SettingsActivity.SettingsActivity;

/**
 * Created by prog1 on 26.04.2016.
 */
public class MenuActivity  extends AppCompatActivity {

    public ArrayList<MenuEntry> fullMenuEntries;
    public ArrayList<MenuEntry> menuEntriesByCategory;

    public ArrayList<String> categories;
    public String selectedCategory;
    public int indexSelectedCategory;

    public RelativeLayout progressLayout;
    public DrawerLayout drawerLayout;
    public ListView drawerListView;
    public ListView menuNameListView;

    NetworkServiceMessageReceiver receiver;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerListView = (ListView) findViewById(R.id.lv_left_drawer);
        menuNameListView = (ListView) findViewById(R.id.lv_menu_name);
        progressLayout = (RelativeLayout) findViewById(R.id.menu_progress_bar_layout);
        isServiceConnected = false;

        indexSelectedCategory = 0;
        selectedCategory = "";

        drawerListView.setOnItemClickListener(new DrawerItemClickListener());
        menuNameListView.setOnItemClickListener(new ListMenuClickListener());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        selectedCategory = savedInstanceState.getString("selectedCategory");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString("selectedCategory", selectedCategory);
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    private void selectItem(int position) {

        drawerLayout.closeDrawers();
    }

    private class ListMenuClickListener implements ListView.OnItemClickListener{
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
    }

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            networkService = ((NetworkService.NetworkServiceBinder) service).getServiceInstance();
            isServiceConnected = true;
            if (networkService.state() == NetworkService.STATE_READY ||
                    networkService.state() == NetworkService.STATE_READY_WAIT) {
                drawerLayout.setVisibility(View.VISIBLE);
                progressLayout.setVisibility(View.GONE);
                // TODO : инициализация меню

            } else {
                drawerLayout.setVisibility(View.GONE);
                progressLayout.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            networkService = null;
            isServiceConnected = false;
        }
    };

    private INetworkServiceListener listener = new BaseNetworkListener() {

        @Override
        public void onConnecting() {
            drawerLayout.setVisibility(View.GONE);
            progressLayout.setVisibility(View.VISIBLE);
        }

        @Override
        public void onReady() {
            // TODO : update data
           drawerLayout.setVisibility(View.VISIBLE);
           progressLayout.setVisibility(View.GONE);
        }

        @Override
        public void onWrongPassword() {
            // TODO : надпись
        }

    };

}
