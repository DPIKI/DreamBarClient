package dpiki.dreamclient.Network.MessageProcessors;

import android.os.Bundle;
import android.os.Message;
import android.util.Log;

import dpiki.dreamclient.Network.NetworkService;
import dpiki.dreamclient.Network.NetworkServiceHandler;
import dpiki.dreamclient.Network.NetworkServiceReader;
import dpiki.dreamclient.Network.NetworkServiceWriter;

/**
 * Created by User on 13.05.2016.
 */
public abstract class ImageLoadable extends LostConnectable {

    public ImageLoadable(NetworkServiceHandler handler) {
        super(handler);
    }

    @Override
    public void onImageLoaded(Message msg) {
        // Просто пересылаем ui потоку то что пришло от Reader'а
        mHandler.sendImageToUi(msg.getData());
    }

    @Override
    // msg.arg1 - id изображения, которое надо подгрузить
    public void onSendLoadImageRequest(Message msg) {
        // Формируем данные для отправки на сервер
        Bundle bundle = new Bundle();
        bundle.putInt(NetworkServiceWriter.KEY_ACTION_CODE, NetworkService.ACT_GET_IMAGE);
        bundle.putInt(NetworkServiceWriter.KEY_IMAGE_ID, msg.arg1);

        // Отправляем
        NetworkServiceWriter writer =
                new NetworkServiceWriter(mHandler.context, mHandler.socket, bundle);
        mHandler.writerHandler.post(writer);

        Log.d("ImageLoadable", "onSendLoadImageRequest");
    }
}
