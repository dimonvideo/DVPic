package ua.cv.westward.dvpic.viewer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.VideoView;

import ua.cv.westward.dvpic.R;

// http://www.zdnet.com/blog/burnette/how-to-use-multi-touch-in-android-2-part-6-implementing-the-pinch-zoom-gesture/1847?tag=mantle_skin;content
// http://stackoverflow.com/questions/2537238/how-can-i-get-zoom-functionality-for-images

// http://stackoverflow.com/questions/5198125/how-do-you-find-out-the-scale-of-an-graphics-matrix-in-android

// http://stackoverflow.com/questions/6130285/which-would-be-the-best-to-do-make-a-horizontal-scrolling-app-in-android
// http://stackoverflow.com/questions/6422697/vertical-fling-scrolling-of-text-line-in-android !!!

/**
 * 
 */
public class ImageViewTouch extends androidx.appcompat.widget.AppCompatImageView {
    
    // This matrix is recomputed when we go from the thumbnail image to
    // the full size image.
    private Matrix mBaseMatrix = new Matrix();
    private Matrix mSavedMatrix = new Matrix();

    // The current bitmap being displayed.
    private Bitmap mBitmapDisplayed = null;
    private int mThisWidth = -1;
    private int mThisHeight = -1;

    // Touch event handling
    private enum TouchState { NONE, DRAG, ZOOM; }
    private TouchState mTouchState = TouchState.NONE;
    private PointF mStart = new PointF();
    private PointF mid = new PointF();
    private float oldDistance = 1f;

    // Dynamic scrolling
//    private Scroller mScroller;
    // Determines speed during touch scrolling
//    private VelocityTracker mVelocityTracker;
//    private int mMinimumVelocity;
//    private int mMaximumVelocity;

    // Zooming limits
    private float mMinScale = 1.0f;
    private float mMaxScale = 4.0f;
    
    static final float SCALE_RATE = 1.25F;
    
    protected Runnable mOnLayoutRunnable = null;
    
    protected enum Command { CENTER, MOVE, ZOOM, LAYOUT, RESET; }
    
    public ImageViewTouch( Context context ) {
        super( context );
        init();
    }

    public ImageViewTouch( Context context, AttributeSet attrs )
    {
        super( context, attrs );
        init();    
    }
    
    /**
     * 
     */
    protected void init()
    {
        //Установить тип масштабирования MATRIX - необходимо для трансформации изображений
        setScaleType( ImageView.ScaleType.MATRIX );
//        mScroller = new Scroller( getContext() );
//        ViewConfiguration configuration = ViewConfiguration.get( getContext() );
//        mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
//        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
    }
    
    /**
     * Called from layout when this view should assign a size and position to each of its children.  
     */
    @Override
    protected void onLayout( boolean changed, int left, int top, int right, int bottom ) {
        super.onLayout( changed, left, top, right, bottom );
        
        mThisWidth = right - left;
        mThisHeight = bottom - top;
        
        Runnable r = mOnLayoutRunnable;
        if( r != null ) {
            mOnLayoutRunnable = null;
            r.run();
        }
        if( mBitmapDisplayed != null ) {
            getInitialBaseMatrix( mBitmapDisplayed );
            setImageMatrix( mBaseMatrix );
        }
    }
    
    /**
     * Sets a Bitmap as the content of this ImageView.
     */
    @Override
    public void setImageBitmap( Bitmap bitmap ) {
        super.setImageBitmap( bitmap );
        mBitmapDisplayed = bitmap;
    }
    
    public void clear()
    {
        setImageBitmapReset( null, true );
    }

    /**
     * Увеличить масштаб изображения на фиксированную величину (1,25).
     */
    public void zoomIn() {
        zoomIn( SCALE_RATE );
    }

    /**
     * Уменьшить масштаб изображения на фиксированную величину (1,25).
     */
    public void zoomOut() {
        zoomOut( SCALE_RATE );
    }

