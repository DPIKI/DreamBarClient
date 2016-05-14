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

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;

        if (view == null){
            view = mLayoutInflater.inflate(R.layout.activity_menu_entry,
                    parent, false);
        }

        MenuEntry menuEntry = getMenuEntry(position);

        ((TextView) view.findViewById(R.id.tv_menu_item_name)).setText(menuEntry.name);

        ImageView imageView = (ImageView) view.findViewById(R.id.iv_menu_item_image);
        picassoBuilder.addRequestHandler(new CustomRequestHandler(downloadManager));
        Picasso picasso = picassoBuilder.build();
        picasso.load(CustomRequestHandler.SCHEME + "://" + Integer.toString(menuEntry.id))
                .error(R.drawable.ic_action_new)
                .into(imageView);
        return view;
    }

    MenuEntry getMenuEntry(int position){
        return ((MenuEntry) getItem(position));
    }
}
