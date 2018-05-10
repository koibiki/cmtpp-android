package com.example.opencv;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Size;

import java.util.List;

/**
 * Created by chengli on 17-7-3.
 */

public abstract class CameraBridgeView extends SurfaceView implements SurfaceHolder.Callback {

    private static final String TAG = CameraBridgeView.class.getName();

    private boolean mSurfaceExist;

    private Object mSyncObject = new Object();

    protected int mFrameWidth;

    protected int mFrameHeight;

    private boolean mEnabled;

    private static final int STATE_STOPPED = 0;

    private static final int STATE_STARTED = 1;

    private int mState;

    protected float mScale = 0;

    private CvCameraViewListener mListener;

    private Bitmap mCacheBitmap;

    protected FpsMeter mFpsMeter = null;

    public static final int RGBA = 1;

    public static final int GRAY = 2;

    protected SurfaceHolder mSurfaceHolder;

    public CameraBridgeView(Context context) {
        this(context, null);
    }

    public CameraBridgeView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CameraBridgeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mSurfaceHolder = getHolder();
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mSurfaceHolder.addCallback(this);
    }

    public void enableFpsMeter() {
        if (mFpsMeter == null) {
            mFpsMeter = new FpsMeter();
            mFpsMeter.setResolution(mFrameWidth, mFrameHeight);
        }
    }

    public void disableFpsMeter() {
        mFpsMeter = null;
    }

    public interface CvCameraViewListener {

        void onCameraViewStarted(int width, int height);

        void onCameraViewStopped();

        Mat onCameraFrame(CvCameraViewFrame inputFrame);
    }

    public void setCvCameraViewListener(CvCameraViewListener listener) {
        mListener = listener;
    }

    public interface CvCameraViewFrame {

        Mat rgba();

        Mat gray();
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        checkCurrentState();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        synchronized (mSyncObject) {
            if (!mSurfaceExist) {
                mSurfaceExist = true;
                checkCurrentState();
            } else {
                /** Surface changed. We need to stop camera and restart with new parameters */
                /* Pretend that old surface has been destroyed */
                mSurfaceExist = false;
                checkCurrentState();
                /* Now use new surface. Say we have it now */
                mSurfaceExist = true;
                checkCurrentState();
            }
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    public void enableView() {
        synchronized (mSyncObject) {
            mEnabled = true;
            checkCurrentState();
        }
    }

    public void disableView() {
        synchronized (mSyncObject) {
            mEnabled = false;
            checkCurrentState();
        }
    }

    private void checkCurrentState() {
        Log.d(TAG, "call checkCurrentState");
        int targetState;

        if (mEnabled && mSurfaceExist && getVisibility() == VISIBLE) {
            targetState = STATE_STARTED;
        } else {
            targetState = STATE_STOPPED;
        }

        if (targetState != mState) {
            processExitState(mState);
            mState = targetState;
            processEnterState(mState);
        }
    }

    private void processEnterState(int state) {
        Log.d(TAG, "call processEnterState: " + state);
        switch (state) {
            case STATE_STARTED:
                onEnterStartedState();
                if (mListener != null) {
                    mListener.onCameraViewStarted(mFrameWidth, mFrameHeight);
                }
                break;
            case STATE_STOPPED:
                onEnterStoppedState();
                if (mListener != null) {
                    mListener.onCameraViewStopped();
                }
                break;
        }
    }

    private void processExitState(int state) {
        Log.d(TAG, "call processExitState: " + state);
        switch (state) {
            case STATE_STARTED:
                onExitStartedState();
                break;
            case STATE_STOPPED:
                onExitStoppedState();
                break;
        }
    }

    private void onEnterStoppedState() {
    }

    private void onExitStoppedState() {
    }

    private void onEnterStartedState() {
        Log.d(TAG, "call onEnterStartedState");
        /* Connect camera */
        if (!connectCamera(getWidth(), getHeight())) {
            AlertDialog ad = new AlertDialog.Builder(getContext()).create();
            ad.setCancelable(false); // This blocks the 'BACK' button
            ad.setMessage(
                    "It seems that you device does not support camera (or it is locked). Application will be closed.");
            ad.setButton(DialogInterface.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            ((Activity) getContext()).finish();
                        }
                    });
            ad.show();

        }
    }

    private void onExitStartedState() {
        disconnectCamera();
        if (mCacheBitmap != null) {
            mCacheBitmap.recycle();
        }
    }

    protected void AllocateCache() {
        mCacheBitmap = Bitmap.createBitmap(mFrameHeight, mFrameWidth, Bitmap.Config.ARGB_8888);
    }

    protected void deliverAndDrawFrame(CvCameraViewFrame frame) {
        Mat modified;

        if (mListener != null) {
            modified = mListener.onCameraFrame(frame);
        } else {
            modified = frame.rgba();
        }

        boolean bmpValid = true;
        if (modified != null) {
            try {
                Utils.matToBitmap(modified, mCacheBitmap);
                modified.release();
            } catch (Exception e) {
                Log.e(TAG, "Mat type: " + modified);
                Log.e(TAG, "Utils.matToBitmap() throws an exception: " + e.getMessage());
                bmpValid = false;
            }
        }

        if (bmpValid && mCacheBitmap != null) {
            Canvas canvas = getHolder().lockCanvas();
            if (canvas != null) {
                canvas.drawColor(0, android.graphics.PorterDuff.Mode.CLEAR);

                Log.d(TAG, "mStretch value: " + mScale);

                int width = mCacheBitmap.getWidth();
                int height = mCacheBitmap.getHeight();
                int canvasWidth = canvas.getWidth();
                int canvasHeight = canvas.getHeight();
//                canvas.drawBitmap(mCacheBitmap, 0, -mCacheBitmap.getHeight(), null);
                if (mScale != 0) {
                    canvas.drawBitmap(mCacheBitmap,
                            new Rect(0, 0, width, height),
                            new Rect((int) ((canvasWidth - mScale * width) / 2),
                                    (int) ((canvasHeight - mScale * height) / 2),
                                    (int) ((canvasWidth - mScale * width) / 2 + mScale * width),
                                    (int) ((canvasHeight - mScale * height) / 2 + mScale * height)),
                            null);
                } else {
                    canvas.drawBitmap(mCacheBitmap,
                            new Rect(0, 0, width, height),
                            new Rect((canvasWidth - width) / 2,
                                    (canvasHeight - height) / 2,
                                    (canvasWidth - width) / 2 + width,
                                    (canvasHeight - height) / 2 + height), null);
                }
                if (mFpsMeter != null) {
                    mFpsMeter.measure();
                    mFpsMeter.draw(canvas, 20, 80);
                }
                getHolder().unlockCanvasAndPost(canvas);
            }
        }
    }

    protected abstract boolean connectCamera(int width, int height);

    protected abstract void disconnectCamera();

    protected Size calculateCameraFrameSize(List<?> supportedSizes,
                                            CameraBridgeViewBase.ListItemAccessor accessor,
                                            int surfaceWidth, int surfaceHeight) {
        int calcWidth = 0;
        int calcHeight = 0;

        for (Object size : supportedSizes) {
            int width = accessor.getWidth(size);
            int height = accessor.getHeight(size);

            if (width <= surfaceWidth && height <= surfaceHeight) {
                if (width >= calcWidth && height >= calcHeight) {
                    calcWidth = width;
                    calcHeight = height;
                }
            }
        }
        return new Size(1920, 1080);
    }

}