    /** DYNAMIC SCROLL/FLING */    
    
/*    @Override
    public void computeScroll() {
        if( !mScroller.isFinished() ) {
            if( mScroller.computeScrollOffset() ) {
                int oldX = getScrollX();
                int oldY = getScrollY();
                int x = mScroller.getCurrX();
                int y = mScroller.getCurrY();

                if( oldX != x || oldY != y ) {
                    scrollTo( x, y );
                }

                // Keep on drawing until the animation has finished.
                invalidate();
                return;
            }
        }
        // Done with scroll, clean up state.
        completeScroll();
    }*/
    
/*    private void endDrag() {
        if( mVelocityTracker != null ) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }*/

/*    private void completeScroll() {
        mScroller.abortAnimation();
        int oldX = getScrollX();
        int oldY = getScrollY();
        int x = mScroller.getCurrX();
        int y = mScroller.getCurrY();
        if( oldX != x || oldY != y ) {
            scrollTo( x, y );
        }
    }*/
    
    /** TOUCH EVENT HANDLING */
        
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent( MotionEvent event ) {



        // handle touch events
        switch( event.getAction() & MotionEvent.ACTION_MASK ) {
        
            case MotionEvent.ACTION_DOWN:
                if( isZoomed() ) {
                    // Disallow ScrollView to intercept touch events.
                    this.getParent().requestDisallowInterceptTouchEvent( true );                
                }
                
                //If being flinged and user touches, stop the fling.
//                completeScroll();
                mSavedMatrix.set( mBaseMatrix );
                // Remember where the motion event started
                mStart.set( event.getX(), event.getY() );
                mTouchState = TouchState.DRAG;
                break;
                
            case MotionEvent.ACTION_POINTER_DOWN:
                oldDistance = spacing( event );
                if( oldDistance > 10f ) {
                    mSavedMatrix.set( mBaseMatrix );
                    midPoint( mid, event );
                    mTouchState = TouchState.ZOOM;
                }
                break;
                
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                
                // Allow ScrollView to intercept touch events.
//                this.getParent().requestDisallowInterceptTouchEvent(false);
                
                mTouchState = TouchState.NONE;
                break;
                
            case MotionEvent.ACTION_MOVE:
                if( mTouchState == TouchState.DRAG ) {
                    mBaseMatrix.set( mSavedMatrix );
                    mBaseMatrix.postTranslate( event.getX() - mStart.x, event.getY() - mStart.y );
                    // limit pan
                    center( true, true );
                } else if( mTouchState == TouchState.ZOOM ) {
                    float newDistance = spacing( event );
                    if( newDistance > 10f ) {
                        mBaseMatrix.set( mSavedMatrix );
                        float scale = newDistance / oldDistance;
                        
                        // limit zoom
                        float currentScale = mBaseMatrix.mapRadius( 1.0f );
                        if( scale * currentScale > mMaxScale ) {
                            scale = mMaxScale / currentScale;
                        } else if( scale * currentScale < mMinScale ) {
                            scale = mMinScale / currentScale;
//                            mZoomed = false;
                        }                        
                        mBaseMatrix.postScale( scale, scale, mid.x, mid.y );
                        // limit pan
                        center( true, true );
                    }
                }
                break;
        }
        
        // perform the image transformation
        setImageMatrix( mBaseMatrix );        
        // indicate event was handled
        return true;
    }

    /**
     * Выяснить, было ли изображение увеличено по сравнению со своими
     * стартовыми размерами.
     * @return
     */
    private boolean isZoomed() {
        float currentScale = mBaseMatrix.mapRadius( 1.0f );
        return currentScale > mMinScale;
    }

//    Log.i( "DVPic", Float.toString( currentScale ));
    
    /**
     * Determine the space between the first two fingers
     */
    private float spacing( MotionEvent event ) {
       float x = event.getX(0) - event.getX(1);
       float y = event.getY(0) - event.getY(1);
       return ( x * x + y * y );
    }

    /**
     * Calculate the mid point of the first two fingers
     */
    private void midPoint( PointF point, MotionEvent event ) {
       float x = event.getX(0) + event.getX(1);
       float y = event.getY(0) + event.getY(1);
       point.set( x / 2, y / 2 );
    }

