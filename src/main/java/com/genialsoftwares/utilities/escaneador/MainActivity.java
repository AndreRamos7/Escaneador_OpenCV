package com.genialsoftwares.utilities.escaneador;

import androidx.appcompat.app.AppCompatActivity;

import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
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
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import android.view.WindowManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2, View.OnTouchListener, View.OnClickListener {
    //CameraBridgeViewBase cameraBridgeViewBase;
    private Captura mOpenCvCameraView;

    BaseLoaderCallback baseLoaderCallback;
    int counter = 0;
    SeekBar min_seek_h = null; // initiate the Seek bar
    SeekBar min_seek_s = null; // initiate the Seek bar
    SeekBar min_seek_v = null; // initiate the Seek bar

    SeekBar max_seek_h = null; // initiate the Seek bar
    SeekBar max_seek_s = null; // initiate the Seek bar
    SeekBar max_seek_v = null; // initiate the Seek bar
    private final String TAG = "TSTES";

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                    mOpenCvCameraView.setOnTouchListener(MainActivity.this);
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

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

        mOpenCvCameraView = (Captura) findViewById(R.id.CameraView);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
        //cameraBridgeViewBase.setCameraIndex(1);

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
        if(mOpenCvCameraView != null){
            mOpenCvCameraView.disableView();
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
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

        Scalar lower = new Scalar(min_seek_h_value, min_seek_s_value, min_seek_v_value);
        Scalar upper = new Scalar(max_seek_h_value, max_seek_s_value, max_seek_v_value);

        Mat hrq =  new Mat();
        Point ponto = new Point();
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        MatOfPoint retangulo = new MatOfPoint();
        Size sz = new Size(15, 15);

        Mat frame =  inputFrame.rgba();
        Mat frameOriginal =  frame.clone();
        Mat mascara =  new Mat();
        //Core.flip(frame, frame, 1);
        int h_frame = (int) frame.size().height;
        int w_frame = (int) frame.size().width;
        int a_frame_crop = (h_frame * w_frame);


        Imgproc.cvtColor(frame, frame, Imgproc.COLOR_RGB2HSV);
        Imgproc.blur(frame, frame, sz, new Point(2,2));
        Core.inRange(frame, lower, upper, frame);

        Core.bitwise_not(frame, frame);
        Core.bitwise_and(frame, frame, mascara);

        Imgproc.findContours(frame, contours, hrq, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
        //Utils.matToBitmap (frame, myBitmap);

        for (int contourIdx = 0; contourIdx < contours.size(); contourIdx++) {
            Rect rect = Imgproc.boundingRect(contours.get(contourIdx));
            double area_contours = Imgproc.contourArea(contours.get(contourIdx));
            if(a_frame_crop/12 < area_contours && area_contours < a_frame_crop/4){
                int h = (int) contours.get(contourIdx).size().height;
                int w = (int) contours.get(contourIdx).size().width;
                Point pt1 = new Point(rect.x, rect.y);
                Point pt2 = new Point(rect.x + rect.width, rect.y + rect.height);
                Imgproc.rectangle(frameOriginal, pt1, pt2, new Scalar(255, 255, 0), 3);
            }
        }
        return frameOriginal;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mOpenCvCameraView != null){
            mOpenCvCameraView.disableView();
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


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        Log.i(TAG,"onTouch event");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        String currentDateandTime = sdf.format(new Date());
        String fileName = Environment.getExternalStorageDirectory().getPath() +
                "/sample_picture_" + currentDateandTime + ".jpg";
        mOpenCvCameraView.takePicture(fileName);
        Toast.makeText(this, fileName + " saved", Toast.LENGTH_SHORT).show();
        return false;
    }
}
