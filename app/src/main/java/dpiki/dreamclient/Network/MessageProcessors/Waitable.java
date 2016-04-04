package dpiki.dreamclient.Network.MessageProcessors;

import android.os.Message;
import android.util.Log;

import dpiki.dreamclient.Network.NetworkService;
import dpiki.dreamclient.Network.NetworkServiceHandler;

/**
 * Created by User on 04.04.2016.
 */
public abstract class Waitable extends LostConnectable {

    private int mTryCount;

    public Waitable(NetworkServiceHandler handler, int tryCount) {
        super(handler);
        mTryCount = tryCount;
    }

    @Override
    public void onInvalidRequest() {
        Log.d("Waitablle", "onInvalidRequest");

        Message msg = mHandler.obtainMessage();

        IMessageProcessor newState;
        int msgWhat;
        switch (state()) {
            case NetworkService.STATE_AUTH_WAIT:
                newState = new AuthMessageProcessor(mHandler);
                msgWhat = NetworkService.MESSAGE_AUTH;
                break;

            case NetworkService.STATE_SYNC_WAIT:
                newState = new SyncMessageProcessor(mHandler);
                msgWhat = NetworkService.MESSAGE_SYNC;
                break;

            case NetworkService.STATE_MENU_WAIT:
                newState = new MenuMessageProcessor(mHandler);
                msgWhat = NetworkService.MESSAGE_MENU;
                break;

            case NetworkService.STATE_READY_WAIT:
                newState = new ReadyMessageProcessor(mHandler);
                msgWhat = NetworkService.MESSAGE_SEND_ORDER;
                break;

            default:
                Log.d("Waitable", "error invalid state");
                return;
        }

        // Меняем состояние
        mHandler.changeState(newState, NetworkService.MESSAGE_INVALID_REQUEST);

        // Говорим попытаться еще раз через секунду
        msg.what = msgWhat;
        msg.arg1 = mTryCount + 1;
        mHandler.sendMessageDelayed(msg, 1000);
    }
}