    /** ZOOM */

    /**
     * Увеличить масштаб изображения.
     * @param rate Коэффициент масштабирования.
     */
    protected void zoomIn( float rate ) {
        if( mBitmapDisplayed == null ) {
            return;
        }
        
        float currentScale = mBaseMatrix.mapRadius( 1.0f );
        if( currentScale >= mMaxScale ) {
            return; // Don't let the user zoom into the molecular level.
        }
        // check max limit for new scale ( newScale = currentScale * rate) 
        if( currentScale * rate > mMaxScale ) {
            rate = mMaxScale / currentScale;
        }

        float cx = getWidth() / 2F;
        float cy = getHeight() / 2F;

        mBaseMatrix.postScale( rate, rate, cx, cy );
        setImageMatrix( mBaseMatrix );
        
        // limit zoom
//        float scale = newDistance / oldDistance;
//        float currentScale = mBaseMatrix.mapRadius( 1.0f );
//        if( scale * currentScale > mMaxScale ) {
//            scale = mMaxScale / currentScale;
//        } else if( scale * currentScale < mMinScale ) {
//        scale = mMinScale / currentScale;
    }

    /**
     * Уменьшить масштаб изображения.
     * @param rate Коэффициент масштабирования.
     */
    protected void zoomOut( float rate ) {
        if( mBitmapDisplayed == null ) {
            return;
        }

        float currentScale = mBaseMatrix.mapRadius( 1.0f );
        if( currentScale <= mMinScale ) {
            return;
        }
        // calculate scale rate for zooming out
        rate = 1F / rate;
        // check min limit for new scale
        if( currentScale * rate < mMinScale ) {
            rate = mMinScale / currentScale;
        }
        
        float cx = getWidth() / 2F;
        float cy = getHeight() / 2F;

        mBaseMatrix.postScale( rate, rate, cx, cy );
        setImageMatrix( mBaseMatrix );
        center( true, true );

        // Zoom out to at most 1x.
/*        Matrix tmp = new Matrix(mSuppMatrix);
        tmp.postScale(1F / rate, 1F / rate, cx, cy);

        if( getScale(tmp) < 1F ) {
            mSuppMatrix.setScale( 1F, 1F, cx, cy );
        } else {
            mSuppMatrix.postScale(1F / rate, 1F / rate, cx, cy);
        }*/
    }    
    
    /**
     * Setup the base matrix so that the image is centered and scaled properly.
     * @param bitmap
     */
    private void getInitialBaseMatrix( Bitmap bitmap ) {
        float viewWidth = getWidth();
        float viewHeight = getHeight();

        float w = bitmap.getWidth();
        float h = bitmap.getHeight();
        mBaseMatrix.reset();

        // We limit up-scaling to 2x otherwise the result may look bad if it's
        // a small icon.
        float widthScale = Math.min( viewWidth / w, 2.0f );
        float heightScale = Math.min( viewHeight / h, 2.0f );
        float scale = Math.min( widthScale, heightScale );

        mMinScale = scale; 
        mBaseMatrix.postScale( scale, scale );
        mBaseMatrix.postTranslate((viewWidth - w * scale) / 2F, (viewHeight - h * scale) / 2F);
    }

