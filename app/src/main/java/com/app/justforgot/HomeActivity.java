package com.app.justforgot;

import android.Manifest;
import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import com.app.justforgot.database.DatabaseClient;
import com.app.justforgot.database.PasscodeData;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.util.List;

import io.ghyeok.stickyswitch.widget.StickySwitch;

import static android.view.Gravity.TOP;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = HomeActivity.class.getSimpleName();
    LinearLayout layoutBottomSheet;
    ScrollView scroll;
    BottomSheetBehavior sheetBehavior;
    StickySwitch sticky_switch;
    ComponentName cn;
    DevicePolicyManager mgr;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor mpref;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        setContentView(R.layout.activity_home);
        mgr=
                (DevicePolicyManager)getSystemService(DEVICE_POLICY_SERVICE);
        cn=new ComponentName(HomeActivity.this, AdminReceiver.class);
        sharedPreferences=getSharedPreferences("mystate",MODE_PRIVATE);
        mpref=sharedPreferences.edit();
        Dexter.withActivity(this)
                .withPermissions(
                        Manifest.permission.RECEIVE_SMS,
                        Manifest.permission.ACCESS_NOTIFICATION_POLICY,
                        Manifest.permission.WAKE_LOCK,
                        Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.READ_CALL_LOG,
                        Manifest.permission.READ_LOGS,
                        Manifest.permission.SET_ALARM,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA,
                        Manifest.permission.CALL_PHONE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        // check if all permissions are granted
                        if (report.areAllPermissionsGranted()) {
                            // do you work now


                            Toast.makeText(HomeActivity.this, "all granted", Toast.LENGTH_SHORT).show();
                        }

                        // check for permanent denial of any permission
                        if (report.isAnyPermissionPermanentlyDenied()) {
                            // permission is denied permenantly, navigate user to app settings
                            showSettingsDialog();
                            Toast.makeText(HomeActivity.this, "no", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }

                }).withErrorListener(new PermissionRequestErrorListener() {
            @Override
            public void onError(DexterError error) {
                Toast.makeText(getApplicationContext(), "Some Error! ", Toast.LENGTH_SHORT).show();
            }
        })

                .check();
        sticky_switch=findViewById(R.id.sticky_switch);
        sticky_switch.setOnSelectedChangeListener(new StickySwitch.OnSelectedChangeListener() {
            @Override
            public void onSelectedChange(StickySwitch.Direction direction, String s) {
                if (sticky_switch.getDirection()== StickySwitch.Direction.RIGHT)
                {
                    mpref.putBoolean("itsclicked",true);
                    mpref.commit();


                        if (mgr.isAdminActive(cn)) {
                            int msgId;

                            if (mgr.isActivePasswordSufficient()) {
                                msgId=R.string.compliant;
                            }
                            else {
                                msgId=R.string.not_compliant;
                            }


                        }
                        else {
                            Intent intent=
                                    new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, cn);
                            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                                    getString(R.string.device_admin_explanation));
                            startActivity(intent);
                        }



                }

                else {
                    mgr.removeActiveAdmin(cn);
                    mpref.putBoolean("itsclicked",false);
                    mpref.commit();

                }
            }
        });

        layoutBottomSheet = findViewById(R.id.bottom_sheet);
        scroll = findViewById(R.id.scroll);
        sheetBehavior = BottomSheetBehavior.from(layoutBottomSheet);

        Button btnsilent = layoutBottomSheet.findViewById(R.id.silent_id);
        Button btnnormal = layoutBottomSheet.findViewById(R.id.normal_id);
        Button divertcall = layoutBottomSheet.findViewById(R.id.divertcall);

        divertcall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LayoutInflater layoutInflaterAndroid = LayoutInflater.from(HomeActivity.this);
                View mView = layoutInflaterAndroid.inflate(R.layout.user_input_dialog_box, null);
                AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(HomeActivity.this);
                alertDialogBuilderUserInput.setView(mView);

                final EditText userInputDialogEditText = (EditText) mView.findViewById(R.id.userInputDialog);
                alertDialogBuilderUserInput
                        .setCancelable(false)
                        .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogBox, int id) {


                                new AsyncData().execute("divert",userInputDialogEditText.getText().toString());

                                // ToDo get user input here
                            }
                        })

                        .setNegativeButton("Cancel",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialogBox, int id) {
                                        dialogBox.cancel();
                                    }
                                });

                AlertDialog alertDialogAndroid = alertDialogBuilderUserInput.create();
                alertDialogAndroid.show();
            }
        });


        btnnormal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LayoutInflater layoutInflaterAndroid = LayoutInflater.from(HomeActivity.this);
                View mView = layoutInflaterAndroid.inflate(R.layout.user_input_dialog_box, null);
                AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(HomeActivity.this);
                alertDialogBuilderUserInput.setView(mView);

                final EditText userInputDialogEditText = (EditText) mView.findViewById(R.id.userInputDialog);
                alertDialogBuilderUserInput
                        .setCancelable(false)
                        .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogBox, int id) {


                                new AsyncData().execute("normal",userInputDialogEditText.getText().toString());

                                // ToDo get user input here
                            }
                        })

                        .setNegativeButton("Cancel",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialogBox, int id) {
                                        dialogBox.cancel();
                                    }
                                });

                AlertDialog alertDialogAndroid = alertDialogBuilderUserInput.create();
                alertDialogAndroid.show();
            }
        });
        btnsilent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                LayoutInflater layoutInflaterAndroid = LayoutInflater.from(HomeActivity.this);
                View mView = layoutInflaterAndroid.inflate(R.layout.user_input_dialog_box, null);
                AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(HomeActivity.this);
                alertDialogBuilderUserInput.setView(mView);

                final EditText userInputDialogEditText = (EditText) mView.findViewById(R.id.userInputDialog);
                alertDialogBuilderUserInput
                        .setCancelable(false)
                        .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogBox, int id) {


                                new AsyncData().execute("silent",userInputDialogEditText.getText().toString());

                                // ToDo get user input here
                            }
                        })

                        .setNegativeButton("Cancel",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialogBox, int id) {
                                        dialogBox.cancel();
                                    }
                                });

                AlertDialog alertDialogAndroid = alertDialogBuilderUserInput.create();
                alertDialogAndroid.show();
            }




        });
        sheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_HIDDEN:
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED: {
                    }
                    break;
                    case BottomSheetBehavior.STATE_COLLAPSED: {
                        scroll.fullScroll(TOP);                    }
                    break;
                    case BottomSheetBehavior.STATE_DRAGGING:
                        break;
                    case BottomSheetBehavior.STATE_SETTLING:
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });
    }



    @Override
    public void onClick(View v) {



    }
    private void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
        builder.setTitle("Need Permissions");
        builder.setMessage("This app needs permission to use this feature. You can grant them in app settings.");
        builder.setPositiveButton("GOTO SETTINGS", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                openSettings();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (sharedPreferences.getBoolean("itsclicked",false)==true){
            sticky_switch.setDirection(StickySwitch.Direction.RIGHT);
        }
        else {
            sticky_switch.setDirection(StickySwitch.Direction.LEFT);
        }
    }

    private void openSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivityForResult(intent, 101);
    }

    public class AsyncData extends AsyncTask<String,Void,String> {



        @Override
        protected String doInBackground(String... strings) {
            String name=strings[0];
            String passwords=strings[1];
            String result = "";

            if (name.equalsIgnoreCase("")){
                Toast.makeText(HomeActivity.this, "Enter Data", Toast.LENGTH_SHORT).show();
            }
            else
            {

                PasscodeData passcodeData=new PasscodeData();
                passcodeData.setName(name);
                passcodeData.setPassword( name+" "+passwords);
                String password= DatabaseClient.getInstance(HomeActivity.this).getAppDatabase().passcodeDao().findPassword(name);
                if (password!=null)
                {
                    Log.d("mmmmm",""+DatabaseClient.getInstance(HomeActivity.this).getAppDatabase().passcodeDao().findPassword(name));
                    //Toast.makeText(HomeActivity.this, ""+DatabaseClient.getInstance(HomeActivity.this).getAppDatabase().passcodeDao().findPassword(name), Toast.LENGTH_SHORT).show();
                    DatabaseClient.getInstance(HomeActivity.this).getAppDatabase().passcodeDao().update(name, name+" "+passwords);
                    result= "successfully updated";
                }
                else {

                    Log.d("mmmmm","first");
//                    Toast.makeText(HomeActivity.this, "first time", Toast.LENGTH_SHORT).show();
                    DatabaseClient.getInstance(HomeActivity.this).getAppDatabase().passcodeDao().insert(passcodeData);

                    result= "successfully inserted";
                }
            }
            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Toast.makeText(HomeActivity.this, ""+s, Toast.LENGTH_SHORT).show();
        }
    }


}

