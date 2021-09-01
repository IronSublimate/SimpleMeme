package com.ironsublimate.simplememe.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.core.content.FileProvider;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;
import com.ironsublimate.simplememe.GlobalConfig;
import com.ironsublimate.simplememe.MySharePreference;
import com.ironsublimate.simplememe.R;
import com.ironsublimate.simplememe.bean.EventMessage;
import com.ironsublimate.simplememe.bean.Expression;
import com.ironsublimate.simplememe.callback.GetExpImageListener;
import com.ironsublimate.simplememe.callback.SaveImageToGalleryListener;
import com.ironsublimate.simplememe.task.GetExpImageTask;
import com.ironsublimate.simplememe.task.SaveImageToGalleryTask;
import com.ironsublimate.simplememe.util.FileUtil;
import com.ironsublimate.simplememe.util.ShareUtil;
import com.ironsublimate.simplememe.util.ToastUtil;
import com.ironsublimate.simplememe.util.UIUtil;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;

/**
 * <pre>
 *     author : hewro
 *     e-mail : ihewro@163.com
 *     time   : 2018/07/03
 *     desc   : 在MaterialDialog 基础上封装了一层，用来显示单个表情包
 *     version: 1.0
 * </pre>
 */
public class ExpImageDialog extends MaterialDialog{


    //自定义布局
    private ImageView ivExpression;
    private TextView tvExpression;
    private View save;
    private View share;
    private View delete;
    View timShare;
    private View weChatShare;
    private View qqShare;//qq分享
    private View love;//输出一句撩人的话
    private View inputView;
    private EditText inputText;
    private View getAuto;
    private View saveToDatabase;

    private final Builder builder;
    private Activity activity;//显示该对话框的活动
    private Fragment fragment;//显示该对话框的碎片
    private int position;

    private Expression expression;


    private String[] loves = new String[]{
            "每次看到你的时候 我都觉得 呀我要流鼻血啦 可是 我从来没留过鼻血 我只会流眼泪",
            "你可知 你是我青春年少时义无反顾的梦",
            "请记住我",
            "时间将它磨得退色，又被岁月添上新的柔光，以至于如今的我再已无法辨别当时的心情。那就当是一见钟情吧。",
            "晚来天欲雪，能饮一杯无。",
            "当时明月在，曾照彩云归",
            "都崭新，都暗淡，都独立，都有明天。"
    };



    ExpImageDialog(Builder builder) {
        super(builder);
        this.builder = builder;
        initData();
        initView();//初始化自定义布局
        initListener();//注册监听器
    }

    private void initData(){
        this.activity = this.builder.activity;
        this.fragment = this.builder.fragment;
        this.position = this.builder.position;
    }


    /**
     * 获取到最新的控件数据
     * @param expression
     */
    public void setImageData(Expression expression){
        this.expression = expression;
    }

    /**
     * 更新对话框的界面数据
     */
    private void updateUI(){
        if (expression.getStatus() == 1){//本地图片，显示图片识别框
            inputView.setVisibility(View.VISIBLE);
            if (new File(GlobalConfig.appDirPath + expression.getFolderName() + "/" + expression.getName()).exists()){
                delete.setVisibility(View.VISIBLE);
                save.setVisibility(View.GONE);
            }else {
                save.setVisibility(View.VISIBLE);
                delete.setVisibility(View.GONE);
            }
            if (expression.getDesStatus() == 1){
                inputText.setText(expression.getDescription());
            }else {
                inputText.setText("");
            }
        } else if (expression.getStatus() == 3){
            delete.setVisibility(View.VISIBLE);
            save.setVisibility(View.GONE);
        } else if (expression.getStatus() == 2){
            delete.setVisibility(View.GONE);
            inputView.setVisibility(View.GONE);
        }
        UIUtil.setImageToImageView(expression,ivExpression);
        tvExpression.setText(expression.getName());
    }

    @Override
    public void show() {
        updateUI();
        super.show();

        initTapView();

    }

