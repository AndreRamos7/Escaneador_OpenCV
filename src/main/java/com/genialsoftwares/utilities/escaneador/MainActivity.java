package com.genialsoftwares.utilities.escaneador;

import androidx.appcompat.app.AppCompatActivity;

import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2, View.OnClickListener {
    CameraBridgeViewBase cameraBridgeViewBase;
    BaseLoaderCallback baseLoaderCallback;
    int counter = 0;
    SeekBar min_seek_h = null; // initiate the Seek bar
    SeekBar min_seek_s = null; // initiate the Seek bar
    SeekBar min_seek_v = null; // initiate the Seek bar

    SeekBar max_seek_h = null; // initiate the Seek bar
    SeekBar max_seek_s = null; // initiate the Seek bar
    SeekBar max_seek_v = null; // initiate the Seek bar



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Button btn_play = (Button) findViewById(R.id.button);
        max_seek_h = (SeekBar) findViewById(R.id.max_seek_h);
        max_seek_s = (SeekBar) findViewById(R.id.max_seek_s);
        max_seek_v = (SeekBar) findViewById(R.id.max_seek_v);

        min_seek_h = (SeekBar) findViewById(R.id.min_seek_h);
        min_seek_s = (SeekBar) findViewById(R.id.min_seek_s);
        min_seek_v = (SeekBar) findViewById(R.id.min_seek_v);

        cameraBridgeViewBase = (JavaCameraView) findViewById(R.id.CameraView);
        cameraBridgeViewBase.setVisibility(SurfaceView.VISIBLE);
        cameraBridgeViewBase.setCvCameraViewListener(this);
        //cameraBridgeViewBase.setCameraIndex(1);
        baseLoaderCallback = new BaseLoaderCallback(this) {
            @Override
            public void onManagerConnected(int status) {
                super.onManagerConnected(status);
                switch (status){
                    case BaseLoaderCallback.SUCCESS:
                        cameraBridgeViewBase.enableView();
                        break;
                    default:
                        super.onManagerConnected(status);
                        break;
                }
            }
        };
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //btn_play.setOnClickListener(this);
    }
    @Override
    public void onClick(View v) {
        Toast.makeText(getApplicationContext(), "Clikado!", Toast.LENGTH_LONG);
    }

    @Override
    protected void onPause(){
        super.onPause();
        if(cameraBridgeViewBase != null){
            cameraBridgeViewBase.disableView();
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        if(!OpenCVLoader.initDebug()){
            Toast.makeText(getApplicationContext(), "There's a problem, yo!", Toast.LENGTH_SHORT);
        }else{
            baseLoaderCallback.onManagerConnected(baseLoaderCallback.SUCCESS);
        }
    }

    @Override
    public void onCameraViewStarted(int width, int height) {}

    @Override
    public void onCameraViewStopped() {}

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        //Bitmap myBitmap = null;
        int min_seek_h_value = min_seek_h.getProgress();
        int min_seek_s_value = min_seek_s.getProgress();
        int min_seek_v_value = min_seek_v.getProgress();

        int max_seek_h_value = max_seek_h.getProgress();
        int max_seek_s_value = max_seek_s.getProgress();
        int max_seek_v_value = max_seek_v.getProgress();


        Mat frame =  inputFrame.rgba();
        Mat frameOriginal =  frame.clone();
        Mat hrq =  new Mat();
        Point ponto = new Point();
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        MatOfPoint retangulo = new MatOfPoint();
        Size sz = new Size(5, 5);
        Scalar lower = new Scalar(min_seek_h_value, min_seek_s_value, min_seek_v_value);
        Scalar upper = new Scalar(max_seek_h_value, max_seek_s_value, max_seek_v_value);

        Imgproc.GaussianBlur(frame, frame, sz, 1);
        Imgproc.cvtColor(frame, frame, Imgproc.COLOR_RGB2HSV);
        Core.inRange(frame, lower, upper, frame);
        Imgproc.findContours(frame, contours, hrq, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        //Utils.matToBitmap (frame, myBitmap);

        for (int contourIdx = 0; contourIdx < contours.size(); contourIdx++) {
            Rect rect = Imgproc.boundingRect(contours.get(contourIdx));
            double area_contours = Imgproc.contourArea(contours.get(contourIdx));
            if(10 < area_contours && area_contours < 70){
                int h = (int) contours.get(contourIdx).size().height;
                int w = (int) contours.get(contourIdx).size().width;
                Point center = new Point(rect.x, rect.y);
                Imgproc.circle(frameOriginal, center, h, new Scalar(0, 255, 2), 3);
            }



            //Imgproc.drawContours(frame, contours, contourIdx, new Scalar(0, 0, 255), -1);
        }
        /*for (List contour: contours) {

            //Imgproc.circle(frame, (x, y), int(h), (2, 255, 0), 2)
            Imgproc.drawContours(frame, contours, contourIdx, new Scalar(0, 0, 255), -1);
        }*/
        /*if(counter % 2 == 0){
            Core.flip(frame, frame, 1);
            Imgproc.cvtColor(frame, frame, Imgproc.COLOR_RGB2GRAY);
        }*/
        //Core.flip(frame, frame, 0);
        //Imgproc.cvtColor(frame, frame, Imgproc.COLOR_RGB2GRAY);
        //Imgproc.blur(frame, frame, sz);
        //Imgproc.Canny(frame, frame, 80, 100);
        //Imgproc.findContours();


        return frameOriginal;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(cameraBridgeViewBase != null){
            cameraBridgeViewBase.disableView();
        }
    }


    // =================================================================================
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }

    private void hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // Set the content to appear under the system bars so that the
                        // content doesn't resize when the system bars hide and show.
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE
                        // Hide the nav bar and status bar
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    // Shows the system bars by removing all the flags
    // except for the ones that make the content appear under the system bars.
    private void showSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }


}
