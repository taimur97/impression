package com.afollestad.impression.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * @author Aidan Follestad (afollestad)
 */
public class ScaleListenerImageView extends ImageView {

    private PhotoViewAttacher mAttacher;
    private boolean checkForChange;

    public ScaleListenerImageView(Context context) {
        super(context);
    }

    public ScaleListenerImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PhotoViewAttacher attachPhotoView() {
        if (mAttacher != null) {
            mAttacher.update();
        } else {
            mAttacher = new PhotoViewAttacher(this);
        }
        return mAttacher;
    }

    @Override
    public void setScaleType(ScaleType scaleType) {
        super.setScaleType(scaleType);
        if (scaleType == ScaleType.MATRIX) {
            checkForChange = true;
        } else if (checkForChange && mAttacher != null) {
            mAttacher.cleanup();
            mAttacher = null;
            Log.v("ScaleListenerImageView", "Destroying PhotoViewAttacher instance.");
        }
    }
}
