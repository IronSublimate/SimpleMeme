package com.ironsublimate.simplememe.task;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.ironsublimate.simplememe.R;
import com.ironsublimate.simplememe.bean.Expression;
import com.ironsublimate.simplememe.ocr.PaddleOCRNcnn;
import com.ironsublimate.simplememe.util.UIUtil;
import com.ironsublimate.simplememe.view.ExpImageDialog;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <pre>
 *     author : ironsublimate
 *     e-mail : houyuxuan3120487@163.com
 *     time   : 2021/10/07
 *     desc   :generate description words by AI
 *     version: 1.0
 * </pre>
 */
public class GetExpDesTask {
    private static final String TAG = "Detector";
    private static final PaddleOCRNcnn detector;
    private static final ExecutorService executor = Executors.newSingleThreadExecutor(); // change according to your requirements;
    private static final Handler handler = new Handler(Looper.getMainLooper());
    //    private static final LinkedBlockingDeque<GetExpDesTask> queue = new LinkedBlockingDeque<GetExpDesTask>();
    //    private static final NotificationManager notificationManager = (NotificationManager) UIUtil.getContext().getSystemService(Context.NOTIFICATION_SERVICE);
    private final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(UIUtil.getContext());

    private static final int notificationID = 2333;
    private static final String notificationChannelName = "channel_get_description";
    private final NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(UIUtil.getContext(), Integer.toString(notificationID));


    static {
        detector = new PaddleOCRNcnn();
        AssetManager assets = UIUtil.getContext().getAssets();
        detector.Init(assets);

    }

    private Activity activity;
    private int count = 0;
    private boolean isRepeat;
    private ExpImageDialog dialog = null;
    private Notification notification = null;
//    private Expression expression = null;

    private volatile static int taskCount = 0;
    private volatile static int taskCurrent = 0;

    public GetExpDesTask() {
        init();
    }

    public GetExpDesTask(Callback callback) {
        this.callback = callback;
        init();
    }

    private void init() {
        NotificationChannel channel = new NotificationChannel(Integer.toString(notificationID), notificationChannelName, NotificationManager.IMPORTANCE_DEFAULT);
        notificationManager.createNotificationChannel(channel);
        notificationBuilder.setOngoing(true)
                .setContentTitle("识别文字中")
                .setContentText("0/0")
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setProgress(0, 0, false);
    }

    public interface Callback {
        void onComplete(String result);
    }

    Callback callback = null;

    public void execute(Expression expression) {
        synchronized (GetExpDesTask.class) {
            taskCount++;
        }
        executor.submit(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            String s = "";
//            String s = writeDescription(expression);
            synchronized (GetExpDesTask.class) {
                taskCurrent++;
                if (taskCurrent == taskCount) {
                    taskCount = 0;
                    taskCurrent = 0;
                    notificationManager.cancel(notificationID);
                } else {
                    notificationBuilder
                            .setContentText(taskCurrent + "/" + taskCount)
                            .setProgress(taskCount, taskCurrent, false);
                    notificationManager.notify(notificationID, notificationBuilder.build());
                }
            }
            handler.post(() -> {
                if (this.callback != null) {
                    callback.onComplete(s);
                }
            });

        });
    }

    //Please check expression.getDesStatus() == 0 before call this function
    static private String writeDescription(Expression expression) {
        Bitmap image = BitmapFactory.decodeFile(expression.getUrl());
        PaddleOCRNcnn.Obj[] objs = detector.Detect(image, false);
        StringBuilder sb = new StringBuilder();
        for (PaddleOCRNcnn.Obj o : objs) {
            sb.append(o.label);
            sb.append('\n');
//            Log.i(TAG,o.label);
        }
        if (sb.length() > 1) {
            sb.deleteCharAt(sb.length() - 1);
        }
        String s = sb.toString();
        expression.setDesStatus(1);
        expression.setDescription(s);
        expression.save();
        return s;
    }
}

