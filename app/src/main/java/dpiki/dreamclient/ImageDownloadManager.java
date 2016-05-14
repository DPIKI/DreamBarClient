package dpiki.dreamclient;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.google.common.collect.HashMultimap;

import java.util.HashMap;

import dpiki.dreamclient.Network.NetworkService;
import dpiki.dreamclient.Network.NetworkServiceReader;

/**
 * Created by User on 13.05.2016.
 */
public class ImageDownloadManager extends Handler {

    private NetworkService mNetworkService;
    private HashMultimap<Integer, Message> waiters = HashMultimap.create();

    public ImageDownloadManager(Looper looper) {
        super(looper);
    }

    @Override
    public void handleMessage(Message msg) {
        if (mNetworkService != null) {
            waiters.put(msg.what, msg);
            mNetworkService.downloadImage(msg.what);
        } else {
            msg.notify();
        }
    }

    // -------- synchronous API -----------

    public void setNetworkService(NetworkService service) {
        mNetworkService = service;
    }

    public void resetNetworkService() {
        for (Message msg : waiters.values()) {
            msg.notify();
        }

        waiters.clear();

        mNetworkService = null;
    }

    public void publishImage(int id, byte[] image) {
        if (mNetworkService != null) {
            Bundle bundle = new Bundle();
            bundle.putByteArray(NetworkServiceReader.KEY_IMAGE, image);
            for (Message msg : waiters.get(id)) {
                msg.setData(bundle);
                msg.notify();
            }
            waiters.removeAll(id);
        }
    }

    // -------- asynchronous API -----------

    public Bitmap getImage(int id) {
        Message msg = obtainMessage();
        msg.what = id;
        sendMessage(msg);
        try {
            msg.wait();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }

        Bundle bundle = msg.getData();
        if (bundle == null)
            return null;

        byte[] bytesImage = bundle.getByteArray(NetworkServiceReader.KEY_IMAGE);
        if (bytesImage == null)
            return null;

        return BitmapFactory.decodeByteArray(bytesImage, 0, bytesImage.length);
    }
}
