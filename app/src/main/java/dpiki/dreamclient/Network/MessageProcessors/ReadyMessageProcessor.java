package dpiki.dreamclient.Network.MessageProcessors;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import dpiki.dreamclient.Network.NetworkService;
import dpiki.dreamclient.Network.NetworkServiceHandler;
import dpiki.dreamclient.Network.NetworkServiceWriter;

/**
 * Created by User on 30.03.2016.
 */
public class ReadyMessageProcessor extends ImageLoadable {

    public ReadyMessageProcessor(NetworkServiceHandler handler) {
        super(handler);
    }

    @Override
    public int state() {
        return NetworkService.STATE_READY;
    }

    @Override
    public void onSendOrder(Message msg) {
        // Меняем состояние
        mHandler.changeState(new ReadyWaitMessageProcessor(mHandler),
                NetworkService.MESSAGE_SEND_ORDER);

        // Формируем данные для отправки
        Bundle bundle = new Bundle();
        bundle.putInt(NetworkServiceWriter.KEY_ACTION_CODE, NetworkService.ACT_MAKE_ORDER);

        // Выводим их
        NetworkServiceWriter writer = new NetworkServiceWriter(mHandler.context, mHandler.socket, bundle);
        writer.start();

        Log.d("RMP", "Order sent");
    }

    @Override
    public void onCall(Message msg) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mHandler.context);
        builder.setTicker("Оповещение от бармена")
                .setSmallIcon(android.R.drawable.ic_menu_info_details)
                .setLargeIcon(BitmapFactory.decodeResource(mHandler.context.getResources(),
                        android.R.drawable.ic_popup_reminder))
                .setWhen(System.currentTimeMillis())
                .setDefaults(Notification.DEFAULT_ALL)
                .setContentTitle("Оповещение от бармена")
                .setContentText("Оповещение от бармена");
        Notification notification = builder.build();
        NotificationManager nm = (NotificationManager)mHandler.context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(101, notification);
    }

}
