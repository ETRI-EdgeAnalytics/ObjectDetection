package com.etri.lightnetwork;

import android.graphics.Bitmap;

import com.etri.lightnetwork.tensorflow.Classifier;

import java.util.List;

public class ImageCache {
    static private ImageCache instance;
    private Bitmap bitmap;
    private List<Classifier.Recognition> results;
    private long time;

    static public ImageCache getInstance(){
        if(instance == null) {
            instance = new ImageCache();
        }

        return instance;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bmp) {
        bitmap = bmp;
    }

    public void setResult(List<Classifier.Recognition> results) {
        this.results = results;
    }

    public List<Classifier.Recognition> getResult() {
        return results;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
