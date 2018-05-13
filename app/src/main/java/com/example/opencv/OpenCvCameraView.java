package com.example.opencv;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup;

import org.opencv.BuildConfig;
import org.opencv.android.JavaCameraView;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.List;

/**
 * Created by chengli on 17-7-3.
 */

public class OpenCvCameraView extends CameraBridgeView implements Camera.PreviewCallback {

    private static final String TAG = OpenCvCameraView.class.getName();

    private byte mBuffer[];

    private Mat[] mFrameChain;

    private int mChainIdx = 0;

    private Thread mThread;

    private volatile boolean mStopThread;

    protected Camera mCamera;

    protected OpenCvCameraFrame[] mCameraFrame;

    private SurfaceTexture mSurfaceTexture;

    private boolean mCameraFrameReady;

    public OpenCvCameraView(Context context) {
        this(context, null);
    }

    public OpenCvCameraView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public OpenCvCameraView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private class OpenCvCameraFrame implements CvCameraViewFrame {

        @Override
        public Mat gray() {
            return mYuvFrameData.submat(0, mHeight, 0, mWidth);
        }

        @Override
        public Mat rgba() {
            Imgproc.cvtColor(mYuvFrameData, mRgba, Imgproc.COLOR_YUV2RGBA_NV21, 4);
            return mRgba;
        }

        public OpenCvCameraFrame(Mat Yuv420sp, int width, int height) {
            super();
            mWidth = width;
            mHeight = height;
            mYuvFrameData = Yuv420sp;
            mRgba = new Mat();
        }

        public void release() {
            mRgba.release();
        }

        private Mat mYuvFrameData;

        private Mat mRgba;

        private int mWidth;

        private int mHeight;
    }

    private boolean initializeCamera(int width, int height) {
        Log.d(TAG, "Initialize java camera");
        boolean result = true;
        synchronized (this) {
            mCamera = null;

            try {
                mCamera = Camera.open(1);
                mCamera.setDisplayOrientation(90);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (mCamera == null) {
                return false;
            }

            try {
                Camera.Parameters params = mCamera.getParameters();
                Log.d(TAG, "getSupportedPreviewSizes()");

                List<Camera.Size> sizes = params.getSupportedPreviewSizes();

                if (sizes != null) {
                    Size frameSize = calculateCameraFrameSize(sizes,
                            new JavaCameraView.JavaCameraSizeAccessor(), width, height);

                    params.setPreviewFormat(ImageFormat.NV21);
                    Log.d(TAG, "Set preview size to " + ((int) frameSize.width) + "x"
                            + ((int) frameSize.height));
                    params.setPreviewSize((int) frameSize.width, (int) frameSize.height);

                    if (!android.os.Build.MODEL.equals("GT-I9100")) {
                        params.setRecordingHint(true);
                    }

                    List<String> FocusModes = params.getSupportedFocusModes();
                    if (FocusModes != null && FocusModes
                            .contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
                        params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
                    }

                    mCamera.setParameters(params);
                    params = mCamera.getParameters();

                    mFrameWidth = params.getPreviewSize().width;
                    mFrameHeight = params.getPreviewSize().height;
                    AllocateCache();

                    if ((getLayoutParams().width == ViewGroup.LayoutParams.MATCH_PARENT) && (
                            getLayoutParams().height == ViewGroup.LayoutParams.MATCH_PARENT)) {

                        mScale = Math.min(((float) width) / mFrameHeight,
                                ((float) height) / mFrameWidth);
                    } else {
                        mScale = 0;
                    }
                    enableFpsMeter();
                    if (mFpsMeter != null) {
                        mFpsMeter.setResolution(mFrameWidth, mFrameHeight);
                    }

                    int size = mFrameWidth * mFrameHeight;
                    size = size * ImageFormat.getBitsPerPixel(params.getPreviewFormat()) / 8;
                    mBuffer = new byte[size];

                    mCamera.addCallbackBuffer(mBuffer);
                    mCamera.setPreviewCallbackWithBuffer(this);

                    mFrameChain = new Mat[2];
                    mFrameChain[0] = new Mat(mFrameHeight + (mFrameHeight / 2), mFrameWidth,
                            CvType.CV_8UC1);
                    mFrameChain[1] = new Mat(mFrameHeight + (mFrameHeight / 2), mFrameWidth,
                            CvType.CV_8UC1);

                    mCameraFrame = new OpenCvCameraFrame[2];
                    mCameraFrame[0] = new OpenCvCameraFrame(mFrameChain[0],
                            mFrameWidth, mFrameHeight);
                    mCameraFrame[1] = new OpenCvCameraFrame(mFrameChain[1],
                            mFrameWidth, mFrameHeight);

                    mSurfaceTexture = new SurfaceTexture(10);
                    mCamera.setPreviewTexture(mSurfaceTexture);

                    Log.d(TAG, "startPreview");
                    mCamera.startPreview();
                }
            } catch (Exception e) {
                result = false;
                e.printStackTrace();
            }
        }

        return result;
    }

    private void releaseCamera() {
        synchronized (OpenCvCameraView.this) {
            if (mCamera != null) {
                mCamera.stopPreview();
                mCamera.setPreviewCallback(null);
                mCamera.release();
            }
            mCamera = null;
            if (mFrameChain != null) {
                mFrameChain[0].release();
                mFrameChain[1].release();
            }
            if (mCameraFrame != null) {
                mCameraFrame[0].release();
                mCameraFrame[1].release();
            }
        }
    }

    @Override
    protected boolean connectCamera(int width, int height) {
        Log.d(TAG, "Connecting to camera");
        if (!initializeCamera(width, height)) {
            return false;
        }
        mCameraFrameReady = false;
        Log.d(TAG, "Starting processing thread");
        mStopThread = false;
        mThread = new Thread(new CameraWorker());
        mThread.start();
        return true;
    }

    @Override
    protected void disconnectCamera() {
        Log.d(TAG, "Disconnecting from camera");
        try {
            mStopThread = true;
            Log.d(TAG, "Notify thread");
            synchronized (this) {
                this.notify();
            }
            Log.d(TAG, "Wating for thread");
            if (mThread != null) {
                mThread.join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            mThread = null;
        }
        releaseCamera();

        mCameraFrameReady = false;
    }

    @Override
    public void onPreviewFrame(byte[] frame, Camera camera) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Preview Frame received. Frame size: " + frame.length);
        }
        synchronized (this) {
            mFrameChain[mChainIdx].put(0, 0, frame);
            mCameraFrameReady = true;
            this.notify();
        }
        if (mCamera != null) {
            mCamera.addCallbackBuffer(mBuffer);
        }
    }

    private class CameraWorker implements Runnable {

        @Override
        public void run() {
            do {
                boolean hasFrame = false;
                synchronized (OpenCvCameraView.this) {
                    try {
                        while (!mCameraFrameReady && !mStopThread) {
                            OpenCvCameraView.this.wait();
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (mCameraFrameReady) {
                        mChainIdx = 1 - mChainIdx;
                        mCameraFrameReady = false;
                        hasFrame = true;
                    }
                }

                if (!mStopThread && hasFrame) {
                    if (!mFrameChain[1 - mChainIdx].empty()) {
                        Log.d(TAG, "mChainId:" + mChainIdx);
                        deliverAndDrawFrame(mCameraFrame[1 - mChainIdx]);
                    }
                }
            } while (!mStopThread);
            Log.d(TAG, "Finish processing thread");
        }
    }

}
