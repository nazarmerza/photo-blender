#include <jni.h>
#include <string>
#include <android/bitmap.h>
#include <cstdint>

#include <jni.h>
#include <android/bitmap.h>
#include <cstdint>

#include <jni.h>
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