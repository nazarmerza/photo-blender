package com.nm.blenderapp;

import android.app.Application;
import android.graphics.Bitmap;
import android.net.Uri;


public class App extends Application {

    private Bitmap topBitmap;



    private Bitmap bottomBitmap;

        private Bitmap sharedBitmap;

        private Uri photoUri1;
        private Uri photoUri2;

        public void setSharedBitmap(Bitmap bitmap) {
            this.sharedBitmap = bitmap;
        }

        public Bitmap getSharedBitmap() {
            return sharedBitmap;
        }

    public Bitmap getTopBitmap() {
        return topBitmap;
    }

    public void setTopBitmap(Bitmap topBitmap) {
        this.topBitmap = topBitmap;
    }



    public Bitmap getBottomBitmap() {
        return bottomBitmap;
    }

    public void setBottomBitmap(Bitmap bottomBitmap) {
        this.bottomBitmap = bottomBitmap;
    }
    public Uri getPhotoUri1() {
        return photoUri1;
    }

    public void setPhotoUri1(Uri photoUri1) {
        this.photoUri1 = photoUri1;
    }

    public Uri getPhotoUri2() {
        return photoUri2;
    }

    public void setPhotoUri2(Uri photoUri2) {
        this.photoUri2 = photoUri2;
    }
}

