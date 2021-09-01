package com.ironsublimate.simplememe.task;

import android.app.Activity;
import android.os.AsyncTask;

import com.afollestad.materialdialogs.MaterialDialog;
import com.blankj.ALog;
import com.ironsublimate.simplememe.MyDataBase;
import com.ironsublimate.simplememe.bean.EventMessage;
import com.ironsublimate.simplememe.bean.Expression;
import com.ironsublimate.simplememe.bean.ExpressionFolder;
import com.ironsublimate.simplememe.callback.TaskListener;
import com.ironsublimate.simplememe.util.UIUtil;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import es.dmoral.toasty.Toasty;

/**
 * <pre>
 *     author : hewro
 *     e-mail : ihewro@163.com
 *     time   : 2018/08/23
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public class MoveExpTask extends AsyncTask<Void, Integer, Boolean> {

    private Activity activity;
    private List<Expression> expressionList;
    private List<Expression> originExpList;
    private String folderName;
    List<String> checkList;
    private MaterialDialog dialog;
    private Boolean status;//false 表示移动，true 表示 复制
    private List<ExpressionFolder> expressionFolderList;
    private TaskListener listener;

    public MoveExpTask(List<Expression> originExpList, List<String> checkList, String folderName, Activity activity, Boolean status, TaskListener listener) {
        this.activity = activity;
        this.originExpList = originExpList;
        this.checkList = checkList;
        this.folderName = folderName;
        this.status = status;
        this.listener = listener;

        ALog.d("目标目录名称" + folderName);
    }


    @Override
    protected void onPreExecute() {
        this.dialog = new MaterialDialog.Builder(activity)
                .progress(true, 0)
                .progressIndeterminateStyle(true)
                .show();
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        //组合中需要添加的文件list
        expressionList = new ArrayList<>();
        expressionList.clear();
        Collections.sort(checkList, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return Integer.parseInt(o2) - Integer.parseInt(o1);
            }
        });
        for (int i = 0; i < checkList.size(); i++) {
            expressionList.add(originExpList.get(Integer.parseInt(checkList.get(i))));
        }

        //具体操作
        for (Expression expression : expressionList) {
            //复制到
            if (status) {
                Expression newExp = new Expression(expression.getStatus(), expression.getName(), expression.getUrl(), folderName, expression.getImage());
                MyDataBase.addExpressionRecord(newExp);
            } else {
                //移动到
                String originFolderName = expression.getFolderName();
                MyDataBase.moveExpressionRecord(expression, originFolderName, folderName);
            }
        }
        //发送数据库变化通知
        UIUtil.autoBackUpWhenItIsNecessary();
        EventBus.getDefault().post(new EventMessage(EventMessage.DATABASE));
        return true;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        dialog.dismiss();
        if (!aBoolean) {
            Toasty.error(activity, "指定目录不存在，非法错误" + folderName).show();
        } else {
            Toasty.success(activity, "添加成功").show();
        }


        if (listener != null) {
            listener.onFinish(aBoolean);
        }


    }
}
