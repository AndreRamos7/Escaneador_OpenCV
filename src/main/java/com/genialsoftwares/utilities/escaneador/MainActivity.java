package com.genialsoftwares.utilities.escaneador;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2, View.OnClickListener {
    private CameraBridgeViewBase cameraBridgeViewBase;
    private BaseLoaderCallback baseLoaderCallback;
    private int counter = 0;
    private SeekBar min_seek_h = null; // initiate the Seek bar
    private SeekBar min_seek_s = null; // initiate the Seek bar
    private SeekBar min_seek_v = null; // initiate the Seek bar

    private SeekBar max_seek_h = null; // initiate the Seek bar
    private SeekBar max_seek_s = null; // initiate the Seek bar
    private SeekBar max_seek_v = null; // initiate the Seek bar
    private final String TAG = "Genial";
    private Mat foto  = null;
    private Rect rect_foto = new Rect();
    private ImageView mImageView;
    private int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.READ_CONTACTS)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_READ_CONTACTS);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            Toast.makeText(this.getApplicationContext(), "Essa permissão já foi concedida!", Toast.LENGTH_LONG).show();;
        }

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this.getApplicationContext(), "Sem permissão para acessar a câmera!", Toast.LENGTH_LONG).show();;
        }
        Button btn_play = (Button) findViewById(R.id.button);
        mImageView = (ImageView) findViewById(R.id.imageView);

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
        btn_play.setOnClickListener(this);
        btn_play.animate().rotation(btn_play.getRotation() - 90).start();


    }
    @Override
    public void onClick(View v) {
        Bitmap analyzed = Bitmap.createBitmap(foto.cols(), foto.rows(), Bitmap.Config.RGB_565);
        Utils.matToBitmap(foto, analyzed);
        //SHOW IMAGE
        Bitmap cortado = cortarBitmap(rect_foto.x, rect_foto.y, rect_foto.width, rect_foto.height, analyzed);
        mImageView.setImageBitmap(cortado);
        saveImage(cortado);
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


    private void saveImage(Bitmap finalBitmap) {
        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/genial_images");

        if (!myDir.exists()){
            myDir.mkdir();
            // If you require it to make the entire directory path including parents,
            // use directory.mkdirs(); here instead.
        }

        String  timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fname = timeStamp +".jpg";

        File file = new File(myDir, fname);
        if (file.exists()) file.delete ();
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inScaled = false;
            options.inDither = false;
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            Bitmap source = BitmapFactory.decodeFile(file.getAbsolutePath(), options);

            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);

            Toast.makeText(this.getApplicationContext(), "Salvo nos arquivos!", Toast.LENGTH_LONG).show();
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }




    private Bitmap cortarBitmap(int startX, int startY, int width, int height, Bitmap bmp) {
        Bitmap source = bmp;
        Bitmap resized = Bitmap.createBitmap(source, startX, startY, width, height);
        return resized;
    }


    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
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
        Mat frame_hsv =  new Mat();
        Mat frame_blur =  new Mat();
        Mat mascara =  new Mat();
        Mat mascara_inv =  new Mat();
        Mat mascara_inv_com_img =  new Mat();
        //Core.flip(frame, frame, 1);
        int h_frame = (int) frame.size().height;
        int w_frame = (int) frame.size().width;
        int a_frame_crop = (h_frame * w_frame);

        Imgproc.cvtColor(frame, frame_hsv, Imgproc.COLOR_RGB2HSV);
        Imgproc.blur(frame_hsv, frame_blur, sz, new Point(2,2));
        Core.inRange(frame_blur, lower, upper, mascara);

        Core.bitwise_not(mascara, mascara_inv);
        Core.bitwise_and(frame, frame, mascara_inv_com_img);

        Imgproc.findContours(mascara_inv, contours, hrq, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

        for (int contourIdx = 0; contourIdx < contours.size(); contourIdx++) {
            Rect rect = Imgproc.boundingRect(contours.get(contourIdx));
            double area_contours = Imgproc.contourArea(contours.get(contourIdx));
            if(a_frame_crop/13 < area_contours && area_contours < a_frame_crop/3){
                int h = (int) contours.get(contourIdx).size().height;
                int w = (int) contours.get(contourIdx).size().width;
                Point pt1 = new Point(rect.x, rect.y);
                Point pt2 = new Point(rect.x + rect.width, rect.y + rect.height);
                Imgproc.rectangle(frame, pt1, pt2, new Scalar(255, 255, 255), 3);
                rect_foto = rect;
            }
        }
        Imgproc.putText(frame, "Captured: " + frame.size(), new Point(frame.cols() / 3 * 2, frame.rows() * 0.1),
                Core.FONT_HERSHEY_SIMPLEX, 1.0, new Scalar(255, 255, 0));

        foto = frame;
        //frame.release();
        frame_blur.release();
        frame_hsv.release();
        mascara.release();
        mascara_inv.release();
        mascara_inv_com_img.release();

        return frame;
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
