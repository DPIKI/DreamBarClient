package dpiki.dreamclient.MenuActivity;

import android.content.Context;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import dpiki.dreamclient.ImageDownloadManager;
import dpiki.dreamclient.R;

/**
 * Created by prog1 on 27.04.2016.
 */
public class MenuListAdapter extends BaseAdapter{
    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private ArrayList<MenuEntry> mMenuEntries;
    private Picasso.Builder picassoBuilder;
    private ImageDownloadManager downloadManager;

    MenuListAdapter(Context context, ArrayList<MenuEntry> menuEntries,
                    ImageDownloadManager manager){
        mContext = context;
        mMenuEntries = menuEntries;
        mLayoutInflater = (LayoutInflater) context.getSystemService(
                mContext.LAYOUT_INFLATER_SERVICE);
        picassoBuilder = new Picasso.Builder(mContext);
        downloadManager = manager;
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

    static class ViewHolder {
        TextView tvItemName;
        ImageView ivImage;
        Picasso picasso;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null){
            convertView = mLayoutInflater.inflate(R.layout.activity_menu_entry,
                    parent, false);
            viewHolder = new ViewHolder();

            viewHolder.tvItemName = (TextView) convertView.findViewById(R.id.tv_menu_item_name);

            viewHolder.ivImage = (ImageView) convertView.findViewById(R.id.iv_menu_item_image);

            picassoBuilder.addRequestHandler(new CustomRequestHandler(downloadManager));
            viewHolder.picasso = picassoBuilder.build();

            convertView.setTag(viewHolder);
        }else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        MenuEntry menuEntry = getMenuEntry(position);

        viewHolder.tvItemName.setText(menuEntry.name);

        viewHolder.picasso.load(CustomRequestHandler.SCHEME + "://" + Integer.toString(menuEntry.id))
                .error(R.drawable.ic_action_new)
                .into(viewHolder.ivImage);

        return convertView;
    }

    MenuEntry getMenuEntry(int position){
        return ((MenuEntry) getItem(position));
    }
}
