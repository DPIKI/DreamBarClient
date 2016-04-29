package dpiki.dreamclient.MenuActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.Iterator;

import dpiki.dreamclient.Database.DatabaseHelper;
import dpiki.dreamclient.Database.DatabaseMenuWorker;
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

    private ArrayList<MenuEntry> mFullMenuEntries;
    private ArrayList<MenuEntry> mMenuEntriesByCategory;

    private ArrayList<String> mCategories;
    private String mSelectedCategory;
    private int mIndexSelectedCategory;

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

        mIndexSelectedCategory = 0;
        mSelectedCategory = "Все категории";

        drawerListView.setOnItemClickListener(new DrawerItemClickListener());
        menuNameListView.setOnItemClickListener(new ListMenuClickListener());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        mSelectedCategory = savedInstanceState.getString("selectedCategory");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString("selectedCategory", mSelectedCategory);
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectDrawerItem(position);
        }
    }

    private void selectDrawerItem(int position) {
        mIndexSelectedCategory = position;
        mSelectedCategory = mCategories.get(mIndexSelectedCategory);
        updateMenuEntriesAdapter(MenuActivity.this);

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
                showMenuLayout();
                initMenu(MenuActivity.this);
            } else {
                showProgress();
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
            drawerLayout.setVisibility(View.VISIBLE);
            progressLayout.setVisibility(View.GONE);
            initMenu(MenuActivity.this);
        }

        @Override
        public void onWrongPassword() {
            // TODO : надпись
        }

    };

    private void showProgress(){
        drawerLayout.setVisibility(View.GONE);
        progressLayout.setVisibility(View.VISIBLE);
    }

    private void showMenuLayout(){
        progressLayout.setVisibility(View.GONE);
        drawerLayout.setVisibility(View.VISIBLE);
    }

    private ArrayList<MenuEntry> readFullListMenuFromDatabase(Context context){
        ArrayList<MenuEntry> listMenu = new ArrayList<>();

        DatabaseHelper databaseHelper = new DatabaseHelper(context);
        SQLiteDatabase database = databaseHelper.getReadableDatabase();
        try {
            listMenu = DatabaseMenuWorker.readMenu(database);
        }finally {
            database.close();
        }
        if (listMenu.isEmpty()){
            showProgress();
        }
        return listMenu;
    }

    private ArrayList<String> getCategories(ArrayList<MenuEntry> fullMenuEntries){
        ArrayList<String> categories = new ArrayList<>();

        categories.add("Все категории");

        if (fullMenuEntries.size() != 0){
            Iterator<MenuEntry> iterator = fullMenuEntries.iterator();
            while (iterator.hasNext()){

                MenuEntry menuEntry = iterator.next();
                boolean isExist = false;

                Iterator<String> i = categories.iterator();
                while (i.hasNext() && !isExist){

                    String category = i.next();
                    if (category.equals(menuEntry.category)){
                        isExist = true;
                    }
                }
                if (!isExist){
                    categories.add(menuEntry.category);
                }
            }
        }

        return categories;
    }

    private ArrayList<MenuEntry> getMenuEntriesByCategory(ArrayList<MenuEntry> fullMenuEntries){
        ArrayList<MenuEntry> menuEntriesByCategory = new ArrayList<>();

        if (mSelectedCategory.equals("Все категории")){
            menuEntriesByCategory = fullMenuEntries;
        }
        else {
            Iterator<MenuEntry> i = fullMenuEntries.iterator();
            while (i.hasNext()){
                MenuEntry menuEntry = i.next();
                if (menuEntry.category.equals(mSelectedCategory)){
                    menuEntriesByCategory.add(menuEntry);
                }
            }

        }

        return menuEntriesByCategory;
    }

    private void initMenu(Context context){
        updateMenuEntriesAdapter(context);
        updateCategoriesAdapter(context);

        mIndexSelectedCategory = getIndexSelectedCategory();
        drawerListView.setItemChecked(mIndexSelectedCategory, true);
    }

    private int getIndexSelectedCategory(){
        int selectedIndex = 0;

        if (!mSelectedCategory.equals("Все категории")){
            for (int i = 0; i < mCategories.size() && selectedIndex == 0; i++){
                if (mCategories.get(i).equals(mSelectedCategory)){
                    selectedIndex = i;
                }
            }
        }

        return selectedIndex;
    }

    private void updateMenuEntriesAdapter(Context context){
        mFullMenuEntries = readFullListMenuFromDatabase(context);
        mMenuEntriesByCategory = getMenuEntriesByCategory(mFullMenuEntries);
        MenuListAdapter menuAdapter = new MenuListAdapter(context, mMenuEntriesByCategory);
        menuNameListView.setAdapter(menuAdapter);
    }

    private void updateCategoriesAdapter(Context context){
        mCategories = getCategories(mFullMenuEntries);
        ArrayAdapter<String> categoriesAdapter = new ArrayAdapter<>(context,
                R.layout.activity_menu_drawer_list_item, mCategories);
        drawerListView.setAdapter(categoriesAdapter);
    }

}
