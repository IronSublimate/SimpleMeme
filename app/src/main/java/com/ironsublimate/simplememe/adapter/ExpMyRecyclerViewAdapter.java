package com.ironsublimate.simplememe.adapter;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.blankj.ALog;
import com.chad.library.adapter.base.BaseItemDraggableAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.ironsublimate.simplememe.R;
import com.ironsublimate.simplememe.activity.ExpLocalFolderDetailActivity;
import com.ironsublimate.simplememe.bean.Expression;
import com.ironsublimate.simplememe.bean.ExpressionFolder;
import com.ironsublimate.simplememe.callback.TaskListener;
import com.ironsublimate.simplememe.task.DeleteImageTask;
import com.ironsublimate.simplememe.util.UIUtil;

import java.util.List;

import es.dmoral.toasty.Toasty;

/**
 * <pre>
 *     author : hewro
 *     e-mail : ihewro@163.com
 *     time   : 2018/07/05
 *     desc   : 表情管理的主活动的recyclerView的数据适配器
 *     version: 1.0
 * </pre>
 */
public class ExpMyRecyclerViewAdapter extends BaseItemDraggableAdapter<ExpressionFolder, BaseViewHolder> {

    private Activity activity;
    public ExpMyRecyclerViewAdapter(@Nullable List<ExpressionFolder> data, Activity activity) {
        super(R.layout.item_exp_my,data);
        this.activity = activity;
    }

    @Override
    protected void convert(final BaseViewHolder helper, final ExpressionFolder item) {

        //1. 设置每一项的基本问自己信息
        helper.setText(R.id.exp_name,item.getName());
        helper.setText(R.id.exp_num,item.getCount() + "+");
        helper.setText(R.id.add_time,item.getCreateTime());

        //1.1 设置每个表情包的位置权值
        item.setOrderValue(helper.getAdapterPosition() + 1.0);
        item.saveAsync();

        //2. 显示图片
        List<Expression> expressionList = item.getExpressionList(false);
        int min = UIUtil.getMinInt(expressionList.size(),5);
        int imageViewArray[] = new int[]{R.id.image_1,R.id.image_2,R.id.image_3,R.id.image_4,R.id.image_5};

        for (int i =0;i<min;i++){
            helper.getView(imageViewArray[i]).setVisibility(View.VISIBLE);
            if (min == 5){
                helper.getView(R.id.fl_image_5).setVisibility(View.VISIBLE);
            }
            UIUtil.setImageToImageView(expressionList.get(i), (ImageView) helper.getView(imageViewArray[i]));
        }
        //如果表情包数目小于5，则剩余的表情占位不显示
        for (int j = min; j < 5; j++){
            helper.getView(imageViewArray[j]).setVisibility(View.INVISIBLE);
            helper.getView(R.id.fl_image_5).setVisibility(View.INVISIBLE);
        }

        if (expressionList.size() == 0){//如果是空表情，显示占位图片
            ALog.d("名称" + item.getName() + helper.getAdapterPosition());
            helper.getView(R.id.exp_num_1).setVisibility(View.VISIBLE);
            helper.getView(R.id.view_1).setVisibility(View.VISIBLE);
            helper.getView(imageViewArray[0]).setVisibility(View.VISIBLE);
            UIUtil.setImageToImageView(null, (ImageView) helper.getView(imageViewArray[0]));
            helper.setText(R.id.exp_num_1,"空");
        }else {
            helper.getView(R.id.exp_num_1).setVisibility(View.INVISIBLE);
            helper.getView(R.id.view_1).setVisibility(View.INVISIBLE);
        }


        helper.getView(R.id.item_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ExpLocalFolderDetailActivity.actionStart(activity,item.getId(),item.getName(),item.getCreateTime());
            }
        });


        //3. 点击事件
        //删除表情包
        helper.getView(R.id.delete_exp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MaterialDialog.Builder(activity)
                        .title("操作提示")
                        .content("确认删除该表情包吗，你可以通过表情商店再次下载")
                        .positiveText("好")
                        .negativeText("先不删，留着过年")
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                new DeleteImageTask(true, item.getName(), activity, new TaskListener() {
                                    @Override
                                    public void onFinish(Object result) {
                                        Toasty.success(activity,"删除成功", Toast.LENGTH_SHORT).show();
                                        remove(helper.getAdapterPosition());
                                        notifyDataSetChanged();
                                    }
                                }).execute();

                            }
                        })
                        .show();
            }
        });
    }
}
