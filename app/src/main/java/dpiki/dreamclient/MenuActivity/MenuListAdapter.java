package dpiki.dreamclient.MenuActivity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import dpiki.dreamclient.R;

/**
 * Created by prog1 on 27.04.2016.
 */
public class MenuListAdapter extends BaseAdapter{
    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private ArrayList<MenuEntry> mMenuEntries;

    MenuListAdapter(Context context, ArrayList<MenuEntry> menuEntries){
        mContext = context;
        mMenuEntries = menuEntries;
        mLayoutInflater = (LayoutInflater) context.getSystemService(
                mContext.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return mMenuEntries.size();
    }

    @Override
    public Object getItem(int position) {
        return mMenuEntries.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;

        if (view == null){
            view = mLayoutInflater.inflate(R.layout.activity_menu_entry,
                    parent, false);
        }

        MenuEntry menuEntry = getMenuEntry(position);

        ((TextView) view.findViewById(R.id.tv_menu_item_name)).setText(
                "" + menuEntry.name);
        //TODO: добавить картинку

        return view;
    }

    MenuEntry getMenuEntry(int position){
        return ((MenuEntry) getItem(position));
    }
}
