package com.nm.blenderapp.colorpicker;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ComposeShader;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.os.Bundle;
import android.os.Parcelable;

import androidx.core.view.ViewCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/* loaded from: classes.dex */
public class ColorPicker extends View {
    private Path arrowPointerPath;
    private int arrowPointerSize;
    private float[] colorHSV;
    private RectF colorPointerCoords;
    private Paint colorPointerPaint;
    private Paint colorViewPaint;
    private Path colorViewPath;
    private Bitmap colorWheelBitmap;
    private Paint colorWheelPaint;
    private int colorWheelRadius;
    private Matrix gradientRotationMatrix;
    private int innerPadding;
    private int innerWheelRadius;
    private RectF innerWheelRect;
    private int outerPadding;
    private int outerWheelRadius;
    private RectF outerWheelRect;
    private final int paramArrowPointerSize;
    private final int paramInnerPadding;
    private final int paramOuterPadding;
    private final int paramValueSliderWidth;
    private Paint valuePointerArrowPaint;
    private Paint valuePointerPaint;
    private Paint valueSliderPaint;
    private Path valueSliderPath;
    private int valueSliderWidth;

    public ColorPicker(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.paramOuterPadding = 2;
        this.paramInnerPadding = 5;
        this.paramValueSliderWidth = 10;
        this.paramArrowPointerSize = 4;
        this.colorHSV = new float[]{0.0f, 0.0f, 1.0f};
        init();
    }

