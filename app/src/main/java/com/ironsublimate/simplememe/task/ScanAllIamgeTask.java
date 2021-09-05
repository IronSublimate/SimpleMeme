package com.ironsublimate.simplememe.task;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.Log;

import com.ironsublimate.simplememe.GlobalConfig;
import com.ironsublimate.simplememe.MyDataBase;
import com.ironsublimate.simplememe.R;
import com.ironsublimate.simplememe.activity.MyActivity;
import com.ironsublimate.simplememe.bean.EventMessage;
import com.ironsublimate.simplememe.bean.Expression;
import com.ironsublimate.simplememe.callback.TaskListener;
import com.ironsublimate.simplememe.util.UIUtil;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.ArrayList;

//扫描本机全部表情包
public class ScanAllIamgeTask extends AsyncTask<Void, Void, Void> {
    static final String TAG="[Scan]";
    private Activity activity;

    public ScanAllIamgeTask(Activity activity) {
        this.activity = activity;
    }

    @Override
    protected Void doInBackground(Void... expressions) {
        ArrayList<String> listOfAllImages = new ArrayList<String>();
        Cursor cursor;
        int column_index_data, column_index_folder_name;
        String PathOfImage = null;
        Uri uri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        String[] projection = {MediaStore.MediaColumns.DATA,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME};

        cursor = activity.getContentResolver().query(uri, projection, null,
                null, null);

        column_index_data = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        column_index_folder_name = cursor
                .getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
        while (cursor.moveToNext()) {
            PathOfImage = cursor.getString(column_index_data);
            Log.i(TAG,PathOfImage);
            listOfAllImages.add(PathOfImage);
        }
        for (int i = 0; i < listOfAllImages.size(); i++) {
//            File tempFile = new File(listOfAllImages.get(i));
//            String fileName = tempFile.getName();
            String url = listOfAllImages.get(i);
            final Expression expression = new Expression(1, null, url, this.activity.getString(R.string.default_meme_folder));
            if (!MyDataBase.addExpressionRecord(expression)) {
//                publishProgress(GlobalConfig.ERROR_FILE_LIMIT);
            }
        }
        UIUtil.autoBackUpWhenItIsNecessary();
        EventBus.getDefault().post(new EventMessage(EventMessage.DATABASE));
        cursor.close();
        return null;
    }

}
