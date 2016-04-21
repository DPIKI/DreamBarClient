package dpiki.dreamclient;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class MenuItemView extends AppCompatActivity {
    private String currentCategory;

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
        setContentView(R.layout.activity_menu_item_view);

        Intent intent = getIntent();
        currentCategory = intent.getStringExtra("category");
        Log.d("MenuItemView.onCreate", currentCategory);

        TextView textView = (TextView)findViewById(R.id.tw_category);
        textView.setText(currentCategory);

        ArrayList<String> names = Singleton.menuGetItemNames(currentCategory);
        Log.d("MenuItemView.onCreate", Integer.toString(names.size()));
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, names);
        ListView listView = (ListView)findViewById(R.id.lv_names);
        listView.setAdapter(adapter);
    }
}