    public ColorPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.paramOuterPadding = 2;
        this.paramInnerPadding = 5;
        this.paramValueSliderWidth = 10;
        this.paramArrowPointerSize = 4;
        this.colorHSV = new float[]{0.0f, 0.0f, 1.0f};
        init();
    }

    public ColorPicker(Context context) {
        super(context);
        this.paramOuterPadding = 2;
        this.paramInnerPadding = 5;
        this.paramValueSliderWidth = 10;
        this.paramArrowPointerSize = 4;
        this.colorHSV = new float[]{0.0f, 0.0f, 1.0f};
        init();
    }

    private void init() {
        this.colorPointerPaint = new Paint();
        this.colorPointerPaint.setStyle(Paint.Style.STROKE);
        this.colorPointerPaint.setStrokeWidth(2.0f);
        this.colorPointerPaint.setARGB(128, 0, 0, 0);
        this.valuePointerPaint = new Paint();
        this.valuePointerPaint.setStyle(Paint.Style.STROKE);
        this.valuePointerPaint.setStrokeWidth(2.0f);
        this.valuePointerArrowPaint = new Paint();
        this.colorWheelPaint = new Paint();
        this.colorWheelPaint.setAntiAlias(true);
        this.colorWheelPaint.setDither(true);
        this.valueSliderPaint = new Paint();
        this.valueSliderPaint.setAntiAlias(true);
        this.valueSliderPaint.setDither(true);
        this.colorViewPaint = new Paint();
        this.colorViewPaint.setAntiAlias(true);
        this.colorViewPath = new Path();
        this.valueSliderPath = new Path();
        this.arrowPointerPath = new Path();
        this.outerWheelRect = new RectF();
        this.innerWheelRect = new RectF();
        this.colorPointerCoords = new RectF();
    }

    @Override // android.view.View
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = View.MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = View.MeasureSpec.getSize(heightMeasureSpec);
        int size = Math.min(widthSize, heightSize);
        setMeasuredDimension(size, size);
    }

    @Override // android.view.View
    @SuppressLint({"DrawAllocation"})
    protected void onDraw(Canvas canvas) {
        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;
        canvas.drawBitmap(this.colorWheelBitmap, centerX - this.colorWheelRadius, centerY - this.colorWheelRadius, (Paint) null);
        this.colorViewPaint.setColor(Color.HSVToColor(this.colorHSV));
        canvas.drawPath(this.colorViewPath, this.colorViewPaint);
        float[] hsv = {this.colorHSV[0], this.colorHSV[1], 1.0f};
        SweepGradient sweepGradient = new SweepGradient(centerX, centerY, new int[]{ViewCompat.MEASURED_STATE_MASK, Color.HSVToColor(hsv), -1}, (float[]) null);
        sweepGradient.setLocalMatrix(this.gradientRotationMatrix);
        this.valueSliderPaint.setShader(sweepGradient);
        canvas.drawPath(this.valueSliderPath, this.valueSliderPaint);
        float hueAngle = (float) Math.toRadians(this.colorHSV[0]);
        int colorPointX = ((int) ((-Math.cos(hueAngle)) * this.colorHSV[1] * this.colorWheelRadius)) + centerX;
        int colorPointY = ((int) ((-Math.sin(hueAngle)) * this.colorHSV[1] * this.colorWheelRadius)) + centerY;
        float pointerRadius = 0.075f * this.colorWheelRadius;
        int pointerX = (int) (colorPointX - (pointerRadius / 2.0f));
        int pointerY = (int) (colorPointY - (pointerRadius / 2.0f));
        this.colorPointerCoords.set(pointerX, pointerY, pointerX + pointerRadius, pointerY + pointerRadius);
        canvas.drawOval(this.colorPointerCoords, this.colorPointerPaint);
        this.valuePointerPaint.setColor(Color.HSVToColor(new float[]{0.0f, 0.0f, 1.0f - this.colorHSV[2]}));
        double valueAngle = (this.colorHSV[2] - 0.5f) * 3.141592653589793d;
        float valueAngleX = (float) Math.cos(valueAngle);
        float valueAngleY = (float) Math.sin(valueAngle);
        canvas.drawLine(centerX + (this.innerWheelRadius * valueAngleX), centerY + (this.innerWheelRadius * valueAngleY), centerX + (this.outerWheelRadius * valueAngleX), centerY + (this.outerWheelRadius * valueAngleY), this.valuePointerPaint);
        if (this.arrowPointerSize > 0) {
            drawPointerArrow(canvas);
        }
    }

    private void drawPointerArrow(Canvas canvas) {
        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;
        double tipAngle = (this.colorHSV[2] - 0.5f) * 3.141592653589793d;
        double leftAngle = tipAngle + 0.032724923474893676d;
        double rightAngle = tipAngle - 0.032724923474893676d;
        double tipAngleX = Math.cos(tipAngle) * this.outerWheelRadius;
        double tipAngleY = Math.sin(tipAngle) * this.outerWheelRadius;
        double leftAngleX = Math.cos(leftAngle) * (this.outerWheelRadius + this.arrowPointerSize);
        double leftAngleY = Math.sin(leftAngle) * (this.outerWheelRadius + this.arrowPointerSize);
        double rightAngleX = Math.cos(rightAngle) * (this.outerWheelRadius + this.arrowPointerSize);
        double rightAngleY = Math.sin(rightAngle) * (this.outerWheelRadius + this.arrowPointerSize);
        this.arrowPointerPath.reset();
        this.arrowPointerPath.moveTo(((float) tipAngleX) + centerX, ((float) tipAngleY) + centerY);
        this.arrowPointerPath.lineTo(((float) leftAngleX) + centerX, ((float) leftAngleY) + centerY);
        this.arrowPointerPath.lineTo(((float) rightAngleX) + centerX, ((float) rightAngleY) + centerY);
        this.arrowPointerPath.lineTo(((float) tipAngleX) + centerX, ((float) tipAngleY) + centerY);
        this.valuePointerArrowPaint.setColor(Color.HSVToColor(this.colorHSV));
        this.valuePointerArrowPaint.setStyle(Paint.Style.FILL);
        canvas.drawPath(this.arrowPointerPath, this.valuePointerArrowPaint);
        this.valuePointerArrowPaint.setStyle(Paint.Style.STROKE);
        this.valuePointerArrowPaint.setStrokeJoin(Paint.Join.ROUND);
        this.valuePointerArrowPaint.setColor(ViewCompat.MEASURED_STATE_MASK);
        canvas.drawPath(this.arrowPointerPath, this.valuePointerArrowPaint);
    }

    @Override // android.view.View
    protected void onSizeChanged(int width, int height, int oldw, int oldh) {
        int centerX = width / 2;
        int centerY = height / 2;
        this.innerPadding = (width * 5) / 100;
        this.outerPadding = (width * 2) / 100;
        this.arrowPointerSize = (width * 4) / 100;
        this.valueSliderWidth = (width * 10) / 100;
        this.outerWheelRadius = ((width / 2) - this.outerPadding) - this.arrowPointerSize;
        this.innerWheelRadius = this.outerWheelRadius - this.valueSliderWidth;
        this.colorWheelRadius = this.innerWheelRadius - this.innerPadding;
        this.outerWheelRect.set(centerX - this.outerWheelRadius, centerY - this.outerWheelRadius, this.outerWheelRadius + centerX, this.outerWheelRadius + centerY);
        this.innerWheelRect.set(centerX - this.innerWheelRadius, centerY - this.innerWheelRadius, this.innerWheelRadius + centerX, this.innerWheelRadius + centerY);
        this.colorWheelBitmap = createColorWheelBitmap(this.colorWheelRadius * 2, this.colorWheelRadius * 2);
        this.gradientRotationMatrix = new Matrix();
        this.gradientRotationMatrix.preRotate(270.0f, width / 2, height / 2);
        this.colorViewPath.arcTo(this.outerWheelRect, 270.0f, -180.0f);
        this.colorViewPath.arcTo(this.innerWheelRect, 90.0f, 180.0f);
        this.valueSliderPath.arcTo(this.outerWheelRect, 270.0f, 180.0f);
        this.valueSliderPath.arcTo(this.innerWheelRect, 90.0f, -180.0f);
    }

    private Bitmap createColorWheelBitmap(int width, int height) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        int[] colors = new int[13];
        float[] hsv = {0.0f, 1.0f, 1.0f};
        for (int i = 0; i < colors.length; i++) {
            hsv[0] = ((i * 30) + 180) % 360;
            colors[i] = Color.HSVToColor(hsv);
        }
        colors[12] = colors[0];
        SweepGradient sweepGradient = new SweepGradient(width / 2, height / 2, colors, (float[]) null);
        RadialGradient radialGradient = new RadialGradient(width / 2, height / 2, this.colorWheelRadius, -1, ViewCompat.MEASURED_SIZE_MASK, Shader.TileMode.CLAMP);
        ComposeShader composeShader = new ComposeShader(sweepGradient, radialGradient, PorterDuff.Mode.SRC_OVER);
        this.colorWheelPaint.setShader(composeShader);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawCircle(width / 2, height / 2, this.colorWheelRadius, this.colorWheelPaint);
        return bitmap;
    }

    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        switch (action) {
            case 0:
            case 2:
                int x = (int) event.getX();
                int y = (int) event.getY();
                int cx = x - (getWidth() / 2);
                int cy = y - (getHeight() / 2);
                double d = Math.sqrt((cx * cx) + (cy * cy));
                if (d <= this.colorWheelRadius) {
                    this.colorHSV[0] = (float) (Math.toDegrees(Math.atan2(cy, cx)) + 180.0d);
                    this.colorHSV[1] = Math.max(0.0f, Math.min(1.0f, (float) (d / this.colorWheelRadius)));
                    invalidate();
                } else if (x >= getWidth() / 2 && d >= this.innerWheelRadius) {
                    this.colorHSV[2] = (float) Math.max(0.0d, Math.min(1.0d, (Math.atan2(cy, cx) / 3.141592653589793d) + 0.5d));
                    invalidate();
                }
                return true;
            case 1:
            default:
                return super.onTouchEvent(event);
        }
    }

    public void setColor(int color) {
        Color.colorToHSV(color, this.colorHSV);
    }

    public int getColor() {
        return Color.HSVToColor(this.colorHSV);
    }

    public float[] getHSV() {
        return this.colorHSV;
    }

    @Override // android.view.View
    protected Parcelable onSaveInstanceState() {
        Bundle state = new Bundle();
        state.putFloatArray("color", this.colorHSV);
        state.putParcelable("super", super.onSaveInstanceState());
        return state;
    }

    @Override // android.view.View
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            this.colorHSV = bundle.getFloatArray("color");
            super.onRestoreInstanceState(bundle.getParcelable("super"));
            return;
        }
        super.onRestoreInstanceState(state);
    }
}