    // Center as much as possible in one or both axis. Centering is
    // defined as follows: if the image is scaled down below the
    // view's dimensions then center it (literally). If the image
    // is scaled larger than the view and is translated out of view
    // then translate it back into view (i.e. eliminate black bars).
    protected void center( boolean horizontal, boolean vertical ) {
        if( mBitmapDisplayed == null ) {
            return;
        }

        Matrix m = mBaseMatrix;

        RectF rect = new RectF( 0, 0, mBitmapDisplayed.getWidth(), mBitmapDisplayed.getHeight() );

        m.mapRect(rect);

        float height = rect.height();
        float width = rect.width();

        float deltaX = 0, deltaY = 0;

        if( vertical ) {
            int viewHeight = getHeight();
            if( height < viewHeight ) {
                deltaY = (viewHeight - height) / 2 - rect.top;
            } else if( rect.top > 0 ) {
                deltaY = -rect.top;
            } else if( rect.bottom < viewHeight ) {
                deltaY = getHeight() - rect.bottom;
            }
        }

        if( horizontal ) {
            int viewWidth = getWidth();
            if( width < viewWidth ) {
                deltaX = (viewWidth - width) / 2 - rect.left;
            } else if( rect.left > 0 ) {
                deltaX = -rect.left;
            } else if( rect.right < viewWidth ) {
                deltaX = viewWidth - rect.right;
            }
        }

        mBaseMatrix.postTranslate( deltaX, deltaY );
        setImageMatrix( mBaseMatrix );
    }

    
    /*    public float getScale() {
        return getValue( matrix, Matrix.MSCALE_X );        
    }

    protected float getValue( Matrix matrix, int whichValue )
    {
        matrix.getValues( mMatrixValues );
        return mMatrixValues[whichValue];
    }*/
    
    /** UNDISCOVERED SOURCE LIBRARY CODE */
    
    /**
     * This function changes bitmap, reset base matrix according to the size
     * of the bitmap, and optionally reset the supplementary matrix.
     * @param bitmap
     * @param reset
     */
    public void setImageBitmapReset( final Bitmap bitmap, final boolean reset ) {
        final int viewWidth = getWidth();
        if ( viewWidth <= 0 ) {
            mOnLayoutRunnable = new Runnable() {
                public void run() {
                    setImageBitmapReset( bitmap, reset );
                }
            };
            return;
        }
        
        if ( bitmap != null ) {
            getInitialBaseMatrix( bitmap );
            setImageBitmap( bitmap );
        } else {
            mBaseMatrix.reset();
            setImageBitmap( null );
        }
        
//        if ( reset ) {
//            mSuppMatrix.reset();
//        }
        
        setImageMatrix( mBaseMatrix );
//        mMaxZoom = maxZoom();
    }

    // Sets the maximum zoom, which is a scale relative to the base matrix. It
    // is calculated to show the image at 400% zoom regardless of screen or
    // image orientation. If in the future we decode the full 3 megapixel image,
    // rather than the current 1024x768, this should be changed down to 200%.
    protected float maxZoom() {
        if( mBitmapDisplayed == null ) {
            return 1F;
        }

        float fw = (float) mBitmapDisplayed.getWidth() / (float) mThisWidth;
        float fh = (float) mBitmapDisplayed.getHeight() / (float) mThisHeight;
        float max = Math.max( fw, fh ) * 4;
        return max;
    }

/*    protected void zoomTo( float scale, float centerX, float centerY ) {
        if( scale > mMaxZoom ) {
            scale = mMaxZoom;
        }

        float oldScale = getScale();
        float deltaScale = scale / oldScale;

        mSuppMatrix.postScale(deltaScale, deltaScale, centerX, centerY);
        setImageMatrix(getImageViewMatrix());
        center(true, true);
    }*/

/*    protected void zoomTo(final float scale, final float centerX, final float centerY, final float durationMs) {
        final float incrementPerMs = (scale - getScale()) / durationMs;
        final float oldScale = getScale();
        final long startTime = System.currentTimeMillis();

        mHandler.post(new Runnable() {
            public void run() {
                long now = System.currentTimeMillis();
                float currentMs = Math.min(durationMs, now - startTime);
                float target = oldScale + (incrementPerMs * currentMs);
                zoomTo(target, centerX, centerY);

                if (currentMs < durationMs) {
                    mHandler.post(this);
                }
            }
        });
    }*/

/*    protected void zoomTo(float scale) {
        float cx = getWidth() / 2F;
        float cy = getHeight() / 2F;

        zoomTo(scale, cx, cy);
    }*/

/*    protected void postTranslate(float dx, float dy) {
        mSuppMatrix.postTranslate(dx, dy);
    }*/

/*    protected void panBy(float dx, float dy) {
        postTranslate(dx, dy);
        setImageMatrix(getImageViewMatrix());
    }*/
}
