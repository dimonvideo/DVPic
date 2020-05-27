package ua.cv.westward.dvpic.viewer;

import java.io.File;
import java.io.IOException;

import ua.cv.westward.dvpic.R;
import ua.cv.westward.dvpic.types.ViewerOptions;
import ua.cv.westward.dvpic.utils.BitmapUtils;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;

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
     * @param fname
     */
    @Override
    protected void setImage( String fname ) {
        try {
            Bitmap bmp = BitmapUtils.decodeBitmap( new File(fname), IMAGE_MAX_SIZE );
            if( bmp != null ) {
                touchViewer.setImageBitmap( bmp );
            } else {
                setErrorMessage( getContext().getString( R.string.msg_bitmap_error ));
            }
        } catch( IOException e ) {
            setErrorMessage( e.getMessage() );
        }
    }
}
