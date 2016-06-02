package dpiki.dreamclient;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import dpiki.dreamclient.OrderActivity.OrderEntry;

/**
 * Created by Lenovo on 31.05.2016.
 */
public class EditDialog {

    Dialog dialog;
    Context context;
    OrderEntry currentOrderEntry;

    TextView tvDialogCount;
    TextView tvDialogName;
    EditText editDialogNotes;
    Button btnDialogInc;
    Button btnDialogDec;
    Button btnDialogOk;
    Button btnDialogCancel;
    IEditDialogCallback callback;

    int bufCount;

    public EditDialog(Context ctx, Dialog dlg,
                      IEditDialogCallback clbck) {
        context = ctx;
        dialog = dlg;
        currentOrderEntry = null;
        bufCount = 0;
        callback = clbck;

        dialog.setTitle("Редактирование заказа");
        dialog.setContentView(R.layout.activity_order_dialog);
        tvDialogCount = (TextView) dialog.findViewById(R.id.ov_dialog_tv_count);
        tvDialogName = (TextView) dialog.findViewById(R.id.ov_dialog_tv_name);
        editDialogNotes = (EditText) dialog.findViewById(R.id.ov_dialog_edit_note);
        btnDialogDec = (Button) dialog.findViewById(R.id.ov_dialog_btn_minus);
        btnDialogInc = (Button) dialog.findViewById(R.id.ov_dialog_btn_plus);
        btnDialogOk = (Button) dialog.findViewById(R.id.ov_dialog_btn_ok);
        btnDialogCancel = (Button) dialog.findViewById(R.id.ov_dialog_btn_cancel);

        btnDialogInc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bufCount < 1000) {
                    bufCount++;
                    tvDialogCount.setText("Количество: " + Integer.toString(bufCount));
                } else {
                    Toast.makeText(context,
                            "Ебанулся?", Toast.LENGTH_LONG).show();
                }
            }
        });

        btnDialogDec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bufCount > 1) {
                    bufCount--;
                    tvDialogCount.setText("Количество: " + Integer.toString(bufCount));
                }
            }
        });

        btnDialogOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentOrderEntry.count = bufCount;
                currentOrderEntry.note = editDialogNotes.getText().toString();
                callback.onOkButtonClick(currentOrderEntry);
                dialog.dismiss();
            }
        });

        btnDialogCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    public void showDialog(OrderEntry entry) {
        currentOrderEntry = entry;
        bufCount = entry.count;
        tvDialogName.setText(currentOrderEntry.name);
        tvDialogCount.setText("Количество: " + Integer.toString(currentOrderEntry.count));
        editDialogNotes.setText(currentOrderEntry.note);
        dialog.show();
    }

    public void hideDialog() {
        dialog.dismiss();
    }
}
