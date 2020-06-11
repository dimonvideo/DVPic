package ua.cv.westward.dvpic.viewer;

import java.io.File;
import java.io.IOException;

import ua.cv.westward.dvpic.FlipViewerActivity;
import ua.cv.westward.dvpic.R;
import ua.cv.westward.dvpic.types.ViewerOptions;
import ua.cv.westward.dvpic.utils.BitmapUtils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.MediaController;
import android.widget.VideoView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

/**
 * Compound View, состоящий из ImageViewTouch и UI элементов, показывающих
 * заголовок и статус изображения.
 *
 * @author Vladimir Kuts
 */
@SuppressLint("ViewConstructor")
public class ImageViewer extends BaseViewer {

    private static final int IMAGE_MAX_SIZE = 1024;

    // UI references
    private final ImageViewTouch touchViewer;
    private boolean bVideoIsBeingTouched = false;
    private Handler mHandler = new Handler();

    /**
     * Конструктор класса.
     *
     * @param context Android context
     */
    public ImageViewer(Context context, ViewerOptions options) {
        super(context, options, R.layout.image_viewer_merge);

        // Get references to the child controls.
        touchViewer = (ImageViewTouch) findViewById(R.id.imageView);
    }

    /* PUBLIC SERVICE METHODS */

    @Override
    public void recycle() {
        BitmapDrawable d = (BitmapDrawable) touchViewer.getDrawable();
        if( d != null ) {
            Bitmap bmp = d.getBitmap();
            bmp.recycle();
            bmp = null;
        }
    }

    /* SETTERS */

    /**
     * Увеличить масштаб изображения.
     */
    @Override
    public void zoomIn() {
        touchViewer.zoomIn();
    }

    /**
     * Уменьшить масштаб изображения.
     */
    @Override
    public void zoomOut() {
        touchViewer.zoomOut();
    }
    /**
     * Загрузить указанную картинку из файла. В случае ошибки вывести информацию
     * об ошибке вместо изображения.
     */
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void setImage(String fname) {
        final boolean[] mVolumePlaying = {true};

        String ext = fname.substring(fname.lastIndexOf(".") + 1);
        if (ext.equals("mp4")) {
            VideoView video = findViewById(R.id.videoImage);
            FloatingActionButton fab = findViewById(R.id.fab);
            fab.setVisibility(VISIBLE);
            video.setVisibility(VISIBLE);
            video.setVideoURI(Uri.fromFile(new File(fname)));
            video.requestFocus();
            try {

                video.setOnPreparedListener(mp -> {
                    mp.setVolume(0, 0);
                    mp.setLooping(true);
                    video.start();

                    fab.setOnClickListener(v -> {
                        if (!mVolumePlaying[0]) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                fab.setImageDrawable(getResources().getDrawable(R.drawable.mute, getContext().getTheme()));
                            } else
                                fab.setImageDrawable(getResources().getDrawable(R.drawable.mute));
                            mp.setVolume(0F, 0F);

                        } else {
                            mp.setVolume(0.3F, 0.3F);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                fab.setImageDrawable(getResources().getDrawable(R.drawable.unmute, getContext().getTheme()));
                            } else
                                fab.setImageDrawable(getResources().getDrawable(R.drawable.unmute));


                        }
                        mVolumePlaying[0] = !mVolumePlaying[0];


                    });


                });


            } catch (Throwable ignored) {
                setErrorMessage(getContext().getString(R.string.msg_bitmap_error));
            }

        } else {

            try {
                Bitmap bmp = BitmapUtils.decodeBitmap(new File(fname), IMAGE_MAX_SIZE);
                if (bmp != null) {



                    touchViewer.setImageBitmap(bmp);




                } else {
                    setErrorMessage(getContext().getString(R.string.msg_bitmap_error));
                }
            } catch (IOException e) {
                setErrorMessage(e.getMessage());
            }

        }
    }
}