package dpiki.dreamclient.MenuActivity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Iterator;

import dpiki.dreamclient.Database.DatabaseHelper;
import dpiki.dreamclient.Database.DatabaseMenuWorker;
import dpiki.dreamclient.Database.DatabaseOrderWorker;
import dpiki.dreamclient.Network.BaseNetworkListener;
import dpiki.dreamclient.Network.INetworkServiceListener;
import dpiki.dreamclient.Network.NetworkService;
import dpiki.dreamclient.Network.NetworkServiceMessageReceiver;
import dpiki.dreamclient.OrderActivity.OrderEntry;
import dpiki.dreamclient.R;
import dpiki.dreamclient.SettingsActivity.SettingsActivity;

/**
 * Created by prog1 on 26.04.2016.
 */
public class MenuActivity  extends AppCompatActivity {

    public ArrayList<String> categories;
    public int checkedPosition = 0;
    public String hash;
    public ArrayList<MenuEntry> menuEntryArrayList;
    public ArrayList<MenuEntry> menuEntriesByCategory;

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

        Log.d("CheckPos: ", "In onResume" + checkedPosition);
        Intent intent = new Intent(this, NetworkService.class);
        bindService(intent, connection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("CheckPos: ", "In onPause" + checkedPosition);
        unregisterReceiver(receiver);
        unbindService(connection);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        progressLayout = (RelativeLayout) findViewById(R.id.menu_progress_bar_layout);
        drawerListView = (ListView) findViewById(R.id.lv_left_drawer);
        menuNameListView = (ListView) findViewById(R.id.lv_menu_name);
        isServiceConnected = false;

         Log.d("CheckPos: ", "In onCreate" + checkedPosition);
        drawerListView.setOnItemClickListener(new DrawerItemClickListener());

        menuNameListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                DatabaseHelper databaseHelper = new DatabaseHelper(MenuActivity.this);
                SQLiteDatabase database = databaseHelper.getWritableDatabase();
                MenuEntry menuEntry = menuEntryArrayList.get(position);
                OrderEntry orderEntry = new OrderEntry(menuEntry.id, menuEntry.name, 1, 17,
                        "");
                DatabaseOrderWorker.writeOrderEntry(database, orderEntry);
                database.close();
            }
        });
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

       Log.d("CheckPos: ", "In onRestoreInstanceState do saved.getInt()" + checkedPosition);
       String savedHash = savedInstanceState.getString(getString(R.string.s_pref_key_hash));
       hash = savedHash;
       checkedPosition = savedInstanceState.getInt("currentIndex");
       Log.d("CheckPos: ", "In onRestoreInstanceState posle saved.getInt()" + checkedPosition);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String curHash = preferences.getString(getString(R.string.s_pref_key_hash), "");
        outState.putString(getString(R.string.s_pref_key_hash), curHash);
        outState.putInt("currentIndex", checkedPosition);
        Log.d("CheckPos: ", "In onSaveInstanceState" + checkedPosition);
    }

    //  Слушатель для элементов списка в выдвижной панели
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    private void selectItem(int position) {
        MenuListAdapter menuListAdapter;
        checkedPosition = position;
        if (position!=0) {
            String category = categories.get(position);
            menuEntriesByCategory = getNameMenuByCategory(menuEntryArrayList, category);
            menuListAdapter = new MenuListAdapter(MenuActivity.this,
                    menuEntriesByCategory);
        }else {
            menuEntriesByCategory = menuEntryArrayList;
            menuListAdapter = new MenuListAdapter(MenuActivity.this,
                    menuEntriesByCategory);
        }
        menuNameListView.setAdapter(menuListAdapter);
        drawerLayout.closeDrawers();
    }

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            networkService = ((NetworkService.NetworkServiceBinder)service).getServiceInstance();
            isServiceConnected = true;
            if (networkService.state() == NetworkService.STATE_READY  ||
                networkService.state() == NetworkService.STATE_READY_WAIT) {
                SharedPreferences preferences =
                        PreferenceManager.getDefaultSharedPreferences(MenuActivity.this);
                String currentHash = preferences.getString(getString(R.string.s_pref_key_hash), "");
                if (!currentHash.equals(hash)) {
                    checkedPosition = 0;
                }

                Log.d("CheckPos: ", "In onServiceConnected" + checkedPosition);
                drawerLayout.setVisibility(View.VISIBLE);
                progressLayout.setVisibility(View.GONE);
                // TODO : инициализация меню
                DatabaseHelper databaseHelper = new DatabaseHelper(MenuActivity.this);
                SQLiteDatabase db = databaseHelper.getReadableDatabase();

                try {
                    menuEntryArrayList = DatabaseMenuWorker.readMenu(db);
                } finally {
                    db.close();
                }
                MenuListAdapter menuListAdapter = new MenuListAdapter(MenuActivity.this,
                        menuEntryArrayList);
                menuNameListView.setAdapter(menuListAdapter);

                categories = getCategory(menuEntryArrayList);
                drawerListView.setAdapter(new ArrayAdapter<String>(MenuActivity.this,
                        R.layout.activity_menu_drawer_list_item, categories));
            }
            else {
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
            // TODO : progress bar
            drawerLayout.setVisibility(View.GONE);
            progressLayout.setVisibility(View.VISIBLE);
        }

        @Override
        public void onReady() {
            // TODO : update data
            DatabaseHelper dbHelper = new DatabaseHelper(MenuActivity.this);
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            try {
                menuEntryArrayList = DatabaseMenuWorker.readMenu(db);
                MenuListAdapter menuListAdapter = new MenuListAdapter(MenuActivity.this,
                        menuEntryArrayList);
                menuNameListView.setAdapter(menuListAdapter);

                categories = getCategory(menuEntryArrayList);
                drawerListView.setAdapter(new ArrayAdapter<String>(MenuActivity.this,
                    R.layout.activity_menu_drawer_list_item, categories));
                Toast.makeText(MenuActivity.this, "Обновилось меню", Toast.LENGTH_LONG).show();
            } finally {
                db.close();
            }
            drawerLayout.setVisibility(View.VISIBLE);
            progressLayout.setVisibility(View.GONE);
        }

        @Override
        public void onWrongPassword() {
            // TODO : надпись
        }

    };

    private ArrayList<MenuEntry> getNameMenuByCategory(ArrayList<MenuEntry> menuEntries,
                                                       String category){
        ArrayList<MenuEntry> listMenu  = new ArrayList<>();

        for(int i = 0; i < menuEntries.size(); i++) {
            MenuEntry menuEntry = menuEntries.get(i);
            if (menuEntry.category.equals(category.toString())){
                listMenu.add(menuEntry);
            }
        }

        return listMenu;
    }

    private ArrayList<String> getCategory(ArrayList<MenuEntry> menuEntries){
        ArrayList<String> categories = new ArrayList<>();
        boolean isExist = false;

        categories.add("Все категории");

        Iterator<MenuEntry> i = menuEntries.iterator();
        while (i.hasNext()){
            String category = i.next().category;
            isExist = false;
            for (int k = 0; k < categories.size() && !isExist; k++){
                if (category.equals(categories.get(k))){
                    isExist = true;
                }
            }
            if (!isExist){
                categories.add(category);
            }
        }
        return categories;
    }
}
