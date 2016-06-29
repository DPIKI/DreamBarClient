package dpiki.dreamclient.MenuActivity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeSet;

import dpiki.dreamclient.Database.DatabaseHelper;
import dpiki.dreamclient.Database.DatabaseMenuWorker;
import dpiki.dreamclient.Database.DatabaseOrderWorker;
import dpiki.dreamclient.EditDialog;
import dpiki.dreamclient.IEditDialogCallback;
import dpiki.dreamclient.ImageDownloadManager;
import dpiki.dreamclient.Network.BaseNetworkListener;
import dpiki.dreamclient.Network.INetworkServiceListener;
import dpiki.dreamclient.Network.NetworkService;
import dpiki.dreamclient.OrderActivity.OrderEntry;
import dpiki.dreamclient.R;
import dpiki.dreamclient.SettingsActivity.SettingsActivity;

/**
 * Created by prog1 on 26.04.2016.
 */
public class MenuActivity extends AppCompatActivity implements IEditDialogCallback {

    private ArrayList<MenuEntry> mFullMenuEntries;
    private ArrayList<MenuEntry> mMenuEntriesByCategory;

    private ArrayList<String> mCategories;
    private String mSelectedCategory;
    private int mIndexSelectedCategory;

    public RelativeLayout menuLayout;
    public RelativeLayout progressLayout;
    public RelativeLayout wrongPasswordLayout;
    public RelativeLayout disconnectedLayout;
    public ListView menuNameListView;
    public TextView tvTitle;
    public Spinner spinner;

    public EditDialog dialog;

    public ImageDownloadManager imageDownloadManager;

    NetworkService networkService;
    Boolean isServiceConnected;
    Switch sw;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        menuNameListView = (ListView) findViewById(R.id.lv_menu_name);
        menuLayout = (RelativeLayout) findViewById(R.id.menu_layout);
        progressLayout = (RelativeLayout) findViewById(R.id.menu_progress_bar_layout);
        wrongPasswordLayout = (RelativeLayout) findViewById(R.id.mv_wrong_password_layout);
        disconnectedLayout = (RelativeLayout) findViewById(R.id.mv_disconnected_layout);

        tvTitle = (TextView) findViewById(R.id.tv_toolbar_title);
        tvTitle.setText("Меню");
        tvTitle.setVisibility(View.GONE);
        spinner = (Spinner) findViewById(R.id.spinner);
        spinner.setVisibility(View.VISIBLE);

        isServiceConnected = false;

        mIndexSelectedCategory = 0;
        mSelectedCategory = "Все категории";

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mIndexSelectedCategory = position;
                mSelectedCategory = mCategories.get(mIndexSelectedCategory);
                updateMenuEntriesAdapter();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        menuNameListView.setOnItemClickListener(new ListMenuClickListener());
        menuNameListView.setOnItemLongClickListener(new ListMenuLongClickListener());

        imageDownloadManager = new ImageDownloadManager(getMainLooper());

        initToolbar();
        initSwitch();

