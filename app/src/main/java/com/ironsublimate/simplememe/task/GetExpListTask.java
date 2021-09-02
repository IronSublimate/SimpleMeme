package com.ironsublimate.simplememe.task;

import android.os.AsyncTask;

import com.ironsublimate.simplememe.bean.Expression;
import com.ironsublimate.simplememe.callback.GetExpListListener;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Thread.sleep;

/**
 * <pre>
 *     author : hewro
 *     e-mail : ihewro@163.com
 *     time   : 2018/07/12
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public class GetExpListTask extends AsyncTask<String,Void,List<Expression>>{

    GetExpListListener listener;
    private boolean isImage;
    public GetExpListTask(GetExpListListener listener) {
        this.listener = listener;
    }

    /**
     *
     * @param listener
     * @param isImage 是否查询表情列表，将图片的数据也查询出来
     */
    public GetExpListTask(GetExpListListener listener, boolean isImage) {
        this.listener = listener;
        this.isImage = isImage;
    }

    @Override
    protected List<Expression> doInBackground(String... strings) {
        String name = strings[0];
        List<Expression> expressionList;
        try {
            if (isImage){
                expressionList = LitePal.where("foldername = ?",name).find(Expression.class);
            }else {
                expressionList = LitePal.select("id","foldername","status","url","desstatus","description").where("foldername = ?",name).find(Expression.class);
            }

            sleep(800);
            return expressionList;
        }catch (Exception e){
            return new ArrayList<>();
        }
    }

    @Override
    protected void onPostExecute(List<Expression> expressions) {
        listener.onFinish(expressions);
    }
}
