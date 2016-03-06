package dpiki.dreamclient;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class General extends AppCompatActivity {

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
}
