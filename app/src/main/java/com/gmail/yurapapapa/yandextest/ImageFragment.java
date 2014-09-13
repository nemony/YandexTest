package com.gmail.yurapapapa.yandextest;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.yandex.disk.client.ListItem;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by yurap_000 on 011 11 сен.
 */
public class ImageFragment extends Fragment {
    private static final String TAG = "ImageFragment";
    private PicsDownloader picsDownloader;
    private ImageView imageView;
    private List<Bitmap> bitmaps = new ArrayList<Bitmap>();
    private int amountOfPics;
    private int currPos = 0;
    private Dialog dialog;

    private static final int MESSAGE_OK = 1;
    private static final int MESSAGE_FAIL = 0;
    private boolean shouldSlide = true;
    private boolean shouldSlideSync;

    private Handler mHandler;
    private SlidingThread slidingThread;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        picsDownloader = new PicsDownloader(getActivity());
        picsDownloader.setListener(listener);
        picsDownloader.start();
        picsDownloader.getLooper();

        createDialog();
        createHandler();
        slidingThread = new SlidingThread();
        slidingThread.start();
    }
    private void createHandler() {
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MESSAGE_OK:
                        Log.d(TAG, "sdf");
                        if (dialog.isShowing())
                            dialog.dismiss();
                        //imageView.setVisibility(View.VISIBLE);
                        imageView.setImageBitmap(bitmaps.get(currPos));
                        currPos = (currPos + 1) % amountOfPics;
                        break;
                    case MESSAGE_FAIL:
                        Log.d(TAG, "MESSAGE_FAILED");
                        //imageView.setVisibility(View.INVISIBLE);
                            dialog.show();
                }
            }
        };
    }



    private PicsDownloader.Listener listener = new PicsDownloader.Listener() {
        @Override
        public void onPicDownloaded(final Bitmap bitmap) {
            Log.d(TAG, "onPicDownloaded");
            bitmaps.add(bitmap);
            slidingThread.resumeSliding();
        }
    };


    private class SlidingThread extends Thread {
        synchronized void resumeSliding(){
            Log.d(TAG, "resumeSliding");
            shouldSlideSync = true;
            notify();
        }
        @Override
        public void run() {
            try {
                while (shouldSlide) {
                    if (bitmaps != null && bitmaps.size() > currPos) {
                        mHandler.sendEmptyMessage(MESSAGE_OK);
                        TimeUnit.SECONDS.sleep(3);
                    } else {
                        mHandler.sendEmptyMessage(MESSAGE_FAIL);
                        shouldSlideSync = false;
                        synchronized (this) {
                            while (!shouldSlideSync) {
                                wait();
                            }
                        }
                    }
                }
            } catch (InterruptedException e) {
                Log.d(TAG, e.toString());
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        View v = inflater.inflate(R.layout.image_layout, container, false);

        ArrayList<ListItem> pics = getActivity().getIntent().getParcelableArrayListExtra("pics");
        amountOfPics = pics.size();
        boolean onLooperPrepared;
        do {
            onLooperPrepared = picsDownloader.queuePics(pics);
        } while (!onLooperPrepared);


        imageView = (ImageView) v.findViewById(R.id.imageView);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        return v;
    }


    private void createDialog() {
        dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog);
        dialog.setCancelable(false);
        final Window window = dialog.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        picsDownloader.clearQueue();
        picsDownloader.quit();
        mHandler.removeCallbacksAndMessages(null);
        shouldSlide = false;
    }


}
