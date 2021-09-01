package com.ironsublimate.simplememe.activity;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

import com.ironsublimate.simplememe.R;
import com.ironsublimate.simplememe.callback.GetExpFolderDataListener;
import com.ironsublimate.simplememe.task.AppStartTask;
import com.ironsublimate.simplememe.util.APKVersionCodeUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

public class WelcomeActivity extends AppCompatActivity {

    @BindView(R.id.imageView)
    ImageView imageView;
    @BindView(R.id.textView2)
    TextView versionName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcom);
        ButterKnife.bind(this);
        versionName.setText("v" + APKVersionCodeUtils.getVerName(this));

        new AppStartTask(new GetExpFolderDataListener() {
            @Override
            public void onFinish(String jsonString) {
                finish();
                MainActivity.actionStart(WelcomeActivity.this);
            }
        }).execute();
    }

}
