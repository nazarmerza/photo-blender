package com.nm.blenderapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class BlendActivity extends AppCompatActivity {
    static {
        System.loadLibrary("blenderpro");
    }

    private native void processBitmapsNative(Bitmap topBitmap, Bitmap bottomBitmap, Bitmap outputBitmap, int overlapHeight);
    private native void linearBlendNative(Bitmap topBitmap, Bitmap bottomBitmap, Bitmap outputBitmap, int overlapHeight);
    private native void processSingleBitmap(Bitmap outputBitmap);

    private final String TAG = this.getClass().getName();
    private ZoomPanImageView topImageView;
    private ZoomPanImageView bottomImageView;

    private ImageView outputImageView;
    private Bitmap originalTopBitmap;
    private Bitmap originalBottomBitmap;

    private boolean isCropped = false;
    private boolean isBlended = false;
    private int overlapHeight = 0; // ✅ Class variable to store overlap height

    private App app;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blend); // updated layout using LinearLayouts

        topImageView = findViewById(R.id.top_image_view);
        bottomImageView = findViewById(R.id.bottom_image_view);
        outputImageView = findViewById(R.id.output_image_view);

        LinearLayout blendButtonLayout = findViewById(R.id.blend_button);
        LinearLayout undoButtonLayout = findViewById(R.id.undo_button);
        LinearLayout colorizeButtonLayout = findViewById(R.id.next_button);
        LinearLayout backButtonLayout = findViewById(R.id.back_button);

        app = (App) getApplication();

        topImageView.setImageURI(app.getPhotoUri1());
        bottomImageView.setImageURI(app.getPhotoUri2());

        loadImageToFit(topImageView, app.getTopBitmap());
        loadImageToFit(bottomImageView, app.getBottomBitmap());


        // Then balance
//        balanceBitmapDensities(topImageView, bottomImageView);

//        topImageView.setImageBitmap(app.getTopBitmap());
//        bottomImageView.setImageBitmap(app.getBottomBitmap());

