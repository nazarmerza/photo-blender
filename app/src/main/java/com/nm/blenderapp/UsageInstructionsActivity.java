package com.nm.blenderapp;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class UsageInstructionsActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usage);

        LinearLayout backButtonLayout = findViewById(R.id.back_button);
        backButtonLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        TextView usageText = findViewById(R.id.usageText);

        String instructions = "The BlenderPro Application was inspired by some great tutorials and demos on the https://www.photoshopessentials.com website, titled: Blend Photos Like A Hollywood Movie Poster With Photoshop. For details of how these Photoshop actions are performed, refer to that tutorial:\n"
                + "https://www.photoshopessentials.com/photo-effects/photo-blend/\n"
                + "https://www.photoshopessentials.com/photo-effects/blend-photos-cs6/\n\n"
                + "This app, Photo Blender, does exactly that, but only with a few clicks and no need for professional tools or skills.\n\n"
                + "App Usage:\n"
                + "Photo selection: Select two images, top and bottom. For creativity, it is best to blend your photo with something artistic gotten from the web. Then press the Next button.\n\n"
                + "Photo blending: On this page, the two images selected previously are shown. Each image can be moved in any direction and zoomed in and out. Perform these move/zoom operations until the desired proportion of the two images is obtained. Before the blend action, two images must have some overlap region. Blend and Undo can be performed any number of times until the perfect poster is created. Press the Next button to move to the Colorization step.\n\n"
                + "Photo Colorize: Typically, the two photos blended don’t necessarily have the same tone. Colorization helps bring uniformity to the new picture. It works by picking a color and applying that color to the image.\n\n"
                + "Click Picker button: This brings up a color picker dialog. Pick any color — from grayscale to vibrant — but typically what is needed is something close to the existing tone of the image.\n\n"
                + "There is also a noise button on the right side. Each time you click it, it adds some noise to the image. This also helps to uniformize the image. It can be done before or after colorization. You may try both ways to get the best result.";

        usageText.setText(instructions);
    }
}