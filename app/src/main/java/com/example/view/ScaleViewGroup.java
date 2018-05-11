package com.example.view;

import android.content.Context;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

public class ScaleViewGroup extends RelativeLayout {

    private static final String TAG = "ScaleViewGroup";

    private float mCurrentScaleX = 1;
    private float mCurrentScaleY = 1;
    private float mPreDistance;

    private static final int O_VERTIVAL = 0;
    private static final int O_HOR = 1;
    private static final int O_ALL = 2;

    private int mCurrentOrientation = O_ALL;

    View mScalableView;
    private float mScale;
    private Point mCoor = new Point();
    private boolean mScalable;

    public void setScalableView(View view) {
        mScalableView = view;
    }

    public void resetScalableView() {
        if (mScalableView != null) {
            mCurrentScaleX = 1;
            mCurrentScaleY = 1;
            mScalableView.setScaleX(1);
            mScalableView.setScaleY(1);
            mScalableView.layout(0, 0, mScalableView.getWidth(), mScalableView.getHeight());
        }
    }

    public void setScalable(boolean scalable) {
        this.mScalable = scalable;
    }

    public Box getBoxCoor() {
        return calculateViewCoor();
    }

    static class Point extends PointF {
        boolean initialed = false;
    }

    public class Box {
        public int left;
        public int top;
        public int width;
        public int height;

        Box(int left, int top, int width, int height) {
            this.left = left;
            this.top = top;
            this.width = width;
            this.height = height;
        }
    }

    public ScaleViewGroup(Context context) {
        super(context);
    }

    public ScaleViewGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ScaleViewGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mScalableView == null || !mScalable) {
            return super.onTouchEvent(event);
        }
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                if (event.getPointerCount() == 2) {
                    mPreDistance = getDistance(event);
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                resetStatus();
                calculateViewCoor();
                break;
            case MotionEvent.ACTION_MOVE:
                if (event.getPointerCount() == 1) {
                    moveView(mScalableView, event.getRawX(), event.getRawY());
                } else if (event.getPointerCount() == 2) {
                    scaleView(event);
                }
                break;
        }
        return true;
    }

    private void resetStatus() {
        mCoor.initialed = false;
        if (mCurrentOrientation == O_VERTIVAL) {
            mCurrentScaleY = mScale;
        } else if (mCurrentOrientation == O_HOR) {
            mCurrentScaleX = mScale;
        }
        mCurrentOrientation = O_ALL;
        Log.d(TAG, "SCALE X :" + mCurrentScaleX + "  SCALE Y:" + mCurrentScaleY);
    }

    private Box calculateViewCoor() {
        int left = mScalableView.getLeft();
        int top = mScalableView.getTop();
        int width = mScalableView.getWidth();
        int height = mScalableView.getHeight();
        int real_left = (int) (left + width * (1 - mCurrentScaleX) / 2);
        int real_top = (int) (top + height * (1 - mCurrentScaleY) / 2);
        int real_width = (int) (mCurrentScaleX * width);
        int real_height = (int) (mCurrentScaleY * height);
        Log.d(TAG, "real_left:" + real_left + " real_top:" + real_top + " real_width:" + real_width + " real_height:" + real_height);
        return new Box(real_left, real_top, real_width, real_height);
    }

    private void scaleView(MotionEvent event) {
        float distance = getDistance(event);
        if (distance > 10f && mCurrentOrientation != O_ALL) {
            if (mCurrentOrientation == O_VERTIVAL) {
                mScale = mCurrentScaleY * distance / mPreDistance;
                mScalableView.setScaleY(mScale);
            } else if (mCurrentOrientation == O_HOR) {
                mScale = mCurrentScaleX * distance / mPreDistance;
                mScalableView.setScaleX(mScale);
            }
        }
    }

    private void moveView(View view, float rawX, float rawY) {
        if (view == null) {
            return;
        }
        if (!mCoor.initialed) {
            mCoor.initialed = true;
            mCoor.set(rawX - view.getLeft(), rawY - view.getTop());
        } else {
            int left = (int) (rawX - mCoor.x);
            int top = (int) (rawY - mCoor.y);
            int width = left + view.getWidth();
            int height = top + view.getHeight();
            view.layout(left, top, width, height);
            Log.d(TAG, "left:" + left + " top:" + top + " width:" + width + " height:" + height);
        }
    }

    private float getDistance(MotionEvent event) {
        float x = Math.abs(event.getX(1) - event.getX(0));
        float y = Math.abs(event.getY(1) - event.getY(0));

        if (x <= y && mCurrentOrientation == O_ALL) {
            mCurrentOrientation = O_VERTIVAL;
        } else if (x > y && mCurrentOrientation == O_ALL) {
            mCurrentOrientation = O_HOR;
        }
        return (float) Math.sqrt(x * x + y * y);
    }

}
