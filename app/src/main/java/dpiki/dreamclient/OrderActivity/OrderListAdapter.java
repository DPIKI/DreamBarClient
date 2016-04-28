//
//TODO: запрос
//
package dpiki.dreamclient.OrderActivity;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import dpiki.dreamclient.Database.DatabaseHelper;
import dpiki.dreamclient.R;

/**
 * Created by prog1 on 24.04.2016.
 */
public class OrderListAdapter extends BaseAdapter {
    Context context;
    LayoutInflater layoutInflater;
    ArrayList<OrderEntry> orderEntries;

    OrderListAdapter(Context ctx, ArrayList<OrderEntry> orders){
        context = ctx;
        orderEntries = orders;
        layoutInflater = (LayoutInflater) context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
       return orderEntries.size();
    }

    @Override
    public Object getItem(int position) {
        return orderEntries.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = convertView;
        if (view == null){
            view = layoutInflater.inflate(R.layout.activity_order_entry,
                    parent, false);
        }


        OrderEntry order = getOrderEntry(position);

        ((TextView) view.findViewById(R.id.tv_order_name)).setText(
                "" + order.name);
        ((TextView) view.findViewById(R.id.tv_order_count)).setText(
                "" + order.count);
       if (!order.note.toString().equals("")) {
            ((TextView) view.findViewById(R.id.tv_note)).setText(
                    order.note.toString());
        }
        else {
             ((TextView) view.findViewById(R.id.tv_note)).setText(
                     "Без заметок");
        }

        return view;
    }

    OrderEntry getOrderEntry(int position){
        return ((OrderEntry) getItem(position));
    }

}


























