package dpiki.dreamclient;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class MenuCategoryView extends AppCompatActivity {

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_category_view);

        DatabaseHelper dbHelper = new DatabaseHelper(this);
        ArrayList<MenuEntry> menuEntries = DatabaseHelper.readMenu(dbHelper.getReadableDatabase());

        ArrayList<String> categories = new ArrayList<>();
        for(int i = 0;i <menuEntries.size();i++){
            categories.add(menuEntries.get(i).category.toString());
        }

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
