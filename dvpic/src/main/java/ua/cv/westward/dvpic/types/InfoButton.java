package ua.cv.westward.dvpic.types;

import ua.cv.westward.dvpic.R;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class InfoButton extends RelativeLayout {

    // UI references
    private final TextView mTitle;
    private final TextView mCounter;

    /**
     * Конструктор, программное создание объекта.
     * @param context
     */
    public InfoButton( Context context ) {
        this( context, null );
    }

    /**
     * Создание объекта через XML layout.
     * @param context
     * @param attrs
     */
    public InfoButton( Context context, AttributeSet attrs ) {
        super( context, attrs );
//        setBackgroundResource( R.drawable.ic_theme_btn );

        // Inflate the view from the layout resource.
        LayoutInflater li;
        li = (LayoutInflater) getContext().getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        li.inflate( R.layout.info_button, this, true );

        // Get references to the child controls.
        mTitle = (TextView) findViewById( R.id.title );
        mCounter = (TextView) findViewById( R.id.counter );

        // Get attributes
        if( attrs != null ) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.dvpic, 0, 0);
            setTitle( a.getText( R.styleable.dvpic_infoButtonTitle ));
            a.recycle();
        }
    }

    public InfoButton( Context context, AttributeSet attrs, int defStyle ) {
        super( context, attrs, defStyle );
//        setBackgroundResource( R.drawable.ic_theme_btn );

        // Inflate the view from the layout resource.
        LayoutInflater li;
        li = (LayoutInflater) getContext().getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        li.inflate( R.layout.info_button, this, true );

        // Get references to the child controls.
        mTitle = (TextView) findViewById( R.id.title );
        mCounter = (TextView) findViewById( R.id.counter );

        // Get attributes
        if( attrs != null ) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.dvpic, 0, 0);
            setTitle( a.getText( R.styleable.dvpic_infoButtonTitle ));
            a.recycle();
        }
    }

    /* SETTERS */

    /**
     * Установить onClick обработчик.
     */
/*    public void setOnClickListener( View.OnClickListener l ) {
        mTitle.setOnClickListener( l );
    }*/

    /**
     * Установить заголовок кнопки.
     */
    public void setTitle( CharSequence title ) {
        mTitle.setText( title );
    }

    /**
     * Установить значение счетчиков для кнопки.
     * @param totalCnt
     * @param newCnt
     */
    public void setCounter( int totalCnt, int newCnt ) {
        if( newCnt > 0 ) {
            mCounter.setText( Integer.toString(newCnt) );
            mCounter.setVisibility( View.VISIBLE );
        } else {
            mCounter.setVisibility( View.GONE );
        }

//        StringBuilder sb = new StringBuilder();
//        if( newCnt > 0 ) {
//            sb.append( getContext().getString( R.string.msg_new_images_counter ));
//            sb.append( ' ' );
//            sb.append( newCnt );
//        }
//        mCounter.setText( sb.toString() );
    }
}