    private void initTapView(){
        int leftButton;
        String title;
        String des;
        if (new File(GlobalConfig.appDirPath + expression.getFolderName() + "/" + expression.getName()).exists()){
            leftButton = R.id.delete_image;
            title = "删除图片";
            des = "点击删除该图片在本地的文件\n\n但你仍然可以离线使用";
        }else {
            leftButton = R.id.save_image;
            title = "保存到本地";
            des = "点击保存到本地以文件形式存储";
        }

        List<TapTarget> tapTargets = new ArrayList<>();

        TapTarget imageDesTarget;
        if (expression.getStatus() == 2){//网络图片
            imageDesTarget = null;
        }else {
            imageDesTarget = TapTarget.forView(findViewById(R.id.input_view), "图片描述区", "填写图片描述可以帮助你更快的搜索到相应表情\n\n你可以使用自动识别功能自动识别图片中的文字")
                    .textTypeface(Typeface.SANS_SERIF) //指定字体
                    .drawShadow(true)
                    .cancelable(false)
                    .transparentTarget(true)
                    .targetCircleColor(android.R.color.black)//内圈的颜色
                    .titleTextColor(R.color.text_primary_dark)
                    .descriptionTextColor(R.color.text_secondary_dark).id(3);
        }

        if (MySharePreference.getUserUsedStatus("isOpenTheImageDialog") == 0) {
            TapTarget leftButtonTarget = TapTarget.forView(findViewById(leftButton), title, des)
                    .textTypeface(Typeface.SANS_SERIF) //指定字体
                    .drawShadow(true).cancelable(false).tintTarget(true)//
                    .tintTarget(true)
                    .targetCircleColor(android.R.color.black)//内圈的颜色
                    .titleTextColor(R.color.text_primary_dark)
                    .descriptionTextColor(R.color.text_secondary_dark).id(1);

            TapTarget shareTarget = TapTarget.forView(findViewById(R.id.share_function), "分享工具栏", "轻松分享到微信、QQ、Tim社交平台\n\n使用系统内置分享功能甚至可以分享到任何地方")
                    .textTypeface(Typeface.SANS_SERIF) //指定字体
                    .drawShadow(true)
                    .cancelable(false)
                    .tintTarget(true)
                    .targetCircleColor(android.R.color.black)//内圈的颜色
                    .titleTextColor(R.color.text_primary_dark)
                    .descriptionTextColor(R.color.text_secondary_dark).id(2);

            tapTargets.add(leftButtonTarget);
            tapTargets.add(shareTarget);
            if (leftButton == R.id.delete_image){
                MySharePreference.getUserUsedStatus("isDeleteImage");
            }else {
                MySharePreference.getUserUsedStatus("isSaveImage");
            }

            if (imageDesTarget != null){
                tapTargets.add(imageDesTarget);
                MySharePreference.getUserUsedStatus("isImageDes");
            }

            new TapTargetSequence(this)
                    .targets(tapTargets)
                    .listener(new TapTargetSequence.Listener() {
                        @Override
                        public void onSequenceFinish() {

                        }

                        @Override
                        public void onSequenceStep(TapTarget lastTarget, boolean targetClicked) {
                            switch (lastTarget.id()){
                                case 1:
                                    break;
                                case 2:
                                    break;
                                case 3:
                                    break;
                            }
                        }

                        @Override
                        public void onSequenceCanceled(TapTarget lastTarget) {

                        }
                    })
                    .start();
        }else {//已经不是第一次打开图片弹窗，但是删除按钮或者保存按钮是第一次使用
            tapTargets.clear();
            if (MySharePreference.getUserUsedStatus("isSaveImage") == 0 || MySharePreference.getUserUsedStatus("isDeleteImage") == 0){
                TapTarget buttonTarget = TapTarget.forView(findViewById(leftButton), title, des)
                        .textTypeface(Typeface.SANS_SERIF) //指定字体
                        .drawShadow(true).cancelable(false).tintTarget(true)//
                        .tintTarget(true)
                        .targetCircleColor(android.R.color.black)//内圈的颜色
                        .titleTextColor(R.color.text_primary_dark)
                        .descriptionTextColor(R.color.text_secondary_dark).id(1);
                tapTargets.add(buttonTarget);

            }
            if (imageDesTarget != null){
                if (MySharePreference.getUserUsedStatus("isImageDes") == 0){
                    tapTargets.add(imageDesTarget);
                }
            }


            if (tapTargets.size() > 0){
                new TapTargetSequence(this).targets(tapTargets).start();
            }


        }
    }

