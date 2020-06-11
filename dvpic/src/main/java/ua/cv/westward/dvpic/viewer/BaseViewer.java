package ua.cv.westward.dvpic.viewer;

import ua.cv.westward.dvpic.R;
import ua.cv.westward.dvpic.site.AppImage;
import ua.cv.westward.dvpic.site.Gallery;
import ua.cv.westward.dvpic.types.ViewerOptions;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public abstract class BaseViewer extends FrameLayout {

    // UI references
    private final ImageView      statusImage;
    private final ImageView      favoriteImage;
    private final TextView       mTitleText;
    private final TextView       mSubtitleText;

    private AppImage mImage;
    private final ViewerOptions mOptions;
    private int mPosition;     // текущая позиция в галерее
    private int mCount;        // количество картинок в галерее


    public BaseViewer( Context context, ViewerOptions options, int layout ) {
        super( context );
        mOptions = options;

        // Inflate the view from the layout resource.
        LayoutInflater li = LayoutInflater.from( context );
        li.inflate( layout, this, true );

        // Get references to the child controls.
        mTitleText = (TextView) findViewById( R.id.title );
        mSubtitleText = (TextView) findViewById( R.id.subtitle );
        statusImage = (ImageView) findViewById( R.id.statusImage );
        favoriteImage = (ImageView) findViewById( R.id.favoriteImage );
    }

    /**
     * Вернуть объект данных изображения
     */
    public AppImage getImage() {
        return mImage;
    }

    /**
     * Вернуть позицию картинки в галерее
     */
    public int getPosition() {
        return mPosition;
    }

    /**
     * Вернуть статус "Избранное" текущего вьювера
     */
    public boolean isFavorite() {
        return mImage.getOption( AppImage.FAVORITE );
    }

    public void recycle() {
        // do nothing here
    }

    public void setData( AppImage imageData, int position, int count ) {
        mImage = imageData;
        mPosition = position;
        mCount = count;

        // load new bitmap
        setImage( imageData.getFilename() );
        // set "new" and "favorite" status indicators
        internalSetStatus( imageData.getStatus() == AppImage.STATUS_NEW );
        internalSetFavorite( imageData.getOption( AppImage.FAVORITE ));
        // title
        if( mOptions.showTitle() ) {
            mTitleText.setText( mImage.getTitle() );
            mTitleText.setVisibility( View.VISIBLE );
        } else {
            mTitleText.setVisibility( View.GONE );
        }
        // subtitle
        if( mOptions.isAnyImageOptions() ) {
            updateSubtitle();
            mSubtitleText.setVisibility( View.VISIBLE );
        } else {
            mSubtitleText.setVisibility( View.GONE );
        }
    }

    /**
     * Установить или снять статус "Новая картинка"
     *
     */
    public void setStatus( int newStatus ) {
        mImage.setViewStatus( newStatus );
        internalSetStatus( newStatus == AppImage.STATUS_NEW );
    }

    /**
     * Установить или снять статус "Избранное"
     */
    public void setFavorite( boolean newValue ) {
        mImage.setOption( AppImage.FAVORITE, newValue );
        internalSetFavorite( newValue );
        // если статус снимается с картинки в галерее Избранное
        if( !newValue && mImage.getSiteID().equals( Gallery.FAV.name() )) {
            // вывести спиннер поверх картинки
            internalSetSpinner();
            // оптимизация совместной работы с адаптером ViewPager: если вьювер
            // будет удален, установить какой-либо признак, по которому адаптер
            // поймет, что незачем искать в курсоре позицию объекта, которого там
            // гарантированно нет
            mPosition = -1;
        }
    }

    /**
     * Вывести сообщение об ошибке.
     * @param str Текст сообщения.
     */
    public void setErrorMessage( String str ) {
        TextView tv = (TextView) findViewById( R.id.message );
        tv.setText( str );
        tv.setVisibility( View.VISIBLE );
    }

    public abstract void zoomIn();

    public abstract void zoomOut();

    /**
     * Уведомление о том, что текущий ImageViewer становится активным.
     * @param position Порядковый номер картинки в галерее.
     * @param count Общее количество картинок в галерее.
     */
    public void notifySetActive( int position, int count ) {
        if( mPosition != position || mCount != count ) {
            mPosition = position;
            mCount = count;
            // subtitle
            if( mOptions.isAnyImageOptions() ) {
                updateSubtitle();
                mSubtitleText.setVisibility( View.VISIBLE );
            } else {
                mSubtitleText.setVisibility( View.GONE );
            }
        }
    }

    /**
     * Уведомление о том, что текущий ImageViewer становится неактивным.
     */
    public void notifySetInactive() {
        internalSetStatus( false );
    }

    protected abstract void setImage( String fname );

    /* INTERNAL */


//    private void updateViewStatus() {
//        if( statusImage != null ) {
//            if( mImage.getStatus() == AppImage.STATUS_NEW ) {
//                statusImage.setVisibility( View.VISIBLE );
//            } else {
//                statusImage.setVisibility( View.INVISIBLE );
//            }
//        }
//    }


    /**
     * Включить или выключить индикатор кнопки "Избранное"
     */
    private void internalSetFavorite( boolean value ) {
        if( favoriteImage != null ) {
            favoriteImage.setVisibility( value ? View.VISIBLE : View.GONE );
        }
    }

    /**
     * Включить или выключить индикатор "Новая картинка"
     */
    private void internalSetStatus( boolean value ) {
        if( statusImage != null ) {
            statusImage.setVisibility( value ? View.VISIBLE : View.GONE );
        }
    }

    /**
     * Вывести ProgressBar в центре вьювера
     */
    private void internalSetSpinner() {
        ProgressBar spinner = new ProgressBar( getContext() );
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.CENTER );
        addView( spinner, params );
    }

    private void updateSubtitle() {
        StringBuilder sb = new StringBuilder();

        // number
        if( mOptions.showImageIndex() ) {
            if( sb.length() > 0 ) {
                sb.append( ", " );
            } else {
                sb.append( "# " );
            }
            sb.append( mPosition + 1 );
            sb.append( " из " );
            sb.append( mCount );
        }
        // info
      //  if( mOptions.showImageInfo() ) {

            // site data
          //  if( mImage.getSiteDate() != null ) {
            //    sb.append( ' ' );
             //   sb.append( mImage.getSiteDate() );
           // }
      //  }
        mSubtitleText.setText( sb.toString() );
    }
}
