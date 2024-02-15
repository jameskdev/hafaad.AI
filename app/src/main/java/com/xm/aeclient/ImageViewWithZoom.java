package com.xm.aeclient;

import static android.view.MotionEvent.INVALID_POINTER_ID;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ImageViewWithZoom extends androidx.appcompat.widget.AppCompatImageView {
    private final ScaleGestureDetector mScaleDetector;
    private int mActivePointerId = INVALID_POINTER_ID;
    private float mLastTouchX = 0f;
    private float mLastTouchY = 0f;
    float mScaleFactor = 1.f;
    float mPrevScaleFactor = 0f;

    public ImageViewWithZoom(@NonNull Context context) {
        this(context, null);
    }

    public ImageViewWithZoom(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ImageViewWithZoom(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        mScaleDetector.onTouchEvent(ev);

        final int action = ev.getActionMasked();

        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                final int pointerIndex = ev.getActionIndex();
                // TODO: Fix unstable panning on API 28
                final float x;
                final float y;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                    x = ev.getRawX(pointerIndex);
                    y = ev.getRawY(pointerIndex);
                } else {
                    x = ev.getX(pointerIndex);
                    y = ev.getY(pointerIndex);
                }
                // Remember the starting position of the pointer.
                mLastTouchX = x;
                mLastTouchY = y;
                // Save the ID of this pointer for dragging.
                mActivePointerId = ev.getPointerId(0);
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                // Find the index of the active pointer and fetch its position.
                final int pointerIndex = ev.findPointerIndex(mActivePointerId);

                // TODO: Fix unstable panning on API 28
                final float x;
                final float y;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                    x = ev.getRawX(pointerIndex);
                    y = ev.getRawY(pointerIndex);
                } else {
                    x = ev.getX(pointerIndex);
                    y = ev.getY(pointerIndex);
                }

                // Calculate the distance moved.
                final float dx = x - mLastTouchX;
                final float dy = y - mLastTouchY;

                setX(getX() + dx);
                setY(getY() + dy);

                // Remember this touch position for the next move event.
                mLastTouchX = x;
                mLastTouchY = y;

                break;
            }

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP: {
                mActivePointerId = INVALID_POINTER_ID;
                break;
            }

            case MotionEvent.ACTION_POINTER_UP: {
                final int pointerIndex = ev.getActionIndex();
                final int pointerId = ev.getPointerId(pointerIndex);

                if (pointerId == mActivePointerId) {
                    // This is the active pointer going up. Choose a new
                    // active pointer and adjust it accordingly.
                    final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                    // TODO: Fix unstable panning on API 28
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                        mLastTouchX = ev.getRawX(newPointerIndex);
                        mLastTouchY = ev.getRawY(newPointerIndex);
                    } else {
                        mLastTouchX = ev.getX(newPointerIndex);
                        mLastTouchY = ev.getY(newPointerIndex);
                    }
                    mActivePointerId = ev.getPointerId(newPointerIndex);
                }
                break;
            }
        }
        return true;
    }

    private class ScaleListener
            extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(@NonNull ScaleGestureDetector detector) {
            if (mPrevScaleFactor != 0f) {
                // Stabilize scale factor, to prevent "stuttering"
                mScaleFactor *= (detector.getScaleFactor() + mPrevScaleFactor) / 2;
            } else {
                mScaleFactor *= detector.getScaleFactor();
            }
            // Don't allow sizes below the minimum size
            if (getHeight() <= getMinimumHeight() || getWidth() <= getMinimumWidth()) {
                if (mScaleFactor < 1f) {
                    return true;
                }
            }
            // Don't let the object get too small or too large.
            mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor, 5.0f));
            mPrevScaleFactor = detector.getScaleFactor();
            setScaleX(mScaleFactor);
            setScaleY(mScaleFactor);
            return true;
        }

        @Override
        public void onScaleEnd(@NonNull ScaleGestureDetector detector) {
            super.onScaleEnd(detector);
            mPrevScaleFactor = 0f;
        }
    }
}