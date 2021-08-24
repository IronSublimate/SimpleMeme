package com.ironsublimate.mememanager.task;

import android.os.AsyncTask;
import android.support.v4.app.Fragment;

import com.ironsublimate.mememanager.bean.ExpressionFolder;
import com.ironsublimate.mememanager.callback.GetMainExpListener;
import com.ironsublimate.mememanager.fragment.ExpressionContentFragment;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;

/**
 * <pre>
 *     author : hewro
 *     e-mail : ihewro@163.com
 *     time   : 2018/07/11
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public class GetExpFolderTask extends AsyncTask<Void,Void,Void> {

    private GetMainExpListener listener;

    private List<ExpressionFolder> expressionFolderList = new ArrayList<>();
    private List<String> pageTitleList = new ArrayList<>();
    private List<Fragment> fragmentList = new ArrayList<>();

    public GetExpFolderTask(GetMainExpListener listener) {
        this.listener = listener;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        //从数据库中获取表情包信息
        expressionFolderList = LitePal.order("ordervalue").find(ExpressionFolder.class);

        if (expressionFolderList.size() == 0) {//如果没有表情包目录，则会显示为空
            fragmentList.add(ExpressionContentFragment.fragmentInstant("默认",true,0));
            pageTitleList.add("默认");
        } else {
            for (int i = 0; i < expressionFolderList.size(); i++) {
                fragmentList.add(ExpressionContentFragment.fragmentInstant(expressionFolderList.get(i).getName(),false,i));
                pageTitleList.add(expressionFolderList.get(i).getName());
            }
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        listener.onFinish(fragmentList,pageTitleList);
    }
}
