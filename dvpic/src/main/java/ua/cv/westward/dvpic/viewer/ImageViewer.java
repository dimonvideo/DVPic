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
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.MediaController;
import android.widget.VideoView;

/**
 * Compound View, состоящий из ImageViewTouch и UI элементов, показывающих
 * заголовок и статус изображения.
 *
 * @author Vladimir Kuts
 */
public class ImageViewer extends BaseViewer {

    private static final int IMAGE_MAX_SIZE = 1024;

    // UI references
    private final ImageViewTouch touchViewer;
    private boolean bVideoIsBeingTouched = false;
    private Handler mHandler = new Handler();

    /**
     * Конструктор класса.
     * @param context Android context
     */
    public ImageViewer( Context context, ViewerOptions options ) {
        super( context, options, R.layout.image_viewer_merge );

        // Get references to the child controls.
        touchViewer = (ImageViewTouch) findViewById( R.id.imageView );
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

    @Override
    public void zoomIn() {

    }

    @Override
    public void zoomOut() {

    }

    /* SETTERS */

    /**
     * Увеличить масштаб изображения.
     */


    /**
     * Уменьшить масштаб изображения.
     */

    /**
     * Загрузить указанную картинку из файла. В случае ошибки вывести информацию
     * об ошибке вместо изображения.
     * @param fname
     */
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void setImage( String fname ) {

        String ext = fname.substring(fname.lastIndexOf(".") + 1);
        if (ext.equals("mp4")) {
            VideoView video = (VideoView) findViewById(R.id.videoImage);


            try {
                video.setVisibility(VISIBLE);
                video.setVideoURI(Uri.parse(fname));
            video.requestFocus();
                video.setOnPreparedListener(mp -> video.start());
                video.setOnTouchListener(
                        new View.OnTouchListener()
                        {
                            @Override
                            public boolean onTouch(View v, MotionEvent motionEvent)
                            {
                                if (video.isPlaying())
                                {
                                    video.stopPlayback();


                  /*

                  Ok, so now you want to use that show(), preferrably without the int timeout

                  I didn't add it in myself but you should be able to handle it yourself

                  */

                                    return true;
                                }
                                else /* I changed it to else, change it to if else if you have something specific you want to deal with */
                                {

                  /*

                  I would use that hide method I linked here, then start the
                  video, I assume you know how to play the video yourself

                  */

                                }

                                return false;
                            }
                        });
            } catch (Throwable ignored) {
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
