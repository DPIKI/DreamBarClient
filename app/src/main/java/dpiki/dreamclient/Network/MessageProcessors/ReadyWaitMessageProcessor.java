package dpiki.dreamclient.Network.MessageProcessors;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.Message;
import android.support.v7.app.NotificationCompat;

import dpiki.dreamclient.Network.NetworkService;
import dpiki.dreamclient.Network.NetworkServiceHandler;

/**
 * Created by User on 30.03.2016.
 */
public class ReadyWaitMessageProcessor extends ImageLoadable {

    public ReadyWaitMessageProcessor(NetworkServiceHandler handler) {
        super(handler);
    }

    @Override
    public int state() {
        return NetworkService.STATE_READY_WAIT;
    }

    @Override
    public void onOrderMade(Message msg) {
        mHandler.changeState(new ReadyMessageProcessor(mHandler),
                NetworkService.MESSAGE_ORDER_MADE);
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