//        topImageView.setImageResource(R.drawable.happy_couple);
//        bottomImageView.setImageResource(R.drawable.couple_sunset_resized);


        backButtonLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        blendButtonLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isBlended) {
                    Toast.makeText(BlendActivity.this, "Photos are already blended!", Toast.LENGTH_SHORT).show();
                } else {
                    performFullCroppingIfOverlapping();
                }
            }
        });

        undoButtonLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                restoreOriginals();
            }
        });

        colorizeButtonLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isBlended) {
                    Toast.makeText(BlendActivity.this, "You need to blend photos firt, before moving to next action.", Toast.LENGTH_SHORT).show();
                } else {
                    Bitmap outputBitmap = ((BitmapDrawable) outputImageView.getDrawable()).getBitmap();
                    app.setSharedBitmap(outputBitmap);
                    Intent intent = new Intent(BlendActivity.this, ColorizeActivity.class);
                    startActivity(intent);
                }
            }
        });
    }


    private void loadImageToFit(final ZoomPanImageView imageView, final Bitmap bitmap) {
        imageView.post(() -> {
            try {
                // Get view dimensions
                int viewWidth = imageView.getWidth();
                int viewHeight = imageView.getHeight();
                if (viewWidth == 0 || viewHeight == 0) return;

                // Load bitmap from URI
                if (bitmap == null) return;

                // Calculate scale factor
                float widthScale = (float) viewWidth / bitmap.getWidth();
                float heightScale = (float) viewHeight / bitmap.getHeight();
                float scale = Math.min(widthScale, heightScale); // Fit inside

                int scaledWidth = Math.round(bitmap.getWidth() * scale);
                int scaledHeight = Math.round(bitmap.getHeight() * scale);

                Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, true);

                imageView.setImageBitmap(scaledBitmap);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadImageToFit(final ZoomPanImageView imageView, final Uri imageUri) {
        imageView.post(() -> {
            try {
                // Get view dimensions
                int viewWidth = imageView.getWidth();
                int viewHeight = imageView.getHeight();
                if (viewWidth == 0 || viewHeight == 0) return;

                // Load bitmap from URI
                Bitmap original = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                if (original == null) return;

                // Calculate scale factor
                float widthScale = (float) viewWidth / original.getWidth();
                float heightScale = (float) viewHeight / original.getHeight();
                float scale = Math.min(widthScale, heightScale); // Fit inside

                int scaledWidth = Math.round(original.getWidth() * scale);
                int scaledHeight = Math.round(original.getHeight() * scale);

                Bitmap scaledBitmap = Bitmap.createScaledBitmap(original, scaledWidth, scaledHeight, true);

                imageView.setImageBitmap(scaledBitmap);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
            }
        });
    }



    private void blendImages() {
        Bitmap topBitmap = getCurrentBitmap(topImageView);
        Bitmap bottomBitmap = getCurrentBitmap(bottomImageView);
        Bitmap outputBitmap = createOutputBitmapRect(topBitmap, bottomBitmap, overlapHeight / 2);
        if (outputBitmap != null) {
            outputBitmap.eraseColor(Color.BLUE);
        }

//        blendImagesIntoOutputBitmap(topBitmap, bottomBitmap, outputBitmap, overlapHeight / 2);

        linearBlendNative(topBitmap, bottomBitmap, outputBitmap, overlapHeight / 2);

        topImageView.setVisibility(View.GONE);
        bottomImageView.setVisibility(View.GONE);
        outputImageView.setVisibility(View.VISIBLE);
        outputImageView.setImageBitmap(outputBitmap);

        // test native to convert to grayscale
//        processSingleBitmap(outputBitmap);
        outputImageView.setImageBitmap(outputBitmap);

        isBlended = true;

    }

    private void blendImagesIntoOutputBitmap(Bitmap topBitmap, Bitmap bottomBitmap, Bitmap outputBitmap, int overlapHeight) {
        if (topBitmap == null || bottomBitmap == null || outputBitmap == null) return;

        int width = topBitmap.getWidth();
        int topHeight = topBitmap.getHeight();
        int bottomHeight = bottomBitmap.getHeight();

        int outputY = 0;

        // 1. Copy top region (top part of topBitmap above overlap)
        for (int y = 0; y < topHeight - overlapHeight; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = topBitmap.getPixel(x, y);
                outputBitmap.setPixel(x, outputY + y, pixel);
            }
        }

        outputY += (topHeight - overlapHeight);

        // 2. Blend overlap region (average RGB from both bitmaps)
        for (int y = 0; y < overlapHeight; y++) {
            for (int x = 0; x < width; x++) {
                int topPixel = topBitmap.getPixel(x, (topHeight - overlapHeight) + y);
                int bottomPixel = bottomBitmap.getPixel(x, y);

                int topR = (topPixel >> 16) & 0xFF;
                int topG = (topPixel >> 8) & 0xFF;
                int topB = topPixel & 0xFF;

                int bottomR = (bottomPixel >> 16) & 0xFF;
                int bottomG = (bottomPixel >> 8) & 0xFF;
                int bottomB = bottomPixel & 0xFF;

                int avgR = (topR + bottomR) / 2;
                int avgG = (topG + bottomG) / 2;
                int avgB = (topB + bottomB) / 2;

                int blendedColor = 0xFF000000 | (avgR << 16) | (avgG << 8) | avgB;
                outputBitmap.setPixel(x, outputY + y, blendedColor);
            }
        }

        outputY = topBitmap.getHeight() - 1;

        // 3. Copy bottom region (bottom part of bottomBitmap below overlap)
        for (int y = overlapHeight; y < bottomHeight; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = bottomBitmap.getPixel(x, y);

//                Log.d(TAG, "y = " + outputY);
                outputBitmap.setPixel(x, outputY, pixel);

            }
            outputY++;
        }

//        Toast.makeText(this, "Test", Toast.LENGTH_SHORT);
    }


    private Bitmap createOutputBitmapRect(Bitmap topBitmap, Bitmap bottomBitmap, int overlapHeight) {
        if (topBitmap == null || bottomBitmap == null) return null;

        int width = topBitmap.getWidth();
        int totalHeight = topBitmap.getHeight() + bottomBitmap.getHeight() - overlapHeight;

        return Bitmap.createBitmap(width, totalHeight, Bitmap.Config.ARGB_8888);
    }

    private void performFullCroppingIfOverlapping() {
        // Step 0: Check for overlap
        Rect topRect = new Rect();
        Rect bottomRect = new Rect();
        boolean topOk = topImageView.getGlobalVisibleRect(topRect);
        boolean bottomOk = bottomImageView.getGlobalVisibleRect(bottomRect);
        Rect overlapRect = new Rect();

        boolean isOverlapping = topOk && bottomOk && overlapRect.setIntersect(topRect, bottomRect);
        if (!isOverlapping || overlapRect.isEmpty()) {
            CharSequence text = "There has to be overlap between images to perform the action!";
            Toast.makeText(this /* MyActivity */, text, Toast.LENGTH_SHORT).show();
            return;
        }

        overlapHeight = overlapRect.height(); // ✅ Store overlap height

        // Step 1: Save original bitmaps
        originalTopBitmap = getCurrentBitmap(topImageView);
        originalBottomBitmap = getCurrentBitmap(bottomImageView);

        // Step 2: Crop to visible parts
        Bitmap topVisible = cropVisiblePart(topImageView, originalTopBitmap);
        Bitmap bottomVisible = cropVisiblePart(bottomImageView, originalBottomBitmap);
        if (topVisible == null || bottomVisible == null) return;

        topImageView.setImageBitmap(topVisible);
        bottomImageView.setImageBitmap(bottomVisible);
        resetTransform(topImageView);
        resetTransform(bottomImageView);

        // Step 3: Determine visual width and crop wider one
        float topWidth = getVisualImageWidth(topImageView);
        float bottomWidth = getVisualImageWidth(bottomImageView);
        float tolerance = 1f;

        if (topWidth > bottomWidth + tolerance) {
            Bitmap cropped = cropHorizontallyToMatch(topVisible, topImageView, bottomWidth);
            topImageView.setImageBitmap(cropped);
        } else if (bottomWidth > topWidth + tolerance) {
            Bitmap cropped = cropHorizontallyToMatch(bottomVisible, bottomImageView, topWidth);
            bottomImageView.setImageBitmap(cropped);
        }

        isCropped = true;

        blendImages();
    }

    private void restoreOriginals() {
        if (!isCropped) return;

        topImageView.setVisibility(View.VISIBLE);
        bottomImageView.setVisibility(View.VISIBLE);
        outputImageView.setVisibility(View.GONE);

        if (originalTopBitmap != null) {
            topImageView.setImageBitmap(originalTopBitmap);
            resetTransform(topImageView);
        }

        if (originalBottomBitmap != null) {
            bottomImageView.setImageBitmap(originalBottomBitmap);
            resetTransform(bottomImageView);
        }

        originalTopBitmap = null;
        originalBottomBitmap = null;
        overlapHeight = 0;
        isCropped = false;
        isBlended = false;
    }

    private Bitmap getCurrentBitmap(ZoomPanImageView imageView) {
        Drawable drawable = imageView.getDrawable();
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }
        return null;
    }

    private void resetTransform(View view) {
        view.setTranslationX(0f);
        view.setTranslationY(0f);
        view.setScaleX(1f);
        view.setScaleY(1f);
    }

    private Bitmap cropVisiblePart(ZoomPanImageView imageView, Bitmap bitmap) {
        if (bitmap == null) return null;

        Rect visibleRect = new Rect();
        if (!imageView.getGlobalVisibleRect(visibleRect)) return null;

        int[] location = new int[2];
        imageView.getLocationOnScreen(location);

        float viewLeft = visibleRect.left - location[0];
        float viewTop = visibleRect.top - location[1];
        float viewRight = visibleRect.right - location[0];
        float viewBottom = visibleRect.bottom - location[1];

        float scaleX = imageView.getScaleX();
        float scaleY = imageView.getScaleY();

        int viewWidth = imageView.getWidth();
        int viewHeight = imageView.getHeight();

        float relLeft = viewLeft / (viewWidth * scaleX);
        float relTop = viewTop / (viewHeight * scaleY);
        float relRight = viewRight / (viewWidth * scaleX);
        float relBottom = viewBottom / (viewHeight * scaleY);

        int bmpX = Math.max(0, (int) (relLeft * bitmap.getWidth()));
        int bmpY = Math.max(0, (int) (relTop * bitmap.getHeight()));
        int bmpW = Math.min(bitmap.getWidth() - bmpX, (int) ((relRight - relLeft) * bitmap.getWidth()));
        int bmpH = Math.min(bitmap.getHeight() - bmpY, (int) ((relBottom - relTop) * bitmap.getHeight()));

        if (bmpW <= 0 || bmpH <= 0) return null;

        return Bitmap.createBitmap(bitmap, bmpX, bmpY, bmpW, bmpH);
    }

    private float getVisualImageWidth(ZoomPanImageView imageView) {
        Drawable drawable = imageView.getDrawable();
        if (drawable == null) return 0;
        int intrinsicWidth = drawable.getIntrinsicWidth();
        float scaleX = imageView.getScaleX();
        return intrinsicWidth * scaleX;
    }

    private Bitmap cropHorizontallyToMatch(Bitmap bitmap, ZoomPanImageView imageView, float targetVisualWidth) {
        if (bitmap == null || targetVisualWidth <= 0) return bitmap;

        float currentVisualWidth = getVisualImageWidth(imageView);
        float scaleRatio = targetVisualWidth / currentVisualWidth;

        int cropWidth = Math.round(bitmap.getWidth() * scaleRatio);
        cropWidth = Math.min(cropWidth, bitmap.getWidth());

        int xOffset = (bitmap.getWidth() - cropWidth) / 2;

        return Bitmap.createBitmap(bitmap, xOffset, 0, cropWidth, bitmap.getHeight());
    }
}
