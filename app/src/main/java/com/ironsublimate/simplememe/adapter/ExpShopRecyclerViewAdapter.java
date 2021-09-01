package com.ironsublimate.simplememe.adapter;

import android.app.Activity;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.InputType;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.blankj.ALog;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.ironsublimate.simplememe.R;
import com.ironsublimate.simplememe.activity.ExpWebFolderDetailActivity;
import com.ironsublimate.simplememe.bean.ExpressionFolder;
//import com.ironsublimate.simplememe.http.HttpUtil;
import com.ironsublimate.simplememe.util.UIUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import es.dmoral.toasty.Toasty;

/**
 * <pre>
 *     author : hewro
 *     e-mail : ihewro@163.com
 *     time   : 2018/07/02
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public class ExpShopRecyclerViewAdapter extends BaseQuickAdapter<ExpressionFolder, BaseViewHolder> {
    @BindView(R.id.exp_name)
    TextView expName;
    @BindView(R.id.image_1)
    ImageView image1;
    @BindView(R.id.image_2)
    ImageView image2;
    @BindView(R.id.image_3)
    ImageView image3;
    @BindView(R.id.image_4)
    ImageView image4;
    @BindView(R.id.image_5)
    ImageView image5;
    @BindView(R.id.exp_num)
    TextView expNum;
    @BindView(R.id.owner_name)
    TextView ownerName;

    private Activity activity = null;
    public ExpShopRecyclerViewAdapter(@Nullable List<ExpressionFolder> data, Activity activity) {
        super(R.layout.item_exp_shop, data);
        this.activity = activity;
    }

    private int downloadCount = 0;//合集已经下载的数目
    private int downloadAllCount;//要下载的合集数目

    List<ExpressionFolder> expressionFolderList = new ArrayList<>();
    private ExpressionFolder expressionFolder;

    @Override
    protected void convert(BaseViewHolder helper, final ExpressionFolder item) {
        UIUtil.setImageToImageView(2,item.getOwnerAvatar(), (ImageView) helper.getView(R.id.owner_avatar));
        helper.setText(R.id.exp_name,item.getName());
        helper.setText(R.id.exp_num,item.getCount() + "+");
        helper.setText(R.id.owner_name,item.getOwner());

        int imageViewArray[] = new int[]{R.id.image_1,R.id.image_2,R.id.image_3,R.id.image_4,R.id.image_5};

        if (item.getName().contains("密码") || item.getName().contains("污污")){
            helper.getView(R.id.download_exp).setVisibility(View.GONE);//先隐藏，答对问题才能显示该按钮
            helper.getView(R.id.item_view).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    zoreChallenge(item);
                }
            });

            for (int j = 0;j< 4; j++){
                helper.getView(imageViewArray[j]).setVisibility(View.GONE);
            }

            ((ImageView)helper.getView(imageViewArray[4])).setImageResource(R.drawable.loading);

        }else {//普通表情包
            int num = 0;
            if (item.getExpressionList().size()<5){
                num = item.getExpressionList().size();
            }else {
                num = 5;
            }

            for (int i =0;i<num;i++){
                helper.getView(imageViewArray[i]).setVisibility(View.VISIBLE);
                UIUtil.setImageToImageView(2,item.getExpressionList().get(i).getUrl(), (ImageView) helper.getView(imageViewArray[i]));
            }
            //如果表情包数目小于5，则剩余的表情占位不显示
            for (int j = num;j< 5; j++){
                helper.getView(imageViewArray[j]).setVisibility(View.INVISIBLE);
                helper.getView(R.id.fl_image_5).setVisibility(View.INVISIBLE);
            }

            if (item.getCount() == 0){
                helper.getView(imageViewArray[0]).setVisibility(View.VISIBLE);
                ((ImageView)helper.getView(imageViewArray[0])).setImageResource(R.drawable.empty);
                helper.getView(imageViewArray[0]).setPadding(20,20,20,20);
            }

            helper.getView(R.id.item_view).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ExpWebFolderDetailActivity.actionStart(activity,item.getDir(),item.getName(),item.getCount(),item.getOwner(),item.getOwnerAvatar());
                }
            });



        }

        //下载表情包
//        helper.getView(R.id.download_exp).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                /*
//                1. 将文件下载到本地
//                2. 下载的图片信息存储到数据库中
//                3. 更新图库以便显示出下载的图片
//                */
//
//                final MaterialDialog getInfoDialog  = new MaterialDialog.Builder(activity)
//                        .title("操作通知")
//                        .content("正在获取表情包数据……")
//                        .progress(true, 0)
//                        .progressIndeterminateStyle(true)
//                        .show();
//                HttpUtil.getExpressionList(item.getDir(), 1, 99999999,item.getName(), new Callback<List<Expression>>() {//获取该目录下的所有表情包，不分页
//                    @Override
//                    public void onResponse(Call<List<Expression>> call, Response<List<Expression>> response) {
//                        if (response.isSuccessful()){
//                            getInfoDialog.dismiss();
//                            Toasty.success(activity,"获取表情包数据成功",Toast.LENGTH_SHORT).show();
//                            DownloadImageTask task = new DownloadImageTask(response.body(),item.getName(),item.getCount(),activity);
//                            task.execute();
//                        }
//                    }
//                    @Override
//                    public void onFailure(Call<List<Expression>> call, Throwable t) {
//                        //获取表情包合集失败，终止下载
//                        Toasty.error(activity,"获取表情包数据失败",Toast.LENGTH_SHORT).show();
//                    }
//                });
//
//            }
//        });
    }



    /**
     * 第0层挑战
     */
    private void zoreChallenge(final ExpressionFolder item){
        MaterialDialog dialog = new MaterialDialog.Builder(activity)
                .title("你确定要进入吗？")
                .content("这里的内容不可描述🙈")
                .positiveText("当然")
                .negativeText("那我就不看了")
                .cancelable(false)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        firstChallenge(item);
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        detainBeforeExit(item);
                    }
                })
                .show();
    }

    /**
     * 第1层挑战
     */
    private void firstChallenge(final ExpressionFolder item){
        MaterialDialog dialog = new MaterialDialog.Builder(activity)
                .content("那输入密码吧（你可以询问软件作者😏）")
                .inputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)
                .input("密码", "", new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(MaterialDialog dialog, CharSequence input) {
                        // Do something
                        if (Objects.equals(dialog.getInputEditText().getText().toString(), "1998")){
                            Toasty.success(activity,"密码正确😏",Toast.LENGTH_SHORT).show();
                            ExpWebFolderDetailActivity.actionStart(activity,item.getDir(),item.getName(),item.getCount(),item.getOwner(),item.getOwnerAvatar());
                        }else {
                            ALog.d(dialog.getInputEditText().getText());
                            Toasty.error(activity,"密码错误😏",Toast.LENGTH_SHORT).show();
                        }
                    }
                }).show();
    }

    /**
     * 退出前的挽留
     */
    private void detainBeforeExit(final ExpressionFolder item){
        new MaterialDialog.Builder(activity)
                .content("真的不看吗？")
                .positiveText("真的")
                .negativeText("那我就免为其难的看看吧（偷瞄~~( ﹁ ﹁ ) ~~~）")
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        firstChallenge(item);
                    }
                })
                .cancelable(false)
                .show();
    }

}
