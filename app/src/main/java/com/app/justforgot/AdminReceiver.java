package com.app.justforgot;

import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.app.justforgot.Service.SpyCameraService;


/**
 * Created by Administrator on 10-03-2017.
 */

public class AdminReceiver extends DeviceAdminReceiver {
    @Override
    public void onEnabled(Context ctxt, Intent intent) {
        ComponentName cn=new ComponentName(ctxt, AdminReceiver.class);
        DevicePolicyManager mgr=
                (DevicePolicyManager)ctxt.getSystemService(Context.DEVICE_POLICY_SERVICE);

        mgr.setPasswordQuality(cn,
                DevicePolicyManager.PASSWORD_QUALITY_ALPHANUMERIC);

        onPasswordChanged(ctxt, intent);
    }

    @Override
    public void onPasswordChanged(Context ctxt, Intent intent) {
        DevicePolicyManager mgr=
                (DevicePolicyManager)ctxt.getSystemService(Context.DEVICE_POLICY_SERVICE);
        int msgId;

        if (mgr.isActivePasswordSufficient()) {
            msgId= R.string.compliant;
        }
        else {
            msgId=R.string.not_compliant;
        }


    }

    @Override
    public void onPasswordFailed(Context ctxt, Intent intent) {


        ctxt.startService(new Intent(ctxt,SpyCameraService.class));


    }

    @Override
    public void onPasswordSucceeded(Context ctxt, Intent intent) {
        Toast.makeText(ctxt, "sfsf", Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onLockTaskModeEntering(Context context, Intent intent, String pkg) {
        super.onLockTaskModeEntering(context, intent, pkg);

    }
}