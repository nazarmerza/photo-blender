package com.nm.blenderapp;

import android.annotation.TargetApi;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.nm.blenderapp.colorpicker.ColorPickerDialog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import yuku.ambilwarna.AmbilWarnaDialog;

public class ColorizeActivity extends AppCompatActivity implements View.OnClickListener {

    static {
        System.loadLibrary("blenderpro");
    }

    public static native void addNoiseNative(Bitmap bitmap, int noiseLevel);
    public static native void colorize(Bitmap bitmap, Bitmap newBitmap, int color);



    private ImageView imageView;
    private Bitmap originalBitmap;
    private Bitmap newBitmap;

    private LinearLayout pickerButton;
    private LinearLayout undoButton;
    private LinearLayout saveButton;
    private LinearLayout backButton;
    private ImageView noiseButton, denoiseButton;

    private int mDefaultColor = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_colorize);

        pickerButton = findViewById(R.id.picker_button);
        undoButton = findViewById(R.id.undo_button);
        saveButton = findViewById(R.id.save_button);
        backButton = findViewById(R.id.back_button);

        noiseButton = findViewById(R.id.btn_noise);
        denoiseButton = findViewById(R.id.btn_denoise);

        pickerButton.setOnClickListener(this);
        undoButton.setOnClickListener(this);
        saveButton.setOnClickListener(this);
        backButton.setOnClickListener(this);
        noiseButton.setOnClickListener(this);
        denoiseButton.setOnClickListener(this);

        originalBitmap = ((App) getApplication()).getSharedBitmap();

        if (originalBitmap != null) {
            imageView = findViewById(R.id.main_image_view);
            imageView.setImageBitmap(originalBitmap);
        }
    }

    @Override
    public void onClick(View v) {
        int viewId = v.getId();

        if (viewId == R.id.back_button) {
            finish();
        } else if (viewId == R.id.picker_button) {
            openColorPickerDialogue();
//            showColorPickerDialogDemo();

        } else if (viewId == R.id.undo_button) {
            undoColorization();

        } else if (viewId == R.id.save_button) {
            Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
            promptSaveBitmap(bitmap);

        }
        else if (viewId == R.id.btn_noise) {
            Bitmap bm = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
            Bitmap copy = bm.copy(bm.getConfig(), true);
//            newBitmap = ImageProcessor.addNoise(bm, 10);
            addNoiseNative(copy, 10);
            imageView.setImageBitmap(copy);

        } else if (viewId == R.id.btn_denoise) {
            undoColorization();
        }
    }

    private void showColorPickerDialogDemo() {
        ColorPickerDialog colorPickerDialog =
                new ColorPickerDialog(this, -1, this::colorize);
        colorPickerDialog.show();
    }

    private void colorize(int color) {
        undoColorization();
        int argb = Color.argb(255, Color.blue(color), Color.green(color), Color.red(color));
        Bitmap bitmap = ((BitmapDrawable) this.imageView.getDrawable()).getBitmap();
        Bitmap result = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        colorize(bitmap, result, argb);
        this.imageView.setImageBitmap(result);
    }

    public void promptSaveBitmap(final Bitmap bitmap) {
        final EditText input = new EditText(this);
        input.setHint("Enter file name (e.g. my_image)");

        new AlertDialog.Builder(this)
                .setTitle("Save Image")
                .setMessage("Enter file name:")
                .setView(input)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String filename = input.getText().toString().trim();
                        if (!filename.isEmpty()) {

                            filename = filename + ".jpg";
                            boolean saved = saveBitmapToGallery(bitmap, filename);
                            if (saved) {
                                Toast.makeText(getApplicationContext(), "Image saved!", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getApplicationContext(), "Failed to save image.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), "Filename cannot be empty.", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @TargetApi(Build.VERSION_CODES.Q)
    public boolean saveBitmapToGallery(Bitmap bitmap, String filename) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, filename);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES); // saves to /Pictures

        Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        if (uri != null) {
            try {
                OutputStream out = getContentResolver().openOutputStream(uri);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
                out.close();
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public boolean saveBitmapToFile(Bitmap bitmap, String filename) {
        try {
            // Choose a directory to save the image (Pictures folder)
            File picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            if (!picturesDir.exists()) {
                picturesDir.mkdirs();  // create directory if not exists
            }

            // Create file
            File imageFile = new File(picturesDir, filename);  // e.g. "my_image.jpg"

            // Write bitmap to file
            OutputStream out = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();

            return true;  // success
        } catch (Exception e) {
            e.printStackTrace();
            return false; // failure
        }
    }
    private void undoColorization() {
        originalBitmap = ((App) getApplication()).getSharedBitmap();
        imageView.setImageBitmap(originalBitmap);
    }


    public void openColorPickerDialogue() {
        final AmbilWarnaDialog colorPickerDialogue = new AmbilWarnaDialog(this, mDefaultColor,
                new AmbilWarnaDialog.OnAmbilWarnaListener() {
                    @Override
                    public void onCancel(AmbilWarnaDialog dialog) {
                        // Nothing
                    }

                    @Override
                    public void onOk(AmbilWarnaDialog dialog, int color) {
                        mDefaultColor = color;
                        newBitmap = colorize(originalBitmap);
                        imageView.setImageBitmap(newBitmap);
                    }
                });
        colorPickerDialogue.show();
    }

    private Bitmap colorize(Bitmap bitmap) {
        int w = bitmap.getWidth(), h = bitmap.getHeight();
        Bitmap newBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);

        float[] hsv = new float[3];

        colorize(bitmap, newBitmap, mDefaultColor);

        return newBitmap;
    }


}
