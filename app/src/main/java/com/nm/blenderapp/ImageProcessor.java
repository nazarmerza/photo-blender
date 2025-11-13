package com.nm.blenderapp;

import android.graphics.Bitmap;
import android.graphics.Color;

import java.util.Random;

public class ImageProcessor {

    public static Bitmap addNoise(Bitmap src, int noiseLevel) {
        Bitmap noisy = src.copy(src.getConfig(), true);
        Random rand = new Random();

        for (int y = 0; y < noisy.getHeight(); y++) {
            for (int x = 0; x < noisy.getWidth(); x++) {
                int pixel = noisy.getPixel(x, y);

                int r = Color.red(pixel);
                int g = Color.green(pixel);
                int b = Color.blue(pixel);

                // Add random noise [-noiseLevel, +noiseLevel]
                r = clamp(r + rand.nextInt(2 * noiseLevel + 1) - noiseLevel);
                g = clamp(g + rand.nextInt(2 * noiseLevel + 1) - noiseLevel);
                b = clamp(b + rand.nextInt(2 * noiseLevel + 1) - noiseLevel);

                noisy.setPixel(x, y, Color.rgb(r, g, b));
            }
        }

        return noisy;
    }

    private static int clamp(int value) {
        return Math.max(0, Math.min(255, value));
    }
}
