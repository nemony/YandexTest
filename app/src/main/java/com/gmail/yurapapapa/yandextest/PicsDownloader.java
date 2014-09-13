package com.gmail.yurapapapa.yandextest;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;

import com.yandex.disk.client.ListItem;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;

/**
 * Created by yurap_000 on 011 11 сен.
 */
public class PicsDownloader extends HandlerThread {
    private static final String TAG = "PicsDownloader";
    private static final int MESSAGE_DOWNLOAD = 0;
    private Context mContext;
    private static Handler mHandler;

    Listener listener;

    public interface Listener {
        void onPicDownloaded(Bitmap bitmap);
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public PicsDownloader(Context context) {
        super(TAG);
        mContext = context;
    }

    @Override
    protected void onLooperPrepared() {
        Log.d(TAG, "onLooperPrepared");
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == MESSAGE_DOWNLOAD) {
                    ListItem item = (ListItem) msg.obj;
                    Log.d(TAG, "Got a request for ListItem " + item.getDisplayName());
                    handleRequest(item);
                }
            }
        };
        Log.d(TAG, "onLooperPrepared2");
    }

    public boolean queuePics(ArrayList<ListItem> list) {
        if (mHandler == null)
            return false;
        for (ListItem item : list) {
            mHandler.obtainMessage(MESSAGE_DOWNLOAD, item).sendToTarget();
        }
        return true;
    }

    private void handleRequest(ListItem item) {
        String token = PreferenceManager.getDefaultSharedPreferences(mContext).getString(MyActivity.TOKEN, null);

        HttpClient httpClient = new DefaultHttpClient();

        HttpGet get = new HttpGet("https://webdav.yandex.ru" + item.getFullPath() + "?preview&size=L");
        get.addHeader("Authorization", "OAuth " + token);

        InputStream in;
        ByteArrayOutputStream out;
        try {
            HttpResponse response = httpClient.execute(get);
            Log.d(TAG, "status " + response.getStatusLine());
            if (response.getStatusLine().getStatusCode() != HttpURLConnection.HTTP_OK) {
                return;
            }
            in = response.getEntity().getContent();
            out = new ByteArrayOutputStream();
            int bytesRead = 0;
            byte[] buff = new byte[1024 * 128];
            while ((bytesRead = in.read(buff)) > 0) {
                out.write(buff, 0, bytesRead);
            }
            byte[] bitmapBytes = out.toByteArray();
            out.close();
            in.close();
            Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.length);
            listener.onPicDownloaded(bitmap);
        } catch (Exception e) {
            Log.d(TAG, e.getCause() + " " + e.getClass() + " " + e.toString());
        }
    }

    public void clearQueue() {
        mHandler.removeMessages(MESSAGE_DOWNLOAD);

    }
}
