package com.ironsublimate.simplememe.task;

import android.app.Activity;
import android.os.AsyncTask;

import com.afollestad.materialdialogs.MaterialDialog;
import com.ironsublimate.simplememe.GlobalConfig;
import com.ironsublimate.simplememe.util.FileUtil;

import es.dmoral.toasty.Toasty;

/**
 * <pre>
 *     author : hewro
 *     e-mail : ihewro@163.com
 *     time   : 2018/08/11
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public class DeleteExpFolderTask extends AsyncTask<Void,Void,Boolean> {

    private String  folderName;
    private Activity activity;

    private MaterialDialog dialog;

    public DeleteExpFolderTask(String folderName,Activity activity) {
        this.folderName = folderName;
        this.activity = activity;
    }

    @Override
    protected void onPreExecute() {

    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        FileUtil.delFolder(GlobalConfig.appDirPath + folderName);
        return null;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        Toasty.success(activity,"删除成功").show();
    }
}
