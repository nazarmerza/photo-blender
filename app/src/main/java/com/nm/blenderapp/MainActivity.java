package com.nm.blenderapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

import android.widget.LinearLayout;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_PHOTO_1 = 100;
    private static final int REQUEST_CODE_PHOTO_2 = 101;

    private ImageView imageView1, imageView2;
    private LinearLayout btnPhoto1, btnPhoto2, btnNext, btnUsage;
    private boolean isPhoto1Selected = false;
    private boolean isPHoto2Selected = false;

    private App app;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);  // Ensure this matches your XML file

        // Bind views
        imageView1 = findViewById(R.id.imageView1);
        imageView2 = findViewById(R.id.imageView2);
        btnPhoto1 = findViewById(R.id.btn_photo1);
        btnPhoto2 = findViewById(R.id.btn_photo2);
        btnNext = findViewById(R.id.btn_next);
        btnUsage = findViewById(R.id.btn_usage);

        app = (App) getApplication();

        // Set listeners
        btnPhoto1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickImageFromGallery(REQUEST_CODE_PHOTO_1);
            }
        });

        btnPhoto2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickImageFromGallery(REQUEST_CODE_PHOTO_2);
            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPhoto1Selected && isPHoto2Selected) {

                    balanceBitmapDensities(imageView1, imageView2);
                    ((App) getApplication()).setTopBitmap(((BitmapDrawable) imageView1.getDrawable()).getBitmap());
                    ((App) getApplication()).setBottomBitmap(((BitmapDrawable) imageView2.getDrawable()).getBitmap());

                    Intent intent = new Intent(MainActivity.this, BlendActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(MainActivity.this, "You need to select two photos before moving to Blend action", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnUsage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                    Intent intent = new Intent(MainActivity.this, UsageInstructionsActivity.class);
                    startActivity(intent);

            }
        });
    }

    private void pickImageFromGallery(int requestCode) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                if (requestCode == REQUEST_CODE_PHOTO_1) {
                    imageView1.setImageBitmap(bitmap);
//                    ((App) getApplication()).setPhotoUri1(selectedImageUri);
                    isPhoto1Selected = true;
                } else if (requestCode == REQUEST_CODE_PHOTO_2) {
                    imageView2.setImageBitmap(bitmap);
//                    ((App) getApplication()).setPhotoUri2(selectedImageUri);

                    isPHoto2Selected = true;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private void balanceBitmapDensities(final ImageView imageView1, final ImageView imageView2) {

                Bitmap bitmap1 = getCurrentBitmap(imageView1);
                Bitmap bitmap2 = getCurrentBitmap(imageView2);
                if (bitmap1 == null || bitmap2 == null) return;

                int viewWidth1 = imageView1.getWidth();
                int viewHeight1 = imageView1.getHeight();
                int viewWidth2 = imageView2.getWidth();
                int viewHeight2 = imageView2.getHeight();

                if (viewWidth1 == 0 || viewHeight1 == 0 || viewWidth2 == 0 || viewHeight2 == 0) return;

                float density1 = (float)(bitmap1.getWidth() * bitmap1.getHeight()) / (viewWidth1 * viewHeight1);
                float density2 = (float)(bitmap2.getWidth() * bitmap2.getHeight()) / (viewWidth2 * viewHeight2);

                if (Math.abs(density1 - density2) < 0.01f) return; // Already matched

                // Find which one is more dense
                if (density1 > density2) {
                    // Scale bitmap1 down
                    float scale = (float)Math.sqrt(density2 / density1);  // √ for 2D scale
                    Bitmap scaled = scaleBitmap(bitmap1, scale);
                    imageView1.setImageBitmap(scaled);
                } else {
                    // Scale bitmap2 down
                    float scale = (float)Math.sqrt(density1 / density2);
                    Bitmap scaled = scaleBitmap(bitmap2, scale);
                    imageView2.setImageBitmap(scaled);
                }

    }

    private Bitmap scaleBitmap(Bitmap bitmap, float scale) {
        int newWidth = Math.round(bitmap.getWidth() * scale);
        int newHeight = Math.round(bitmap.getHeight() * scale);
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
    }
    private Bitmap getCurrentBitmap(ImageView imageView) {
        Drawable drawable = imageView.getDrawable();
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }
        return null;
    }
}
