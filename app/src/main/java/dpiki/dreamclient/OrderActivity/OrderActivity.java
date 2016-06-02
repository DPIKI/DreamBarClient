package dpiki.dreamclient.OrderActivity;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import dpiki.dreamclient.Database.DatabaseHelper;
import dpiki.dreamclient.Database.DatabaseOrderWorker;
import dpiki.dreamclient.EditDialog;
import dpiki.dreamclient.IEditDialogCallback;
import dpiki.dreamclient.MenuActivity.MenuActivity;
import dpiki.dreamclient.Network.BaseNetworkListener;
import dpiki.dreamclient.Network.INetworkServiceListener;
import dpiki.dreamclient.Network.NetworkService;
import dpiki.dreamclient.R;
import dpiki.dreamclient.SettingsActivity.SettingsActivity;
import dpiki.dreamclient.Swipe.SwipeDismissListViewTouchListener;

public class OrderActivity extends AppCompatActivity
        implements IEditDialogCallback {

    NetworkService networkService;
    Boolean isServiceConnected;
    ArrayList<OrderEntry> orderEntries = new ArrayList<>();
    OrderListAdapter orderListAdapter;

    ListView listView;
    TextView textView;
    RelativeLayout orderLayout;
    RelativeLayout progressBarLayout;
    RelativeLayout wrongPasswordLayout;
    RelativeLayout disconnectedLayout;

    EditDialog dialog;

    Dialog selectTableDialog;
    Button btnStDialogOk;
    Button btnStDialogCancel;
    EditText editStDialogTable;

    Switch sw;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);

        orderLayout = (RelativeLayout) findViewById(R.id.ov_order_layout);
        progressBarLayout = (RelativeLayout) findViewById(R.id.ov_pb_layout);
        disconnectedLayout = (RelativeLayout) findViewById(R.id.ov_disconnected_layout);
        wrongPasswordLayout = (RelativeLayout) findViewById(R.id.ov_wrong_password_layout);
        textView = (TextView) findViewById(R.id.ov_pb_text_view);
        listView = (ListView) findViewById(R.id.lv_orders);
        isServiceConnected = false;

        dialog = new EditDialog(this, new Dialog(this), this);

        initListView();
        initSelectTableDialog();
        initToolbar();
        initSwitch();

        Log.d("OrderActivity", "onCreate");
    }

    @Override
    protected void onResume() {
        super.onResume();

        Intent intent = new Intent(this, NetworkService.class);
        bindService(intent, connection, BIND_AUTO_CREATE);

        Log.d("OrderActivity", "onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();

        networkService.unsubscribe(listener);
        unbindService(connection);

        Log.d("OrderActivity", "onPause");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_action_bar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        int id = menuItem.getItemId();

        switch (id) {
            case R.id.settings:
                startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(menuItem);
        }
    }

    public void onClickSendOrder(View view) {
        if (isServiceConnected && !orderEntries.isEmpty()) {
            selectTableDialog.show();
        }
    }

    public void onClickNewOrder(View view) {

        if (!orderEntries.isEmpty()) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            alertDialog.setTitle("Новый заказ");
            alertDialog.setMessage("Вы дейтсвительно хотите создать новый заказ? (Примечание: текущий заказ будет удален)");
            alertDialog.setPositiveButton("Ок", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int arg1) {
                    DatabaseHelper databaseHelper = new DatabaseHelper(OrderActivity.this);
                    SQLiteDatabase database = databaseHelper.getWritableDatabase();
                    try {
                        DatabaseOrderWorker.clearOrder(database);
                    } finally {
                        database.close();
                    }
                    updateAdapter();
                }
            });

            alertDialog.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int arg1) {
                }
            });
            alertDialog.show();
        }
    }

    public void onClickTurnServiceOn(View view) {
        sw.setChecked(true);
    }

    public void onClickChangePassword(View view) {
        startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
    }

    void updateAdapter() {
        DatabaseHelper databaseHelper = new DatabaseHelper(OrderActivity.this);
        SQLiteDatabase database = databaseHelper.getReadableDatabase();
        try {
            orderEntries = DatabaseOrderWorker.readOrder(database);
        } finally {
            database.close();
        }

        orderListAdapter = new OrderListAdapter(OrderActivity.this, orderEntries);
        listView.setAdapter(orderListAdapter);
    }

    void viewProgress(String title) {
        textView.setText(title);

        orderLayout.setVisibility(View.GONE);
        wrongPasswordLayout.setVisibility(View.GONE);
        disconnectedLayout.setVisibility(View.GONE);
        progressBarLayout.setVisibility(View.VISIBLE);
    }

    void viewOrder() {
        orderLayout.setVisibility(View.VISIBLE);
        wrongPasswordLayout.setVisibility(View.GONE);
        disconnectedLayout.setVisibility(View.GONE);
        progressBarLayout.setVisibility(View.GONE);
    }

    void viewWrongPassword() {
        orderLayout.setVisibility(View.GONE);
        wrongPasswordLayout.setVisibility(View.VISIBLE);
        disconnectedLayout.setVisibility(View.GONE);
        progressBarLayout.setVisibility(View.GONE);
    }

    void viewDisconnected() {
        orderLayout.setVisibility(View.GONE);
        wrongPasswordLayout.setVisibility(View.GONE);
        disconnectedLayout.setVisibility(View.VISIBLE);
        progressBarLayout.setVisibility(View.GONE);
    }

    void initSelectTableDialog() {
        selectTableDialog = new Dialog(this);
        selectTableDialog.setTitle("Выберите стол");
        selectTableDialog.setContentView(R.layout.activity_order_select_table_dialog);
        btnStDialogCancel = (Button) selectTableDialog.findViewById(R.id.ct_dialog_btn_cancel);
        btnStDialogOk = (Button) selectTableDialog.findViewById(R.id.ct_dialog_btn_ok);
        editStDialogTable = (EditText) selectTableDialog.findViewById(R.id.ct_dialog_edit_table);

        btnStDialogOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String strTableNum = editStDialogTable.getText().toString();
                int tableNum = Integer.parseInt(strTableNum);

                DatabaseHelper helper = new DatabaseHelper(OrderActivity.this);
                SQLiteDatabase db = helper.getWritableDatabase();
                try {
                    DatabaseOrderWorker.updateTable(db, tableNum);
                }
                finally {
                    db.close();
                }

                networkService.sendOrder();
                viewProgress("Отправляем заказ...");
                selectTableDialog.dismiss();
            }
        });

        btnStDialogCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectTableDialog.dismiss();
            }
        });
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
                            PreferenceManager.getDefaultSharedPreferences(OrderActivity.this);
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putBoolean(getString(R.string.s_pref_key_running), isChecked);
                    editor.apply();
                }
            });
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_action_bar);
        TextView textView = (TextView) findViewById(R.id.tv_toolbar_title);
        textView.setText("DreamBar");
        setSupportActionBar(toolbar);
    }

    void initSwipeListView() {
        SwipeDismissListViewTouchListener touchListener =
                new SwipeDismissListViewTouchListener(
                        listView,
                        new SwipeDismissListViewTouchListener.DismissCallbacks() {
                            @Override
                            public boolean canDismiss(int position) {
                                if(position == 0) {
                                    return false;
                                }
                                else {
                                    return true;
                                }
                            }

                            @Override
                            public void onDismiss(ListView listView, int[] reverseSortedPositions) {
                                for (int position : reverseSortedPositions) {
                                    DatabaseHelper helper = new DatabaseHelper(OrderActivity.this);
                                    SQLiteDatabase db = helper.getWritableDatabase();
                                    try {
                                        DatabaseOrderWorker.deleteOrder(db,
                                                ((OrderEntry)orderListAdapter
                                                        .getItem(position - 1)).rowId);
                                        orderEntries = DatabaseOrderWorker.readOrder(db);
                                    } finally {
                                        db.close();
                                    }
                                }
                                orderListAdapter.orderEntries = orderEntries;
                                orderListAdapter.notifyDataSetChanged();
                            }
                        });
        listView.setOnTouchListener(touchListener);
        listView.setOnScrollListener(touchListener.makeScrollListener());
    }

    void initListView() {
        final View headerView = getLayoutInflater().inflate(
                R.layout.activity_order_header, null);
        listView.addHeaderView(headerView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    startActivity(new Intent(OrderActivity.this, MenuActivity.class));
                } else {
                    dialog.showDialog(orderEntries.get(position - 1));
                }
            }
        });
        initSwipeListView();
    }

    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            networkService = ((NetworkService.NetworkServiceBinder) service).getServiceInstance();
            isServiceConnected = true;
            networkService.subscribe(listener);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            networkService = null;
            isServiceConnected = false;
            viewProgress("Connecting...");
        }
    };

    private INetworkServiceListener listener = new BaseNetworkListener() {

        @Override
        public void onConnecting() {
            viewProgress("Подключаемся...");
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
            viewWrongPassword();
            Log.d("OrderActivity", "onWrongPassword");
        }

        @Override
        public void onOrderMade() {
            DatabaseHelper helper = new DatabaseHelper(OrderActivity.this);
            SQLiteDatabase db = helper.getWritableDatabase();
            try {
                DatabaseOrderWorker.clearOrder(db);
            } finally {
                db.close();
            }

            updateAdapter();
            viewOrder();

            Toast.makeText(OrderActivity.this, "Заказ отправлен", Toast.LENGTH_SHORT).show();
            Log.d("OrderActivity", "onOrderMade");
        }

        @Override
        public void onDisconnected() {
            viewDisconnected();
            Log.d("OrderActivity", "onDisconnected");
        }
    };

    @Override
    public void onOkButtonClick(OrderEntry entry) {
        OrderListAdapter adapter = new OrderListAdapter(OrderActivity.this,
                        orderEntries);
        listView.setAdapter(adapter);

        DatabaseHelper helper = new DatabaseHelper(OrderActivity.this);
        SQLiteDatabase db = helper.getWritableDatabase();
        try {
            DatabaseOrderWorker.updateOrderNoteAndCount(db,
                entry.rowId,
                entry.note,
                entry.count);
        }
        finally {
            db.close();
        }
    }
}
