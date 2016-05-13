package dpiki.dreamclient.MenuActivity;

import android.graphics.Bitmap;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Request;
import com.squareup.picasso.RequestHandler;

import java.io.IOException;

import dpiki.dreamclient.Network.NetworkService;

public class CustomRequestHandler extends RequestHandler{
    public static final String SCHEME = "";

    @Override
    public boolean canHandleRequest(Request data) {
        return SCHEME.equals(data.uri.getScheme());
    }

    @Override
    public Result load(Request request, int networkPolicy) throws IOException {
        int key = Integer.getInteger(request.uri.getHost());
        Bitmap bitmap = NetworkService.downloadImage(key);
        return new Result(bitmap, Picasso.LoadedFrom.NETWORK);
    }
}
