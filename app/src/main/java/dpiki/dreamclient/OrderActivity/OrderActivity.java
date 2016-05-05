package dpiki.dreamclient.OrderActivity;

import android.app.Dialog;
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
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
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
    ArrayList<OrderEntry> orderEntries = new ArrayList<>();
    NetworkServiceMessageReceiver receiver;

    ListView listView;
    RelativeLayout orderLayout;
    RelativeLayout progressBarLayout;
    TextView textView;

    Dialog dialog;
    TextView tvDialogCount;
    TextView tvDialogName;
    EditText editDialogNotes;
    Button btnDialogInc;
    Button btnDialogDec;
    Button btnDialogOk;
    Button btnDialogCancel;
    OrderEntry currentOrderEntry;
    int bufCount;

    Dialog selectTableDialog;
    Button btnStDialogOk;
    Button btnStDialogCancel;
    EditText editStDialogTable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);

        orderLayout = (RelativeLayout) findViewById(R.id.ov_order_layout);
        progressBarLayout = (RelativeLayout) findViewById(R.id.ov_pb_layout);
        textView = (TextView) findViewById(R.id.ov_pb_text_view);
        listView = (ListView) findViewById(R.id.lv_orders);
        isServiceConnected = false;

        final View headerView = getLayoutInflater().inflate(
                R.layout.activity_order_header, null);
        listView.addHeaderView(headerView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    startActivity(new Intent(OrderActivity.this, MenuActivity.class));
                } else {
                    currentOrderEntry = orderEntries.get(position - 1);
                    bufCount = currentOrderEntry.count;
                    tvDialogName.setText(currentOrderEntry.name);
                    tvDialogCount.setText("Количество: " + Integer.toString(currentOrderEntry.count));
                    editDialogNotes.setText(currentOrderEntry.note);
                    dialog.show();
                }
            }
        });

        dialog = new Dialog(this);
        dialog.setTitle("Редактирование заказа");
        dialog.setContentView(R.layout.activity_order_dialog);
        tvDialogCount = (TextView) dialog.findViewById(R.id.ov_dialog_tv_count);
        tvDialogName = (TextView) dialog.findViewById(R.id.ov_dialog_tv_name);
        editDialogNotes = (EditText) dialog.findViewById(R.id.ov_dialog_edit_note);
        btnDialogDec = (Button) dialog.findViewById(R.id.ov_dialog_btn_minus);
        btnDialogInc = (Button) dialog.findViewById(R.id.ov_dialog_btn_plus);
        btnDialogOk = (Button) dialog.findViewById(R.id.ov_dialog_btn_ok);
        btnDialogCancel = (Button) dialog.findViewById(R.id.ov_dialog_btn_cancel);

        btnDialogInc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bufCount < 1000) {
                    bufCount++;
                    tvDialogCount.setText("Количество: " + Integer.toString(bufCount));
                } else {
                    Toast.makeText(OrderActivity.this,
                            "Ебанулся?", Toast.LENGTH_LONG).show();
                }
            }
        });

        btnDialogDec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bufCount > 1) {
                    bufCount--;
                    tvDialogCount.setText("Количество: " + Integer.toString(bufCount));
                }
            }
        });

        btnDialogOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentOrderEntry.count = bufCount;
                currentOrderEntry.note = editDialogNotes.getText().toString();
                OrderListAdapter adapter = new OrderListAdapter(OrderActivity.this,
                        orderEntries);
                listView.setAdapter(adapter);

                DatabaseHelper helper = new DatabaseHelper(OrderActivity.this);
                SQLiteDatabase db = helper.getWritableDatabase();
                try {
                    DatabaseOrderWorker.updateOrderNoteAndCount(db,
                            currentOrderEntry.rowId,
                            currentOrderEntry.note,
                            currentOrderEntry.count);
                }
                finally {
                    db.close();
                }
                dialog.dismiss();
            }
        });

        btnDialogCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

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

        Log.d("OrderActivity", "onCreate");
        initToolbar();
    }

    private void initToolbar(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_action_bar);
        setSupportActionBar(toolbar);
    }

    @Override
    protected void onResume() {
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_action_bar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        int id = menuItem.getItemId();

        switch (id) {
            case R.id.back:
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

    void viewProgress(String title) {
        textView.setText(title);
        orderLayout.setVisibility(View.GONE);
        progressBarLayout.setVisibility(View.VISIBLE);
    }

    void viewOrder() {
        orderLayout.setVisibility(View.VISIBLE);
        progressBarLayout.setVisibility(View.GONE);
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
            } else {
                viewProgress("Connecting...");
            }
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
            viewProgress("Connecting...");
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
            } finally {
                db.close();
            }

            updateAdapter();
            viewOrder();

            Toast.makeText(OrderActivity.this, "Заказ отправлен", Toast.LENGTH_SHORT).show();
            Log.d("OrderActivity", "onOrderMade");
        }
    };
}
