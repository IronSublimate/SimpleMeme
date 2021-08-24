package com.ironsublimate.mememanager.activity;

import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.ironsublimate.mememanager.R;
import com.jaeger.library.StatusBarUtil;

public class BaseActivity extends AppCompatActivity {


    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        //setStatusBar();
    }

    protected void setStatusBar() {
        StatusBarUtil.setColor(this, getResources().getColor(R.color.theme_color_primary));
    }



    /**
     * 点击toolbar上的按钮事件
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

}
