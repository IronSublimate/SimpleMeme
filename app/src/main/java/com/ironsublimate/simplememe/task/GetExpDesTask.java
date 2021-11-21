package com.ironsublimate.simplememe.task;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.ironsublimate.simplememe.R;
import com.ironsublimate.simplememe.activity.MainActivity;
import com.ironsublimate.simplememe.bean.Expression;
import com.ironsublimate.simplememe.ocr.PaddleOCRNcnn;
import com.ironsublimate.simplememe.util.UIUtil;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
    private static final int serviceID = 2333;
    private static final String notificationChannelName = "channel_get_description";
    private static final NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(UIUtil.getContext(), Integer.toString(notificationID));
    private final GetExpService service = new GetExpService();

    static {
        detector = new PaddleOCRNcnn();
        AssetManager assets = UIUtil.getContext().getAssets();
        detector.Init(assets);

    }

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
//        new NotificationCompat.Builder(UIUtil.getContext(), Integer.toString(notificationID);
//        notificationBuilder.setOngoing(true)
//                .setNotificationSilent()
//                .setContentTitle("识别文字中")
//                .setContentText("0/0")
//                .setSmallIcon(R.mipmap.ic_launcher_round)
//                .setProgress(0, 0, false);


    }

    public interface Callback {
        void onComplete(String result);
    }

    Callback callback = null;

    public void execute(Expression expression) {
        synchronized (GetExpDesTask.class) {
            taskCount++;
            if (taskCount == 1) {
                Context context = UIUtil.getContext();
                Intent intent = new Intent(context, GetExpService.class);
                context.startService(intent);
            }
        }
        executor.submit(() -> {
//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            String s = "";
            String s = writeDescription(expression);
            synchronized (GetExpDesTask.class) {
                taskCurrent++;
                if (taskCurrent == taskCount) {
                    taskCount = 0;
                    taskCurrent = 0;
                    notificationManager.cancel(notificationID);
                    Context context = UIUtil.getContext();
                    Intent intent = new Intent(context, GetExpService.class);
                    context.stopService(intent);
                } else {
                    notificationBuilder
//                            .setContentTitle("识别文字中")
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

    public static class GetExpService extends Service {
        public GetExpService() {
        }

        @Override
        public IBinder onBind(Intent intent) {
            // TODO: Return the communication channel to the service.
            return null;
        }

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            startForeground();
            return super.onStartCommand(intent, flags, startId);
        }

        private void startForeground() {
            Intent notificationIntent = new Intent(this, MainActivity.class);

            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                    notificationIntent, 0);
            this.startForeground(notificationID, notificationBuilder
                    .setOngoing(true)
                    .setNotificationSilent()
                    .setContentTitle("识别文字中")
                    .setContentText("0/0")
                    .setSmallIcon(R.mipmap.ic_launcher_round)
                    .setProgress(0, 0, false)
                    .setContentIntent(pendingIntent)
                    .build());
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
        }
    }
}

