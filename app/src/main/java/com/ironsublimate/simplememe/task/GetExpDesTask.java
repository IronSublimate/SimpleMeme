package com.ironsublimate.simplememe.task;

import android.app.Activity;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import com.ironsublimate.simplememe.bean.Expression;
import com.ironsublimate.simplememe.ocr.PaddleOCRNcnn;
import com.ironsublimate.simplememe.util.UIUtil;

/**
 * <pre>
 *     author : ironsublimate
 *     e-mail : ihewro@163.com
 *     time   : 2018/07/11
 *     desc   :generate description words by AI
 *     version: 1.0
 * </pre>
 */
public class GetExpDesTask extends AsyncTask<Expression, Void, Void> {
    private static final String TAG = "Detector";
    private static PaddleOCRNcnn detector;

    static {
        detector = new PaddleOCRNcnn();
        AssetManager assets = UIUtil.getContext().getAssets();
        detector.Init(assets);
    }

    private Activity activity;
    private int count = 0;
    private boolean isRepeat;

    public GetExpDesTask(Activity activity, boolean isRepeat) {
        this.activity = activity;
        this.isRepeat = isRepeat;
    }

    public GetExpDesTask(boolean isRepeat) {
        this.isRepeat = isRepeat;
    }

    //Please check expression.getDesStatus() == 0 before call this function
    static public boolean writeDescription(Expression expression){
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
        expression.setDesStatus(1);
        expression.setDescription(sb.toString());
        expression.save();
        return true;
    }

    @Override
    protected Void doInBackground(Expression... expressions) {
        final Expression expression = expressions[0];
        writeDescription(expression);
//        if (expression.getDesStatus() == 0) {

//        }
//        final File tempFile = new File(GlobalConfig.appTempDirPath + expression.getName());
//        FileUtil.bytesSavedToFile(expression,tempFile);
// 判断是不是识别过了弄到外面去，这里不用
//        if (expression.getDesStatus() == 0) {
//            GeneralBasicParams param = new GeneralBasicParams();
//            param.setDetectDirection(true);
//            param.setImageFile(tempFile);
//            OCR.getInstance(UIUtil.getContext()).recognizeGeneralBasic(param, new OnResultListener<GeneralResult>() {
//                @Override
//                public void onResult(GeneralResult result) {
//                    StringBuilder sb = new StringBuilder();
//                    for (WordSimple wordSimple : result.getWordList()) {
//                        WordSimple word = wordSimple;
//                        sb.append(word.getWords());
//                        sb.append("\n");
//                    }
//                    if (sb.length()>1){
//                        sb.deleteCharAt(sb.length() - 1);
//                    }
//                    expression.setDesStatus(1);
//                    expression.setDescription(sb.toString());
//                    expression.save();
//                    ALog.d(sb);
//                    count ++;
//                    ALog.d("获取文字" + count + "次");
//                    tempFile.delete();
//                }
//
//                @Override
//                public void onError(OCRError error) {
//                    ALog.d(error.getMessage());
//                    //Toasty.info(activity,expression.getName()+"表情的描述自动获取失败，你可以稍后手动识别描述").show();
//                    if (isRepeat){
//                        new GetExpDesTask(isRepeat).execute(expression);
//                    }
//                    count ++;
//                    ALog.d("获取文字" + count + "次");
//                    tempFile.delete();
//                }
//            });
//            expression.setDesStatus(1);
//        expression.setDescription("");
//        expression.save();
//        }
        return null;
    }
}
