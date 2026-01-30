#include <jni.h>
#include <string>
#include <android/bitmap.h>
#include <cstdint>



extern "C"
JNIEXPORT void JNICALL
Java_com_nm_blenderapp_BlendActivity_linearBlendNative(
        JNIEnv *env,
        jobject /*thiz*/,
        jobject topBitmap,
        jobject bottomBitmap,
        jobject outputBitmap,
        jint overlapHeight) {

    AndroidBitmapInfo topInfo, bottomInfo, outputInfo;
    void *topPixels = nullptr;
    void *bottomPixels = nullptr;
    void *outputPixels = nullptr;

    // Get bitmap info
    if (AndroidBitmap_getInfo(env, topBitmap, &topInfo) != ANDROID_BITMAP_RESULT_SUCCESS) return;
    if (AndroidBitmap_getInfo(env, bottomBitmap, &bottomInfo) != ANDROID_BITMAP_RESULT_SUCCESS) return;
    if (AndroidBitmap_getInfo(env, outputBitmap, &outputInfo) != ANDROID_BITMAP_RESULT_SUCCESS) return;

    // Validate format and size
    if (topInfo.format != ANDROID_BITMAP_FORMAT_RGBA_8888 ||
        bottomInfo.format != ANDROID_BITMAP_FORMAT_RGBA_8888 ||
        outputInfo.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
        return;
    }

    if (topInfo.width != bottomInfo.width || topInfo.width != outputInfo.width ||
        topInfo.height + bottomInfo.height - overlapHeight != outputInfo.height) {
        return;
    }

    int width = topInfo.width;
    int topHeight = topInfo.height;
    int bottomHeight = bottomInfo.height;

    // Lock bitmaps
    if (AndroidBitmap_lockPixels(env, topBitmap, &topPixels) != ANDROID_BITMAP_RESULT_SUCCESS) return;
    if (AndroidBitmap_lockPixels(env, bottomBitmap, &bottomPixels) != ANDROID_BITMAP_RESULT_SUCCESS) {
        AndroidBitmap_unlockPixels(env, topBitmap);
        return;
    }
    if (AndroidBitmap_lockPixels(env, outputBitmap, &outputPixels) != ANDROID_BITMAP_RESULT_SUCCESS) {
        AndroidBitmap_unlockPixels(env, topBitmap);
        AndroidBitmap_unlockPixels(env, bottomBitmap);
        return;
    }

    uint8_t* topPtr = static_cast<uint8_t*>(topPixels);
    uint8_t* bottomPtr = static_cast<uint8_t*>(bottomPixels);
    uint8_t* outputPtr = static_cast<uint8_t*>(outputPixels);

    // 1. Top region (copy from topBitmap)
    for (int y = 0; y < topHeight - overlapHeight; y++) {
        uint32_t* topLine = (uint32_t*)(topPtr + y * topInfo.stride);
        uint32_t* outputLine = (uint32_t*)(outputPtr + y * outputInfo.stride);
        for (int x = 0; x < width; x++) {
            outputLine[x] = topLine[x];
        }
    }

    // 2. Overlap region (gradient blend from top to bottom)
    for (int y = 0; y < overlapHeight; y++) {
        int topY = topHeight - overlapHeight + y;
        int bottomY = y;
        int outputY = topHeight - overlapHeight + y;

        float topWeight = (float)(overlapHeight - y) / overlapHeight;
        float bottomWeight = (float)y / overlapHeight;

        uint32_t* topLine = (uint32_t*)(topPtr + topY * topInfo.stride);
        uint32_t* bottomLine = (uint32_t*)(bottomPtr + bottomY * bottomInfo.stride);
        uint32_t* outputLine = (uint32_t*)(outputPtr + outputY * outputInfo.stride);

        for (int x = 0; x < width; x++) {
            uint32_t topPixel = topLine[x];
            uint32_t bottomPixel = bottomLine[x];

            uint8_t topA = (topPixel >> 24) & 0xFF;
            uint8_t topR = (topPixel >> 16) & 0xFF;
            uint8_t topG = (topPixel >> 8) & 0xFF;
            uint8_t topB = topPixel & 0xFF;

            uint8_t bottomA = (bottomPixel >> 24) & 0xFF;
            uint8_t bottomR = (bottomPixel >> 16) & 0xFF;
            uint8_t bottomG = (bottomPixel >> 8) & 0xFF;
            uint8_t bottomB = bottomPixel & 0xFF;

            uint8_t blendA = (uint8_t)(topA * topWeight + bottomA * bottomWeight);
            uint8_t blendR = (uint8_t)(topR * topWeight + bottomR * bottomWeight);
            uint8_t blendG = (uint8_t)(topG * topWeight + bottomG * bottomWeight);
            uint8_t blendB = (uint8_t)(topB * topWeight + bottomB * bottomWeight);

            outputLine[x] = (blendA << 24) | (blendR << 16) | (blendG << 8) | blendB;
        }
    }

    // 3. Bottom region (copy from bottomBitmap)
    for (int y = overlapHeight; y < bottomHeight; y++) {
        int outputY = topHeight + y - overlapHeight;
        uint32_t* bottomLine = (uint32_t*)(bottomPtr + y * bottomInfo.stride);
        uint32_t* outputLine = (uint32_t*)(outputPtr + outputY * outputInfo.stride);
        for (int x = 0; x < width; x++) {
            outputLine[x] = bottomLine[x];
        }
    }

    // Unlock
    AndroidBitmap_unlockPixels(env, topBitmap);
    AndroidBitmap_unlockPixels(env, bottomBitmap);
    AndroidBitmap_unlockPixels(env, outputBitmap);
}