        dialog = new EditDialog(this, this, imageDownloadManager);
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_action_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    private void initSwitch() {
        sw = (Switch) findViewById(R.id.switch_settings);
        sw.setVisibility(View.VISIBLE);
        SharedPreferences pref =
                            PreferenceManager.getDefaultSharedPreferences(this);
        sw.setChecked(pref.getBoolean(getString(R.string.s_pref_key_running), false));

            sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    SharedPreferences pref =
                            PreferenceManager.getDefaultSharedPreferences(MenuActivity.this);
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putBoolean(getString(R.string.s_pref_key_running), isChecked);
                    editor.apply();
                }
            });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_action_bar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem){
        int id = menuItem.getItemId();
        switch (id){
            case R.id.settings:
                startActivity(new Intent(getApplicationContext(),SettingsActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(menuItem);
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        Intent intent = new Intent(this, NetworkService.class);
        bindService(intent, connection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        networkService.unsubscribe(listener);
        imageDownloadManager.resetNetworkService();
        unbindService(connection);
    }

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            networkService = ((NetworkService.NetworkServiceBinder) service).getServiceInstance();
            isServiceConnected = true;
            networkService.subscribe(listener);
            imageDownloadManager.setNetworkService(networkService);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            networkService = null;
            isServiceConnected = false;
        }
    };

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

    @Override
    public void onOkButtonClick(OrderEntry entry) {
        orderToDatabase(entry);
    }

    public void onClickTurnServiceOn(View view) {
        sw.setChecked(true);
    }

    public void onClickChangePassword(View view) {
        startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
    }

    private class ListMenuLongClickListener implements ListView.OnItemLongClickListener{
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            OrderEntry entry = makeOrderFromPosition(position);
            dialog.showDialog(entry);
            return true;
        }
    }

    private class ListMenuClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            OrderEntry entry = makeOrderFromPosition(position);
            orderToDatabase(entry);
        }
    }

    private OrderEntry makeOrderFromPosition(int position) {
        MenuEntry menuEntry = mMenuEntriesByCategory.get(position);
        return new OrderEntry(menuEntry.id, menuEntry.name, 1, 0, "");
    }

    private INetworkServiceListener listener = new BaseNetworkListener() {

        @Override
        public void onDisconnected() {
            dialog.hideDialog();
            showDisconnected();
        }

        @Override
        public void onConnecting() {
            dialog.hideDialog();
            showProgress();
        }

        @Override
        public void onWrongPassword() {
            dialog.hideDialog();
            showWrongPassword();
        }

        @Override
        public void onReady() {
            showMenuLayout();
            initMenu();
        }

        @Override
        public void onImageLoaded(int id, byte[] image) {
            imageDownloadManager.publishImage(id, image);
        }
    };

    private void showProgress() {
        progressLayout.setVisibility(View.VISIBLE);
        tvTitle.setVisibility(View.VISIBLE);
        menuLayout.setVisibility(View.GONE);
        wrongPasswordLayout.setVisibility(View.GONE);
        disconnectedLayout.setVisibility(View.GONE);
        spinner.setVisibility(View.GONE);
    }

    private void showMenuLayout() {
        menuLayout.setVisibility(View.VISIBLE);
        spinner.setVisibility(View.VISIBLE);
        tvTitle.setVisibility(View.GONE);
        progressLayout.setVisibility(View.GONE);
        wrongPasswordLayout.setVisibility(View.GONE);
        disconnectedLayout.setVisibility(View.GONE);
    }

    private void showWrongPassword() {
        wrongPasswordLayout.setVisibility(View.VISIBLE);
        tvTitle.setVisibility(View.VISIBLE);
        progressLayout.setVisibility(View.GONE);
        menuLayout.setVisibility(View.GONE);
        disconnectedLayout.setVisibility(View.GONE);
        spinner.setVisibility(View.GONE);
    }

    private void showDisconnected() {
        disconnectedLayout.setVisibility(View.VISIBLE);
        tvTitle.setVisibility(View.VISIBLE);
        progressLayout.setVisibility(View.GONE);
        wrongPasswordLayout.setVisibility(View.GONE);
        menuLayout.setVisibility(View.GONE);
        spinner.setVisibility(View.GONE);
    }

    private ArrayList<MenuEntry> readFullListMenuFromDatabase(){
        ArrayList<MenuEntry> listMenu = new ArrayList<>();

        DatabaseHelper databaseHelper = new DatabaseHelper(this);
        SQLiteDatabase database = databaseHelper.getReadableDatabase();
        try {
            listMenu = DatabaseMenuWorker.readMenu(database);
        }finally {
            database.close();
        }
        return listMenu;
    }

    private ArrayList<String> getCategories(ArrayList<MenuEntry> fullMenuEntries){
        TreeSet<String> categories = new TreeSet<>();

        categories.add("Все категории");

        for (MenuEntry i : fullMenuEntries) {
            categories.add(i.category);
        }

        return new ArrayList<>(categories);
    }

    private ArrayList<MenuEntry> getMenuEntriesByCategory(ArrayList<MenuEntry> fullMenuEntries) {
        ArrayList<MenuEntry> menuEntriesByCategory = new ArrayList<>();

        if (mSelectedCategory.equals("Все категории")){
            menuEntriesByCategory = fullMenuEntries;
        } else {
            Iterator<MenuEntry> i = fullMenuEntries.iterator();
            while (i.hasNext()) {
                MenuEntry menuEntry = i.next();
                if (menuEntry.category.equals(mSelectedCategory)) {
                    menuEntriesByCategory.add(menuEntry);
                }
            }

            if (menuEntriesByCategory.isEmpty()) {
                menuEntriesByCategory = mFullMenuEntries;
            }
        }

        return menuEntriesByCategory;
    }

    private void initMenu() {
        updateMenuEntriesAdapter();
        updateCategoriesAdapter();

        mIndexSelectedCategory = getIndexSelectedCategory();
        spinner.setSelection(mIndexSelectedCategory, true);
    }

    private int getIndexSelectedCategory() {
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

    private void orderToDatabase(OrderEntry orderEntry) {
        DatabaseHelper databaseHelper = new DatabaseHelper(MenuActivity.this);
        SQLiteDatabase database = databaseHelper.getWritableDatabase();
        try {
            DatabaseOrderWorker.writeOrderEntry(database, orderEntry);
        }finally {
            database.close();
        }
    }

    private void updateMenuEntriesAdapter() {
        mFullMenuEntries = readFullListMenuFromDatabase();
        mMenuEntriesByCategory = getMenuEntriesByCategory(mFullMenuEntries);
        MenuListAdapter menuAdapter = new MenuListAdapter(this, mMenuEntriesByCategory,
                imageDownloadManager);
        menuNameListView.setAdapter(menuAdapter);
        updateSelectedCategory();
    }

    private void updateCategoriesAdapter() {
        mCategories = getCategories(mFullMenuEntries);
        ArrayAdapter<String> categoriesAdapter = new ArrayAdapter<>(this,
                R.layout.activity_menu_drawer_list_item, mCategories);
        spinner.setAdapter(categoriesAdapter);
        updateSelectedCategory();
    }

    public void updateSelectedCategory() {
        //На случай если категория которая была активна была удалена из меню
        String selectedCategory = "Все категории";
        Iterator<String> i = getCategories(mFullMenuEntries).iterator();
        while (i.hasNext() && selectedCategory.equals("Все категории")){
            String category = i.next();
            if (category.equals(mSelectedCategory)){
                selectedCategory = mSelectedCategory;
            }
        }
        mSelectedCategory = selectedCategory;
    }
}
