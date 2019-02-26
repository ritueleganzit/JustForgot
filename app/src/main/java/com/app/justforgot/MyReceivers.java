package com.app.justforgot;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.telephony.SmsMessage;
import android.util.Log;

import com.app.justforgot.database.DatabaseClient;

/**
 * Created by eleganz on 2/1/19.
 */

public class MyReceivers  extends BroadcastReceiver {
    Bundle bundle;
    String phone, message;
    String silentpassword,generalpassword,divert;
    @Override
    public void onReceive(final Context context, Intent intent) {

        bundle = intent.getExtras();

        new notifyAsyncTask().execute(context);


            }

    private class notifyAsyncTask extends AsyncTask<Context,Void,Void>{
        @SuppressLint("MissingPermission")
        @Override
        protected Void doInBackground(Context... voids) {
            silentpassword= DatabaseClient.getInstance(voids[0]).getAppDatabase().passcodeDao().findPassword("silent");
            generalpassword= DatabaseClient.getInstance(voids[0]).getAppDatabase().passcodeDao().findPassword("normal");
            divert= DatabaseClient.getInstance(voids[0]).getAppDatabase().passcodeDao().findPassword("divert");
            Log.d("kkk","--"+silentpassword);
            Log.d("kkk","--"+generalpassword);
            Log.d("kkk","--"+divert);
            if (bundle != null) {
                Object[] objects = (Object[]) bundle.get("pdus");
                for (int i = 0; i < objects.length; i++) {
                    SmsMessage currentmsg = SmsMessage.createFromPdu((byte[]) objects[i]);
                    phone = currentmsg.getDisplayOriginatingAddress();
                    message = currentmsg.getDisplayMessageBody();
                    Log.d("kkk","111");

                    if (message.equalsIgnoreCase(silentpassword)) {
                        AudioManager am = (AudioManager) voids[0].getSystemService(Context.AUDIO_SERVICE);
                        am.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                        Log.d("kkk","ok");
                    }

                    if(message.equalsIgnoreCase(generalpassword))
                    {
                        AudioManager audioManager = (AudioManager) voids[0].getSystemService(Context.AUDIO_SERVICE);
                        audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                    }

                    if (message.equalsIgnoreCase(divert)) {
                        String callForwardString = "**21*" + phone + "#";


                        Intent intentCallForward = new Intent(Intent.ACTION_CALL);
                        Uri uri2 = Uri.fromParts("tel", callForwardString, "#");
                        intentCallForward.setData(uri2);
                        intentCallForward.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        if (ActivityCompat.checkSelfPermission(voids[0], Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                            return null;
                        }
                        voids[0].startActivity(intentCallForward);

                    }
                    if (message.equalsIgnoreCase("stop "+divert)) {

                        String callForwardStringStop = "##21#";

                        Intent intentCallForward = new Intent(Intent.ACTION_CALL);
                        Uri uri2 = Uri.fromParts("tel", callForwardStringStop, "#");
                        intentCallForward.setData(uri2);
                        intentCallForward.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        voids[0].startActivity(intentCallForward);

                    }

                }

            }

            return null;
        }
    }
}
