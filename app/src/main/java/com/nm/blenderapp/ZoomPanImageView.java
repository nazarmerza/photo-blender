package com.nm.blenderapp;

import android.content.Context;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class ZoomPanImageView extends androidx.appcompat.widget.AppCompatImageView {

    private float lastX, lastY;
    private int mode = 0; // 0 = none, 1 = drag, 2 = zoom
    private float startDist = 0f;
    private float startScale = 1f;

    public ZoomPanImageView(Context context) {
        super(context);
        init();
    }

    public ZoomPanImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setScaleType(ScaleType.FIT_CENTER);
        setWillNotDraw(false);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mode = 1;
                lastX = event.getRawX();
                lastY = event.getRawY();
                break;

            case MotionEvent.ACTION_MOVE:
                if (mode == 1) {
                    float dx = event.getRawX() - lastX;
                    float dy = event.getRawY() - lastY;
                    setTranslationX(getTranslationX() + dx);
                    setTranslationY(getTranslationY() + dy);
                    lastX = event.getRawX();
                    lastY = event.getRawY();
                } else if (mode == 2 && event.getPointerCount() == 2) {
                    float newDist = spacing(event);
                    if (newDist > 10f) {
                        float scaleFactor = newDist / startDist;
                        setScaleX(startScale * scaleFactor);
                        setScaleY(startScale * scaleFactor);
                    }
                }
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                if (event.getPointerCount() == 2) {
                    startDist = spacing(event);
                    startScale = getScaleX();
                    mode = 2;
                }
                break;

            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_UP:
                mode = 0;
                break;
        }
        return true;
    }

    private float spacing(MotionEvent e) {
        float x = e.getX(0) - e.getX(1);
        float y = e.getY(0) - e.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    /**
     * Returns the rectangle in screen coordinates where the bitmap is currently drawn,
     * after FIT_CENTER scaling and applying zoom & pan (scaleX, translationX/Y).
     */
    public RectF getBitmapRectOnScreen() {
        Drawable drawable = getDrawable();
        if (drawable == null) return null;

        int drawableWidth = drawable.getIntrinsicWidth();
        int drawableHeight = drawable.getIntrinsicHeight();

        int viewWidth = getWidth();
        int viewHeight = getHeight();

        if (viewWidth == 0 || viewHeight == 0) return null;

        // Calculate scale for FIT_CENTER
        float scale = Math.min((float) viewWidth / drawableWidth, (float) viewHeight / drawableHeight);

        float displayedWidth = drawableWidth * scale;
        float displayedHeight = drawableHeight * scale;

        // Bitmap position inside ImageView (centered)
        float leftInView = (viewWidth - displayedWidth) / 2f;
        float topInView = (viewHeight - displayedHeight) / 2f;

        // Apply zoom (scaleX) and translation (translationX/Y)
        float zoom = getScaleX(); // assumes scaleX == scaleY
        float transX = getTranslationX();
        float transY = getTranslationY();

        // Final displayed rect in screen coords
        float left = leftInView * zoom + transX + getLeft();
        float top = topInView * zoom + transY + getTop();
        float right = left + displayedWidth * zoom;
        float bottom = top + displayedHeight * zoom;

        return new RectF(left, top, right, bottom);
    }

    /**
     * Converts a rectangle from screen coordinates to bitmap pixel coordinates.
     * Returns null if no drawable.
     */
    public RectF screenRectToBitmapRect(RectF screenRect) {
        Drawable drawable = getDrawable();
        if (drawable == null) return null;

        int drawableWidth = drawable.getIntrinsicWidth();
        int drawableHeight = drawable.getIntrinsicHeight();

        RectF bitmapRect = getBitmapRectOnScreen();
        if (bitmapRect == null) return null;

        // Clamp screenRect within bitmapRect
        float left = Math.max(screenRect.left, bitmapRect.left);
        float top = Math.max(screenRect.top, bitmapRect.top);
        float right = Math.min(screenRect.right, bitmapRect.right);
        float bottom = Math.min(screenRect.bottom, bitmapRect.bottom);

        if (right <= left || bottom <= top) return null;

        // Map clamped rect from screen to bitmap pixels
        float scaleX = drawableWidth / bitmapRect.width();
        float scaleY = drawableHeight / bitmapRect.height();

        float bmpLeft = (left - bitmapRect.left) * scaleX;
        float bmpTop = (top - bitmapRect.top) * scaleY;
        float bmpRight = (right - bitmapRect.left) * scaleX;
        float bmpBottom = (bottom - bitmapRect.top) * scaleY;

        return new RectF(bmpLeft, bmpTop, bmpRight, bmpBottom);
    }
}
