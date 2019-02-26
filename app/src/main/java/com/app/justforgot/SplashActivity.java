package com.app.justforgot;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.hanks.htextview.HTextView;
import com.hanks.htextview.HTextViewType;

public class SplashActivity extends AppCompatActivity {

    HTextView hTextView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);


        hTextView = (HTextView) findViewById(R.id.htextdata);
        hTextView.setTypeface(FontManager.getInstance(getAssets()).getFont("fonts/PoiretOne-Regular.ttf"));
// be sure to set custom typeface before setting the animate type, otherwise the font may not be updated.
        hTextView.setAnimateType(HTextViewType.LINE);
        hTextView.animateText("Just Forgot");

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(3000);
                    startActivity(new Intent(SplashActivity.this,HomeActivity.class));
                    overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
                    finish();
                } catch (InterruptedException e) {

                    e.printStackTrace();
                }
            }
        }).start();
    }
}
