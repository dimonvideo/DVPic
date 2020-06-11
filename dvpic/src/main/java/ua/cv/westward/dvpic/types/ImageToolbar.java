package ua.cv.westward.dvpic.types;

import ua.cv.westward.dvpic.R;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;

public class ImageToolbar extends RelativeLayout implements OnClickListener {

    public static final int ID_BACK = 1;
    public static final int ID_FORWARD = 2;
    public static final int ID_ZOOM_IN = 3;
    public static final int ID_ZOOM_OUT = 4;

    private OnClickListener mOnClickListener = null;

    public ImageToolbar( Context context ) {
        this( context, null );
    }

    public ImageToolbar( Context context, AttributeSet attrs ) {
        super( context, attrs );

        // Inflate the view from the layout resource.
        LayoutInflater li;
        li = (LayoutInflater) getContext().getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        assert li != null;
        li.inflate( R.layout.image_toolbar, this, true );


        // set tags and onClick listeners
        View back = findViewById( R.id.back_btn );
        back.setOnClickListener( this );
        back.setTag( ID_BACK );

        View forward = findViewById( R.id.forward_btn );
        forward.setOnClickListener( this );
        forward.setTag( ID_FORWARD );

        View in = findViewById( R.id.zoom_in );
        in.setOnClickListener( this );
        in.setTag( ID_ZOOM_IN );

        View out = findViewById( R.id.zoom_out );
        out.setOnClickListener( this );
        out.setTag( ID_ZOOM_OUT );
    }

    @Override
    public void onClick( View v ) {
        if( mOnClickListener != null ) {
            mOnClickListener.onClick( v );
        }
    }

    @Override
    public void setOnClickListener( OnClickListener listener ) {
        mOnClickListener = listener;
    }

}
