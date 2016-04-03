package dpiki.dreamclient;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class MenuCategoryView extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_category_view);

        ActionBar actionBar = getActionBar();
        actionBar.setTitle("My Title");
        Singleton singleton = Singleton.getInstance();
        final ArrayList<String> categories = singleton.menuGetCategories();

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, categories);

        ListView listView = (ListView)findViewById(R.id.lv_categories);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView textView = (TextView) view;
                String category = textView.getText().toString();
                Intent intent = new Intent(parent.getContext(), MenuItemView.class);
                intent.putExtra("category", category);
                startActivity(intent);
            }
        });
    }
}