    private void initView(){

        View view = getCustomView();
        assert view != null;
        ivExpression = view.findViewById(R.id.expression_image);
        tvExpression = view.findViewById(R.id.expression_name);
        save = view.findViewById(R.id.save_image);
        delete = view.findViewById(R.id.delete_image);
        share = view.findViewById(R.id.share);
        timShare = view.findViewById(R.id.tim_share);
        weChatShare = view.findViewById(R.id.weChat_share);
        qqShare = view.findViewById(R.id.qq_share);
        love = view.findViewById(R.id.love);

        inputView = view.findViewById(R.id.input_view);
        inputText = view.findViewById(R.id.input_text);

        getAuto = view.findViewById(R.id.auto_get);
        saveToDatabase = view.findViewById(R.id.save_to_database);

    }

    private void initListener(){

//        //保存图片到本地
//        save.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                new SaveImageToGalleryTask(new SaveImageToGalleryListener() {
//                    @Override
//                    public void onFinish(Boolean result) {
//                        if (result){
//                            Toasty.success(UIUtil.getContext(),"已保存到" +GlobalConfig.appDirPath + expression.getFolderName() + "/" + expression.getName(), Toast.LENGTH_SHORT).show();
//                            FileUtil.updateMediaStore(activity,GlobalConfig.appDirPath + expression.getFolderName() + "/" + expression.getName());
//                        }else {
//                            Toasty.error(activity,"保存失败，请检查是否允许应用获取存储权限",Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                },activity).execute(expression);
//            }
//        });

        //调用系统分享
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new SaveImageToGalleryTask(new SaveImageToGalleryListener() {
                    @Override
                    public void onFinish(Boolean result) {
                        if (result){
                            FileUtil.updateMediaStore(activity,GlobalConfig.appDirPath + expression.getFolderName() + "/" + expression.getName());
//                            File filePath = new File(GlobalConfig.appDirPath + expression.getFolderName() + "/" + expression.getName());
                            File filePath = new File(expression.getUrl());
                            Log.e("filepath",filePath.getAbsolutePath());
                            Intent shareIntent = new Intent();
                            shareIntent.setAction(Intent.ACTION_SEND);
                            Uri imageUri = FileProvider.getUriForFile(
                                    activity,
                                    UIUtil.getContext().getPackageName() + ".fileprovider",
                                    filePath);
//                            Uri imageUri = expression.getUrl();

                            shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
                            shareIntent.setType("image/*");
                            activity.startActivity(Intent.createChooser(shareIntent, "分享到"));
                        }
                    }
                },activity).execute(expression);

            }
        });

        //调用tim分享
        timShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new SaveImageToGalleryTask(new SaveImageToGalleryListener() {
                    @Override
                    public void onFinish(Boolean result) {
                        if (result){
                            FileUtil.updateMediaStore(activity,GlobalConfig.appDirPath + expression.getFolderName() + "/" + expression.getName());
                            File filePath = new File(GlobalConfig.appDirPath + expression.getFolderName() + "/" + expression.getName());
                            Log.e("filepath", filePath.getAbsolutePath());
                            Intent shareIntent = new Intent();
                            shareIntent.setAction(Intent.ACTION_SEND);
                            Uri imageUri = FileProvider.getUriForFile(
                                    activity,
                                    UIUtil.getContext().getPackageName() + ".fileprovider",
                                    filePath);

                            ShareUtil.shareTimFriend("title", "content", ShareUtil.DRAWABLE, imageUri);
                        }
                    }
                },activity).execute(expression);
            }
        });

        //调用QQ分享
        qqShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                new SaveImageToGalleryTask(new SaveImageToGalleryListener() {
                    @Override
                    public void onFinish(Boolean result) {
                        if (result){
                            FileUtil.updateMediaStore(activity,GlobalConfig.appDirPath + expression.getFolderName() + "/" + expression.getName());
                            File filePath = new File(GlobalConfig.appDirPath + expression.getFolderName() + "/" + expression.getName());
                            Log.e("filepath", filePath.getAbsolutePath());
                            Intent shareIntent = new Intent();
                            shareIntent.setAction(Intent.ACTION_SEND);
                            Uri imageUri = FileProvider.getUriForFile(
                                    activity,
                                    UIUtil.getContext().getPackageName() + ".fileprovider",
                                    filePath);

                            ShareUtil.shareQQFriend("title", "content", ShareUtil.DRAWABLE, imageUri);
                        }
                    }
                },activity).execute(expression);
            }
        });

        //调用微信分享
        weChatShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new SaveImageToGalleryTask(new SaveImageToGalleryListener() {
                    @Override
                    public void onFinish(Boolean result) {
                        if (result){
                            FileUtil.updateMediaStore(activity,GlobalConfig.appDirPath + expression.getFolderName() + "/" + expression.getName());
                            File filePath = new File(GlobalConfig.appDirPath + expression.getFolderName() + "/" + expression.getName());
                            Log.e("filepath", filePath.getAbsolutePath());
                            Intent shareIntent = new Intent();
                            shareIntent.setAction(Intent.ACTION_SEND);
                            Uri imageUri = FileProvider.getUriForFile(
                                    activity,
                                    UIUtil.getContext().getPackageName() + ".fileprovider",
                                    filePath);

                            ShareUtil.shareWeChatFriend("title","content",ShareUtil.DRAWABLE,imageUri);
                        }
                    }
                },activity).execute(expression);
            }
        });


        //点击爱心
        love.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FileUtil.updateMediaStore(activity,GlobalConfig.appDirPath + expression.getFolderName() + "/" + expression.getName());
                ((ImageView)love).setImageDrawable(new IconicsDrawable(activity)
                        .icon(GoogleMaterial.Icon.gmd_favorite)
                        .color(Color.RED)
                        .sizeDp(24));
                int position = (int)(Math.random()*(loves.length - 1));
                ToastUtil.showMessageShort(loves[position]);
            }
        });

        getAuto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new GetExpImageTask(new GetExpImageListener() {
                    @Override
                    public void onFinish(Expression expression) {
//                        final File tempFile = new File(GlobalConfig.appDirPath + expression.getName());
//                        FileUtil.bytesSavedToFile(expression.getImage(),tempFile);

//                        GeneralBasicParams param = new GeneralBasicParams();
//                        param.setDetectDirection(true);
//                        param.setImageFile(tempFile);
                        final MaterialDialog dialog = new MaterialDialog.Builder(activity)
                                .progress(true, 0)
                                .progressIndeterminateStyle(true)
                                .show();
//                        OCR.getInstance(activity).recognizeGeneralBasic(param, new OnResultListener<GeneralResult>() {
//                            @Override
//                            public void onResult(GeneralResult result) {
//                                StringBuilder sb = new StringBuilder();
//                                for (WordSimple wordSimple : result.getWordList()) {
//                                    WordSimple word = wordSimple;
//                                    sb.append(word.getWords());
//                                    sb.append("\n");
//                                }
//                                if (sb.length()>1){
//                                    sb.deleteCharAt(sb.length() - 1);
//                                }
//                                inputText.setText(sb);
//                                dialog.dismiss();
//                                tempFile.delete();
//                            }
//
//                            @Override
//                            public void onError(OCRError error) {
//                                Toasty.error(activity,error.getMessage()).show();
//                                dialog.dismiss();
//                                ALog.d(error.getMessage());
//                                tempFile.delete();
//                            }
//                        });
                    }
                }).execute(expression.getId());


            }
        });

        saveToDatabase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new GetExpImageTask(new GetExpImageListener() {
                    @Override
                    public void onFinish(Expression expression) {
                        expression.setDesStatus(1);
                        expression.setDescription(inputText.getText().toString());
                        expression.save();
                        //发个消息让首页更新数据，第一个数据是内容，第二个是表情包名称，第三个是当前弹框的位置，1表示在localdetail,2表示在首页
                        EventBus.getDefault().post(new EventMessage(EventMessage.DESCRIPTION_SAVE,inputText.getText().toString(),expression.getFolderName(),String.valueOf(position)));
                        Toasty.success(activity,"保存表情描述成功",Toast.LENGTH_SHORT).show();
                    }
                },true).execute(expression.getId());

            }
        });


        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File file = new File(GlobalConfig.appDirPath + expression.getFolderName() + "/" + expression.getName());
                if (file.exists()){
                    //删除
                    file.delete();
                    Toasty.success(activity,"删除成功").show();
                }
            }
        });

    }

    public static class Builder extends MaterialDialog.Builder {

        private Activity activity;//显示该对话框的活动
        private Fragment fragment;//显示该对话框的碎片
        private int position;//1表示在webdetail,2表示在localdetail,3表示在首页

        public Builder(@NonNull Context context) {
            super(context);
        }

        public Builder setContext(Activity activity,Fragment fragment,int position){
            this.activity = activity;
            this.fragment = fragment;
            this.position = position;
            return this;
        }


        @Override
        public ExpImageDialog build() {
            this.customView(R.layout.item_show_expression, false);
            return new ExpImageDialog(this);
        }
    }

}
