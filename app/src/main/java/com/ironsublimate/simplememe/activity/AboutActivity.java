package com.ironsublimate.simplememe.activity;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.blankj.ALog;
import com.ironsublimate.simplememe.R;
import com.ironsublimate.simplememe.util.APKVersionCodeUtils;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.dmoral.toasty.Toasty;

public class AboutActivity extends AppCompatActivity {

    @BindView(R.id.imageView)
    ImageView imageView;
    @BindView(R.id.textView)
    TextView textView;
    @BindView(R.id.textView2)
    TextView textView2;
//    @BindView(R.id.imageView2)
//    ImageView imageView2;
//    @BindView(R.id.imageView3)
//    ImageView imageView3;
    @BindView(R.id.loading_tip)
    TextView loadingTip;


    //毫秒
    private long lastClickTime = -1;
    private long thisClickTime = -1;
    private int clickTimes = 0;

    private boolean isPlayed = false;

    public static void actionStart(Activity activity) {
        Intent intent = new Intent(activity, AboutActivity.class);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        ButterKnife.bind(this);

        textView2.setText("v" + APKVersionCodeUtils.getVerName(this));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
