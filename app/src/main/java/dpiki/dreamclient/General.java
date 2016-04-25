package dpiki.dreamclient;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class General extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_general);
    }

    public void onClickNewOrder(View v) {
       Intent intent = new Intent(this, OrderActivity.class);
       startActivity(intent);
    }

    public void onClickDebug(View v) {
        Intent intent = new Intent(this, Debug.class);
        startActivity(intent);
    }

    public void onClickMenu(View v) {
        Intent intent = new Intent(this, MenuCategoryView.class);
        startActivity(intent);
    }

    public void onClickSettings(View v) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }
}
