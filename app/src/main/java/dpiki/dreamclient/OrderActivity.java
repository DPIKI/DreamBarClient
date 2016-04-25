package dpiki.dreamclient;

import android.app.Dialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import java.util.ArrayList;

public class OrderActivity extends AppCompatActivity {

    ArrayList<OrderEntry> orderEntries = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);

        ListView listView = (ListView) findViewById(R.id.lv_orders);
        fill();
        View headerView = getLayoutInflater().inflate(
                R.layout.header, null);
        listView.addHeaderView(headerView);

        OrderListAdapter orderListAdapter = new OrderListAdapter(this, orderEntries);

        listView.setAdapter(orderListAdapter);

    }

    public void  onClick(View view){
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.order_dialog);
        dialog.setTitle("Стол №3");
        dialog.show();
    }

    public void fill(){
        for (int i = 1; i<20; i++){
            orderEntries.add(new OrderEntry(i,"элемент меню " + (i+7),i+3,i+12,
                    "заметки для бармена"));
        }
    }
}
