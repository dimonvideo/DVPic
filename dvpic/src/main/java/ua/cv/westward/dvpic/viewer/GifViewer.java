package ua.cv.westward.dvpic.viewer;

import java.io.IOException;

import ua.cv.westward.dvpic.R;
import ua.cv.westward.dvpic.types.ViewerOptions;
import ua.cv.westward.dvpic.utils.AssetsUtils;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.webkit.WebView;

@SuppressLint("InlinedApi")
public class GifViewer extends BaseViewer  {

    private final WebView mWebView;
    private final float mDensity;
    private String mFileName;

    public GifViewer( Context context, ViewerOptions options ) {
        super( context, options, R.layout.gif_viewer_merge );

        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        mDensity = dm.density;

        // Get references to the child controls.
        mWebView = (WebView) findViewById( R.id.webView );

        if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ) {
            mWebView.setLayerType( View.LAYER_TYPE_SOFTWARE, null );
        }
        mWebView.setBackgroundColor( Color.parseColor( "#000000" ));
    }

    @Override
    public void zoomIn() {
        mWebView.zoomIn();
    }

    @Override
    public void zoomOut() {
        mWebView.zoomOut();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        getViewTreeObserver().addOnGlobalLayoutListener(
            new OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    float w = getWidth()-20;
                    float h = getHeight()-20;
                    getViewTreeObserver().removeGlobalOnLayoutListener( this );

                    int width = (int) (w/mDensity);
                    int height = (int) (h/mDensity);

                    try {
                        String html = AssetsUtils.getString( getContext(), "viewer.htm" );
                        String data = String.format( html, width, height, height, "file://" + mFileName );
                        mWebView.loadDataWithBaseURL( null, data, "text/html", "UTF-8", null );
                    } catch( IOException e ) {
                        setErrorMessage( e.getMessage() );
                    }
                }
            }
         );
    }

    @Override
    protected void setImage( String fname ) {
        mFileName = fname;
    }
}