extern "C"
JNIEXPORT void JNICALL
Java_com_nm_blenderapp_BlendActivity_processBitmapsNative(
        JNIEnv *env,
        jobject /*thiz*/,
        jobject topBitmap,
        jobject bottomBitmap,
        jobject outputBitmap,
        jint overlapHeight) {

    AndroidBitmapInfo topInfo, bottomInfo, outputInfo;
    void *topPixels = nullptr;
    void *bottomPixels = nullptr;
    void *outputPixels = nullptr;

    // Get bitmap info
    if (AndroidBitmap_getInfo(env, topBitmap, &topInfo) != ANDROID_BITMAP_RESULT_SUCCESS) return;
    if (AndroidBitmap_getInfo(env, bottomBitmap, &bottomInfo) != ANDROID_BITMAP_RESULT_SUCCESS) return;
    if (AndroidBitmap_getInfo(env, outputBitmap, &outputInfo) != ANDROID_BITMAP_RESULT_SUCCESS) return;

    // Validate format and size
    if (topInfo.format != ANDROID_BITMAP_FORMAT_RGBA_8888 ||
        bottomInfo.format != ANDROID_BITMAP_FORMAT_RGBA_8888 ||
        outputInfo.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
        return;
    }

    if (topInfo.width != bottomInfo.width || topInfo.width != outputInfo.width ||
        topInfo.height + bottomInfo.height - overlapHeight != outputInfo.height) {
        return;
    }

    int width = topInfo.width;
    int topHeight = topInfo.height;
    int bottomHeight = bottomInfo.height;

    // Lock bitmaps
    if (AndroidBitmap_lockPixels(env, topBitmap, &topPixels) != ANDROID_BITMAP_RESULT_SUCCESS) return;
    if (AndroidBitmap_lockPixels(env, bottomBitmap, &bottomPixels) != ANDROID_BITMAP_RESULT_SUCCESS) {
        AndroidBitmap_unlockPixels(env, topBitmap);
        return;
    }
    if (AndroidBitmap_lockPixels(env, outputBitmap, &outputPixels) != ANDROID_BITMAP_RESULT_SUCCESS) {
        AndroidBitmap_unlockPixels(env, topBitmap);
        AndroidBitmap_unlockPixels(env, bottomBitmap);
        return;
    }

    // Process output
    uint8_t* topPtr = static_cast<uint8_t*>(topPixels);
    uint8_t* bottomPtr = static_cast<uint8_t*>(bottomPixels);
    uint8_t* outputPtr = static_cast<uint8_t*>(outputPixels);

    // 1. Top region (topBitmap excluding overlap)
    for (int y = 0; y < topHeight - overlapHeight; y++) {
        uint32_t* topLine = (uint32_t*)(topPtr + y * topInfo.stride);
        uint32_t* outputLine = (uint32_t*)(outputPtr + y * outputInfo.stride);
        for (int x = 0; x < width; x++) {
            outputLine[x] = topLine[x];
        }
    }

    // 2. Overlap region (blend top and bottom bitmaps)
    for (int y = 0; y < overlapHeight; y++) {
        int topY = topHeight - overlapHeight + y;
        int bottomY = y;

        uint32_t* topLine = (uint32_t*)(topPtr + topY * topInfo.stride);
        uint32_t* bottomLine = (uint32_t*)(bottomPtr + bottomY * bottomInfo.stride);
        uint32_t* outputLine = (uint32_t*)(outputPtr + (topHeight - overlapHeight + y) * outputInfo.stride);

        for (int x = 0; x < width; x++) {
            uint32_t topPixel = topLine[x];
            uint32_t bottomPixel = bottomLine[x];

            uint8_t topA = (topPixel >> 24) & 0xFF;
            uint8_t topR = (topPixel >> 16) & 0xFF;
            uint8_t topG = (topPixel >> 8) & 0xFF;
            uint8_t topB = topPixel & 0xFF;

            uint8_t bottomA = (bottomPixel >> 24) & 0xFF;
            uint8_t bottomR = (bottomPixel >> 16) & 0xFF;
            uint8_t bottomG = (bottomPixel >> 8) & 0xFF;
            uint8_t bottomB = bottomPixel & 0xFF;

            uint8_t avgA = (topA + bottomA) / 2;
            uint8_t avgR = (topR + bottomR) / 2;
            uint8_t avgG = (topG + bottomG) / 2;
            uint8_t avgB = (topB + bottomB) / 2;

            outputLine[x] = (avgA << 24) | (avgR << 16) | (avgG << 8) | avgB;
        }
    }

    // 3. Bottom region (bottomBitmap excluding overlap)
    for (int y = overlapHeight; y < bottomHeight; y++) {
        int outputY = topHeight + y - overlapHeight;
        uint32_t* bottomLine = (uint32_t*)(bottomPtr + y * bottomInfo.stride);
        uint32_t* outputLine = (uint32_t*)(outputPtr + outputY * outputInfo.stride);
        for (int x = 0; x < width; x++) {
            outputLine[x] = bottomLine[x];
        }
    }

    // Unlock all bitmaps
    AndroidBitmap_unlockPixels(env, topBitmap);
    AndroidBitmap_unlockPixels(env, bottomBitmap);
    AndroidBitmap_unlockPixels(env, outputBitmap);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_nm_blenderapp_BlendActivity_processBitmapsNative2(
        JNIEnv *env,
        jobject thiz,
        jobject topBitmap,
        jobject bottomBitmap,
        jobject outputBitmap,
        jint overlapHeight) {

    // Declare bitmap info structures
    AndroidBitmapInfo topInfo;
    AndroidBitmapInfo bottomInfo;
    AndroidBitmapInfo outputInfo;

    // Declare pixel buffers
    void* topPixels = nullptr;
    void* bottomPixels = nullptr;
    void* outputPixels = nullptr;

    // --- Get Bitmap Info ---
    if (AndroidBitmap_getInfo(env, topBitmap, &topInfo) != ANDROID_BITMAP_RESULT_SUCCESS) return;
    if (AndroidBitmap_getInfo(env, bottomBitmap, &bottomInfo) != ANDROID_BITMAP_RESULT_SUCCESS) return;
    if (AndroidBitmap_getInfo(env, outputBitmap, &outputInfo) != ANDROID_BITMAP_RESULT_SUCCESS) return;

    // --- Validate bitmap dimensions and format (optional but recommended) ---
    if (topInfo.format != ANDROID_BITMAP_FORMAT_RGBA_8888 ||
        bottomInfo.format != ANDROID_BITMAP_FORMAT_RGBA_8888 ||
        outputInfo.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
        return;
    }



    // --- Lock pixels ---
    if (AndroidBitmap_lockPixels(env, topBitmap, &topPixels) != ANDROID_BITMAP_RESULT_SUCCESS) return;
    if (AndroidBitmap_lockPixels(env, bottomBitmap, &bottomPixels) != ANDROID_BITMAP_RESULT_SUCCESS) return;
    if (AndroidBitmap_lockPixels(env, outputBitmap, &outputPixels) != ANDROID_BITMAP_RESULT_SUCCESS) return;

    // Top part copying
    int width = topInfo.width;
    int topHeight = topInfo.height;
    int bottomHeight = bottomInfo.height;

    int outputY = 0;

    // 1. Copy top region (top part of topBitmap above overlap)
    uint32_t* outputLine;
    for (int y = 0; y < topHeight - overlapHeight; y++) {
        uint32_t* topLine = (uint32_t*)((char*)topPixels + y * topInfo.stride);
        outputLine = (uint32_t*)((char*)outputPixels + y * outputInfo.stride);
        for (int x = 0; x < width; x++) {
            outputLine[x] = topLine[x];
        }
    }

//    int rowCounter = 0;
    double factor = 100.0 / overlapHeight / 100.0;
    double multiplier = 0;
    int yB = 0;

    for (int y = topHeight - overlapHeight; y < topHeight; y++) {
        uint32_t* topLine = (uint32_t*)((char*)topPixels + y * topInfo.stride);
        uint32_t* bottomLine = (uint32_t*)((char*)bottomPixels + yB * bottomInfo.stride);
        outputLine = (uint32_t*)((char*)outputPixels + y * outputInfo.stride);

        multiplier = multiplier + factor;

        for (int x = 0; x < width; x++) {
            uint32_t topPixel = topLine[x];
            uint32_t bottomPixel = bottomLine[x];
            uint32_t outputPixel = outputLine[x];

//            uint8_t a = (topPixel >> 24) & 0xFF;
            uint8_t rT = (topPixel >> 16) & 0xFF;
            uint8_t gT = (topPixel >> 8) & 0xFF;
            uint8_t bT = topPixel & 0xFF;

            uint8_t rB = (bottomPixel >> 16) & 0xFF;
            uint8_t gB = (bottomPixel >> 8) & 0xFF;
            uint8_t bB = bottomPixel & 0xFF;

            uint8_t rO, gO, bO;

            uint8_t aO = (outputPixel >> 24) & 0xFF;
            rO = (1 - multiplier) * rT + (rB * multiplier);
            gO = (1 - multiplier) * gT + (gB * multiplier);
            bO = (1 - multiplier) * bT + (bB * multiplier);


            outputLine[x]  = (aO << 24) | (rO << 16) | (gO << 8) | bO;
        }
//        rowCounter++;
    }

    yB = topHeight;
    for (int y = topHeight; y < outputInfo.height; y++) {
        uint32_t* bottomLine = (uint32_t*)((char*)bottomPixels + yB * bottomInfo.stride);
        outputLine = (uint32_t*)((char*)outputPixels + y * outputInfo.stride);

        for (int x = 0; x < width; x++) {
            outputLine[x] = bottomLine[x];
        }
        yB++;
    }

    // --- Unlock pixels ---
    AndroidBitmap_unlockPixels(env, topBitmap);
    AndroidBitmap_unlockPixels(env, bottomBitmap);
    AndroidBitmap_unlockPixels(env, outputBitmap);
}


extern "C"
JNIEXPORT void JNICALL
Java_com_nm_blenderapp_BlendActivity_processSingleBitmap(JNIEnv *env, jobject thiz,
                                                         jobject output_bitmap) {

    AndroidBitmapInfo info; // bitmap info con
    void* pixels = nullptr;

    if (AndroidBitmap_getInfo(env, output_bitmap, &info) != ANDROID_BITMAP_RESULT_SUCCESS) return;
    if (info.format != ANDROID_BITMAP_FORMAT_RGBA_8888) return;

    if (AndroidBitmap_lockPixels(env, output_bitmap, &pixels) != ANDROID_BITMAP_RESULT_SUCCESS) return;

    uint32_t* line;
    for (int y = 0; y < info.height; y++) {
        line = (uint32_t*)((char*)pixels + y * info.stride);
        for (int x = 0; x < info.width; x++) {
            uint32_t pixel = line[x];

            uint8_t a = (pixel >> 24) & 0xFF;
            uint8_t r = (pixel >> 16) & 0xFF;
            uint8_t g = (pixel >> 8) & 0xFF;
            uint8_t b = pixel & 0xFF;

            uint8_t gray = (r + g + b) / 3;

            // Set new grayscale value
            line[x] = (a << 24) | (gray << 16) | (gray << 8) | gray;

        }
    }


    AndroidBitmap_unlockPixels(env, output_bitmap);

}

// noise
// Clamp to 0–255
inline uint8_t clamp(int value) {
    return static_cast<uint8_t>(std::max(0, std::min(255, value)));
}

extern "C"
JNIEXPORT void JNICALL
Java_com_nm_blenderapp_ColorizeActivity_addNoiseNative(
        JNIEnv* env,
        jclass clazz,
        jobject bitmap,
        jint noiseLevel
) {
    AndroidBitmapInfo info;
    void* pixels;

    if (AndroidBitmap_getInfo(env, bitmap, &info) != ANDROID_BITMAP_RESULT_SUCCESS ||
        info.format != ANDROID_BITMAP_FORMAT_RGBA_8888 ||
        AndroidBitmap_lockPixels(env, bitmap, &pixels) != ANDROID_BITMAP_RESULT_SUCCESS) {
        return;
    }

    srand(static_cast<unsigned int>(time(nullptr)));

    for (int y = 0; y < info.height; ++y) {
        uint32_t* line = reinterpret_cast<uint32_t*>((char*)pixels + y * info.stride);
        for (int x = 0; x < info.width; ++x) {
            uint32_t pixel = line[x];

            uint8_t a = (pixel >> 24) & 0xFF;
            int r = (pixel >> 16) & 0xFF;
            int g = (pixel >> 8)  & 0xFF;
            int b = pixel & 0xFF;

            // Add uniform noise [-noiseLevel, +noiseLevel]
            r = clamp(r + (rand() % (2 * noiseLevel + 1) - noiseLevel));
            g = clamp(g + (rand() % (2 * noiseLevel + 1) - noiseLevel));
            b = clamp(b + (rand() % (2 * noiseLevel + 1) - noiseLevel));

            line[x] = (a << 24) | (r << 16) | (g << 8) | b;
        }
    }

    AndroidBitmap_unlockPixels(env, bitmap);
}

static inline float clampf(float v) {
    return v < 0.f ? 0.f : (v > 1.f ? 1.f : v);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_nm_blenderapp_ColorizeActivity_colorize(
        JNIEnv *env,
        jclass /*clazz*/,
        jobject bitmapSrc,
        jobject bitmapDst,
        jint color) {

    AndroidBitmapInfo srcInfo, dstInfo;
    void *srcPixels = nullptr;
    void *dstPixels = nullptr;

    if (AndroidBitmap_getInfo(env, bitmapSrc, &srcInfo) != ANDROID_BITMAP_RESULT_SUCCESS ||
        AndroidBitmap_getInfo(env, bitmapDst, &dstInfo) != ANDROID_BITMAP_RESULT_SUCCESS) {
        return;
    }

    if (srcInfo.format != ANDROID_BITMAP_FORMAT_RGBA_8888 ||
        dstInfo.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
        return;
    }

    if (AndroidBitmap_lockPixels(env, bitmapSrc, &srcPixels) != ANDROID_BITMAP_RESULT_SUCCESS ||
        AndroidBitmap_lockPixels(env, bitmapDst, &dstPixels) != ANDROID_BITMAP_RESULT_SUCCESS) {
        return;
    }

    // ---- Extract picker color (ARGB int from Java) ----
    float r = ((color >> 16) & 0xFF) / 255.f;
    float g = ((color >> 8) & 0xFF) / 255.f;
    float b = (color & 0xFF) / 255.f;

    // Convert picker RGB -> HSV
    float maxv = fmaxf(r, fmaxf(g, b));
    float minv = fminf(r, fminf(g, b));
    float delta = maxv - minv;

    float hue = 0.f;
    float sat = (maxv == 0.f) ? 0.f : (delta / maxv);

    if (delta > 0.f) {
        if (maxv == r) hue = 60.f * fmodf(((g - b) / delta), 6.f);
        else if (maxv == g) hue = 60.f * (((b - r) / delta) + 2.f);
        else hue = 60.f * (((r - g) / delta) + 4.f);
        if (hue < 0.f) hue += 360.f;
    }

    const int width = srcInfo.width;
    const int height = srcInfo.height;

    for (int y = 0; y < height; y++) {
        uint8_t *srcRow = (uint8_t *) srcPixels + y * srcInfo.stride;
        uint8_t *dstRow = (uint8_t *) dstPixels + y * dstInfo.stride;

        for (int x = 0; x < width; x++) {

            uint8_t *sp = srcRow + x * 4;

            // Memory order: [B, G, R, A]
            float bF = sp[0] / 255.f;
            float gF = sp[1] / 255.f;
            float rF = sp[2] / 255.f;

            // ---- Luminance (Photoshop-style) ----
            float lum = 0.299f * rF + 0.587f * gF + 0.114f * bF;

            // ---- Apply color (Color blend mode) ----
            float C = lum * sat;
            float X = C * (1.f - fabsf(fmodf(hue / 60.f, 2.f) - 1.f));
            float m = lum - C;

            float r1, g1, b1;
            if (hue < 60)      { r1 = C; g1 = X; b1 = 0; }
            else if (hue < 120){ r1 = X; g1 = C; b1 = 0; }
            else if (hue < 180){ r1 = 0; g1 = C; b1 = X; }
            else if (hue < 240){ r1 = 0; g1 = X; b1 = C; }
            else if (hue < 300){ r1 = X; g1 = 0; b1 = C; }
            else               { r1 = C; g1 = 0; b1 = X; }

            uint8_t *dp = dstRow + x * 4;
            dp[0] = (uint8_t) (clampf(r1 + m) * 255.f);
            dp[1] = (uint8_t) (clampf(g1 + m) * 255.f);
            dp[2] = (uint8_t) (clampf(b1 + m) * 255.f);
            dp[3] = sp[3]; // preserve alpha
        }
    }

    AndroidBitmap_unlockPixels(env, bitmapSrc);
    AndroidBitmap_unlockPixels(env, bitmapDst);
}



