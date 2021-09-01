package com.ironsublimate.simplememe;

import android.content.Intent;
import android.os.Build;
import android.service.quicksettings.TileService;
import androidx.annotation.RequiresApi;

import com.ironsublimate.simplememe.activity.WelcomeActivity;

@RequiresApi(api = Build.VERSION_CODES.N)
public class QuickService extends TileService {
    public QuickService() {
    }

    @Override
    public void onClick() {
        super.onClick();
        Intent intent = new Intent(this,WelcomeActivity.class);
        startActivityAndCollapse(intent);
    }

    @Override
    public void onStartListening() {
        super.onStartListening();
    }

    @Override
    public void onStopListening() {
        super.onStopListening();
    }

}
