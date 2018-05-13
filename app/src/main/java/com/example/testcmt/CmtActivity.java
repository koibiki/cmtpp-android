package com.example.testcmt;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import com.example.opencv.CameraBridgeView;
import com.example.opencv.OpenCvCameraView;
import com.example.view.ScaleViewGroup;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.core.Mat;

public class CmtActivity extends Activity implements CameraBridgeView.CvCameraViewListener {

    private static final String TAG = "CmtActivity";

    private OpenCvCameraView mOpenCvCameraView;

    private final static String sRoot = Environment.getExternalStorageDirectory().getAbsolutePath();

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.d(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };
    private ScaleViewGroup svg;
    private ImageView si;
    private ScaleViewGroup.Box mBox;

    public CmtActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_cmt);

        mOpenCvCameraView = findViewById(R.id.surface_view);

        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);

        mOpenCvCameraView.setCvCameraViewListener(this);

        si = findViewById(R.id.si);
        svg = findViewById(R.id.svg);
        svg.setScalableView(si);
        si.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null) {
            mOpenCvCameraView.disableView();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null) {
            mOpenCvCameraView.disableView();
            clear();
        }
    }

    public void onCameraViewStarted(int width, int height) {
    }

    public void onCameraViewStopped() {
    }

    @Override
    public Mat onCameraFrame(CameraBridgeView.CvCameraViewFrame inputFrame) {
        if (sStatusTracking) {
            Log.w(TAG, "sStatusTracking");
            return new Mat(track(inputFrame.rgba().nativeObj, mBox.left, mBox.top, mBox.width, mBox.height));
        } else {
            return new Mat(rotateFrame(inputFrame.rgba().nativeObj));
        }
    }

    private native long rotateFrame(long frame);

    private native long track(long frame, int left, int top, int width, int height);

    private native void clear();

    public void reset(View view) {
        svg.resetScalableView();
        svg.setScalable(false);
        si.setVisibility(View.INVISIBLE);
    }

    private static final int STATUS_SELECTING = 0;
    private static final int STATUS_INIT = 1;
    private int STATUS_BUTTON = STATUS_INIT;

    private static boolean sStatusTracking = false;

    public void select_obj(View view) {
        Button button = (Button) view;
        if (STATUS_BUTTON == STATUS_INIT) {
            svg.setScalable(true);
            si.setVisibility(View.VISIBLE);
            button.setText("confirm obj");
            confirmObj();
        } else if (STATUS_BUTTON == STATUS_SELECTING) {
            svg.setScalable(false);
            mBox = svg.getBoxCoor();
            sStatusTracking = true;
            si.setVisibility(View.INVISIBLE);
            button.setText("select obj");
        }
        STATUS_BUTTON = (STATUS_BUTTON + 1) % 2;
    }

    private void confirmObj() {
        ScaleViewGroup.Box box = svg.getBoxCoor();
    }

}
