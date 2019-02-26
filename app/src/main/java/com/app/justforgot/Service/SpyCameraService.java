package com.app.justforgot.Service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Created by Administrator on 22-02-2017.
 */

public class SpyCameraService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        MyCameraManager mgr = MyCameraManager.getInstance(SpyCameraService.this);
        mgr.takeAPhoto();


        return START_NOT_STICKY;
    }



}
