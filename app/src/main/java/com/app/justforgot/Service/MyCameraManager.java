package com.app.justforgot.Service;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.view.Surface;
import android.view.WindowManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by Administrator on 22-02-2017.
 */
public class MyCameraManager implements Camera.ErrorCallback, Camera.PreviewCallback, Camera.AutoFocusCallback, Camera.PictureCallback {


    private static MyCameraManager camManager;

    private Context mContext;
    private Camera mCamera;
    private SurfaceTexture mSurface;
    private boolean isWorking = false;


public static MyCameraManager getInstance(Context context) {
        if(camManager == null) camManager = new MyCameraManager(context);
        return camManager;
        }

public void takeAPhoto() {
        if(isBackCameraAvailable() && !isWorking) {
        initCamera();
        }
        }

private void initCamera() {
        new AsyncTask<Void, Void, Void>() {

@Override
protected Void doInBackground(Void... voids) {
        try {
        isWorking = true;
        mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
        } catch (RuntimeException e) {

        e.printStackTrace();
        isWorking = false;
        }
        return null;
        }

@Override
protected void onPostExecute(Void aVoid) {
        try {
        if(mCamera != null) {
        mSurface = new SurfaceTexture(123);
        mCamera.setPreviewTexture(mSurface);

        Camera.Parameters params = mCamera.getParameters();
        int angle = 270;
        params.setRotation(angle);

        if (autoFocusSupported(mCamera)) {
        params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        }
            Log.d("kkkkk","taken");

        mCamera.setParameters(params);
        mCamera.setPreviewCallback(MyCameraManager.this);
        mCamera.setErrorCallback(MyCameraManager.this);
        mCamera.startPreview();
        muteSound();
        }
        } catch (IOException e) {

        e.printStackTrace();
        releaseCamera();
        }
        }

        }.execute();
        }

private void releaseCamera() {
        if(mCamera != null) {
        mCamera.release();
        mSurface.release();
        mCamera = null;
        mSurface = null;
        }
        unmuteSound();
        isWorking = false;
        }

@Override
public void onPreviewFrame(byte[] bytes, Camera camera) {
        try {
        if(autoFocusSupported(camera)) {
        mCamera.autoFocus(this);
        } else {
        camera.setPreviewCallback(null);
        camera.takePicture(null, null, this);
        }
        } catch (Exception e) {

        e.printStackTrace();
        releaseCamera();
        }
        }

@Override
public void onAutoFocus(boolean success, Camera camera) {
        if(camera != null) {
        try {
        camera.takePicture(null, null, this);
        mCamera.autoFocus(null);
        } catch (Exception e) {
        e.printStackTrace();
        releaseCamera();
        }
        }
        }

@Override
public void onPictureTaken(byte[] bytes, Camera camera) {
        savePicture(bytes);
        releaseCamera();
        }

@Override
public void onError(int error, Camera camera) {
        switch (error) {
        case Camera.CAMERA_ERROR_SERVER_DIED:

        break;
        case Camera.CAMERA_ERROR_UNKNOWN:

        break;
        case Camera.CAMERA_ERROR_EVICTED:

        break;
default:


        break;
        }
        }

private MyCameraManager(Context context) {
        mContext = context;
        }

private boolean isBackCameraAvailable() {
        boolean result = false;
        if(mContext != null && mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(i, info);
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
        result = true;
        break;
        }
        }
        }
        return result;
        }

private boolean autoFocusSupported(Camera camera) {
        if(camera != null) {
        Camera.Parameters params = camera.getParameters();
        List<String> focusModes = params.getSupportedFocusModes();
        if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
        return true;
        }
        }
        return false;
        }

private void muteSound() {
        if(mContext != null) {
        AudioManager mgr = (AudioManager)mContext.getSystemService(Context.AUDIO_SERVICE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        mgr.adjustStreamVolume(AudioManager.STREAM_SYSTEM, AudioManager.ADJUST_MUTE, 0);
        } else {
        mgr.setStreamMute(AudioManager.STREAM_SYSTEM, true);
        }
        }
        }

private void unmuteSound() {
        if(mContext != null) {
        AudioManager mgr = (AudioManager)mContext.getSystemService(Context.AUDIO_SERVICE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        mgr.adjustStreamVolume(AudioManager.STREAM_SYSTEM, AudioManager.ADJUST_UNMUTE, 0);
        } else {
        mgr.setStreamMute(AudioManager.STREAM_SYSTEM, false);
        }
        }
        }

public int getCameraRotationAngle(int cameraId, Camera camera) {
        int result = 270;
        if(camera != null && mContext != null) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int rotation = ((WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
        int degrees = getRotationAngle(rotation);

        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
        result = (info.orientation + degrees) % 360;
        result = (360 - result) % 360; //compensate mirroring
        }
        }
        return result;
        }

private int getRotationAngle(int rotation) {
        int degrees = 0;
        switch (rotation) {
        case Surface.ROTATION_0:
        degrees = 0;
        break;
        case Surface.ROTATION_90:
        degrees = 90;
        break;
        case Surface.ROTATION_180:
        degrees = 180;
        break;
        case Surface.ROTATION_270:
        degrees = 270;
        break;
        }
        return degrees;
        }

private String savePicture(byte[] bytes) {

        String filepath = null;
        try {
        File pictureFileDir = getDir();
        if (bytes == null) {

        return null;
        }
        if (!pictureFileDir.exists() && !pictureFileDir.mkdirs()) {

        return null;
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddhhmmss");
        String date = dateFormat.format(new Date());
        String photoFile = "JustForgotData" + date + ".jpg";

        filepath = pictureFileDir.getPath() + File.separator + photoFile;

        File pictureFile = new File(filepath);
        FileOutputStream fos = new FileOutputStream(pictureFile);
        fos.write(bytes);
        fos.close();




        } catch (Exception e) {
        e.printStackTrace();
        }

        return filepath;
        }



public static File getDir() {
        File sdDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

        return new File(sdDir, "JustForData");
        }


        }

       
        