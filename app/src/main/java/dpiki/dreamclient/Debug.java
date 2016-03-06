package dpiki.dreamclient;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class Debug extends AppCompatActivity {
    EditText editId;
    EditText editName;
    EditText editCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);

        editId = (EditText)findViewById(R.id.e_debugId);
        editName = (EditText)findViewById(R.id.e_debugName);
        editCategory = (EditText)findViewById(R.id.e_debugCategory);
    }

    public void onClickAdd(View view) {
        try {
            MenuEntry me = new MenuEntry();
            me.id = Integer.parseInt(editId.getText().toString());
            me.name = editName.getText().toString();
            me.category = editCategory.getText().toString();

            Singletone s = Singletone.getInstance();
            s.menu.add(me);

            Log.d("Debug.onClick", "entry added");
        }
        catch (NumberFormatException e) {
            Toast toast = Toast.makeText(this, "Неверный формат id", Toast.LENGTH_SHORT);
            toast.show();
        }
    }
}
