package dpiki.dreamclient;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class General extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_general);
    }

    public void onClickNewOrder(View v) {
    }

    public void onClickDebug(View v) {
        Intent intent = new Intent(this, Debug.class);
        startActivity(intent);
    }

    public void onClickMenu(View v) {
        Intent intent = new Intent(this, MenuCategoryView.class);
        startActivity(intent);
    }
}
