package dpiki.dreamclient;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.ContactsContract;

import java.util.ArrayList;
import java.util.Iterator;

import dpiki.dreamclient.Network.NetworkService;

/**
 * Created by User on 13.05.2016.
 */
public class ImageDownloadManager extends Handler {

    class ImageRequest {
        public int id;
        public byte[] img;
    }

    private NetworkService mNetworkService;
    private final ArrayList<ImageRequest> waiters = new ArrayList<>();

    public ImageDownloadManager(Looper looper) {
        super(looper);
    }

    @Override
    public void handleMessage(Message msg) {
        mNetworkService.downloadImage(msg.what);
    }

    public void setNetworkService(NetworkService service) {
        mNetworkService = service;
    }

    public void resetNetworkService() {
        synchronized (waiters) {
            for (ImageRequest i : waiters) {
                synchronized (i) {
                    i.notify();
                }
            }
            waiters.clear();
        }
    }

    public void publishImage(int id, byte[] image) {
        synchronized (waiters) {
            Iterator<ImageRequest> it = waiters.iterator();
            while (it.hasNext()) {
                ImageRequest i = it.next();
                if (i.id == id) {
                    synchronized (i) {
                        i.img = image;
                        i.notify();
                    }
                    it.remove();
                }
            }
        }
    }

    public Bitmap getImage(int id) {
        try {
            ImageRequest ir = new ImageRequest();
            ir.id = id;
            synchronized (waiters) {
                waiters.add(ir);
            }

            Message msg = obtainMessage();
            msg.what = id;
            sendMessage(msg);

            synchronized (ir) {
                ir.wait();
            }

            if (ir.img == null) {
                return null;
            } else {
                return BitmapFactory.decodeByteArray(ir.img, 0, ir.img.length);
            }
        }
        catch (InterruptedException e) {
            return null;
        }
    }
}
