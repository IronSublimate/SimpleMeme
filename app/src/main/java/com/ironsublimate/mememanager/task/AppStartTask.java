package com.ironsublimate.mememanager.task;

import android.os.AsyncTask;

import com.ironsublimate.mememanager.callback.GetExpFolderDataListener;

import static java.lang.Thread.sleep;

/**
 * <pre>
 *     author : hewro
 *     e-mail : ihewro@163.com
 *     time   : 2018/07/06
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public class AppStartTask extends AsyncTask<Void,Integer,String> {

    GetExpFolderDataListener listener;
    private boolean getOnes = false;
    long beginTime;
    long endTime;

    public AppStartTask(GetExpFolderDataListener listener) {
        this.listener = listener;
    }

    @Override
    protected String doInBackground(Void... voids) {
        try {
            sleep(800);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    protected void onPostExecute(String s) {
        listener.onFinish(s);
    }

}
