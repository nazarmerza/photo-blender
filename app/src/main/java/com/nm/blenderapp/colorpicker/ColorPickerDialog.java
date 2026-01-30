package com.nm.blenderapp.colorpicker;

import android.R;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.RelativeLayout;

/* loaded from: classes.dex */
public class ColorPickerDialog extends AlertDialog {
    private ColorPicker colorPickerView;
    private DialogInterface.OnClickListener onClickListener;
    private final OnColorSelectedListener onColorSelectedListener;

    public interface OnColorSelectedListener {
        void onColorSelected(int i);
    }

    public ColorPickerDialog(Context context, int initialColor, OnColorSelectedListener onColorSelectedListener) {
        super(context);
        this.onClickListener = new DialogInterface.OnClickListener() { // from class: com.nm.blenderpro.colorpicker.ColorPickerDialog.1
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case -2:
                        dialog.dismiss();
                        break;
                    case -1:
                        int selectedColor = ColorPickerDialog.this.colorPickerView.getColor();
                        ColorPickerDialog.this.onColorSelectedListener.onColorSelected(selectedColor);
                        break;
                }
            }
        };
        this.onColorSelectedListener = onColorSelectedListener;
        RelativeLayout relativeLayout = new RelativeLayout(context);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(-1, -1);
        layoutParams.addRule(13);
        this.colorPickerView = new ColorPicker(context);
        this.colorPickerView.setColor(initialColor);
        relativeLayout.addView(this.colorPickerView, layoutParams);
        setButton(-1, context.getString(R.string.ok), this.onClickListener);
        setButton(-2, context.getString(R.string.cancel), this.onClickListener);
        setView(relativeLayout);
    }
}
