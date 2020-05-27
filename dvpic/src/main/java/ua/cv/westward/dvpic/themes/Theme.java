package ua.cv.westward.dvpic.themes;

import java.io.File;
import java.io.IOException;

import ua.cv.westward.dvpic.PrefKeys;
import ua.cv.westward.dvpic.R;
import ua.cv.westward.dvpic.utils.BitmapUtils;
import ua.cv.westward.dvpic.utils.FileUtils;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.Display;
import android.view.WindowManager;

public class Theme {

    private Context     mCtx;           // context
    private String      mTheme;         // имя темы
    private ThemeNames  mThemeNames;    // набор имен файлов темы
    private int         mScreenWidth;
    private int         mScreenHeight;
        
    @SuppressWarnings("deprecation")
	public Theme( Context ctx, String theme ) {
        mCtx = ctx;
        
        if( PrefKeys.THEME_DEFAULT.equals( theme ) == false ) {
            mTheme = theme;
        }
        
        int orientation = ctx.getResources().getConfiguration().orientation;        
        if( orientation == Configuration.ORIENTATION_LANDSCAPE ) {
            mThemeNames = ThemeNames.LANDSCAPE;
        } else {
            mThemeNames = ThemeNames.PORTRAIT;
        }
        
        Display display = ((WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        mScreenWidth = display.getWidth();
        mScreenHeight = display.getHeight();
    }
    
    /**
     * Получить фон окна приложения.
     * @return Drawable фоновой картинки.
     * @throws IOException 
     */
    public Drawable getBackground() {
        return mTheme != null ? getThemeBackground() : getDefaultBackground();
    }

    /**
     * Получить фон приложения из темы.
     * @return
     */
    private Drawable getThemeBackground() {
        try {
            File f = getThemeFile( mThemeNames.backgroundImage );
            int max = Math.max( mScreenWidth, mScreenHeight );
            Bitmap bm = BitmapUtils.decodeBitmap( f, max );
            return new BitmapDrawable( mCtx.getResources(), bm );
        } catch( Exception e ) {
            return getDefaultBackground();
        }
    }
    
    /**
     * Получить фон приложения по умолчанию. 
     * @return Drawable фоновой картинки.
     */
    private Drawable getDefaultBackground() {
        return mCtx.getResources().getDrawable( R.drawable.back1 );
    }
    
    /**
     * Получить файловый объект для указанного файла темы. Если файл не найден,
     * выбрасывается исключение.
     * @param fname Имя файла.
     * @return 
     * @throws IOException
     */
    private File getThemeFile( String fname ) throws IOException {
        String themePath = FileUtils.getPathSD( PrefKeys.SD_THEMES_PATH, mTheme );
        File file = new File( themePath, fname );
        if( file.exists() == false ) {
            throw new IOException( "File not found " + file.getAbsolutePath() );
        }
        return file;
    }
        
    /**************************************************************************/
    
    private enum ThemeNames {
        PORTRAIT( "background.jpg" ),
        LANDSCAPE( "background-land.jpg" );
        
        public String backgroundImage;
        
        private ThemeNames( String bgImage ) {
            this.backgroundImage = bgImage;
        }
    }
    
    /**      
     http://stackoverflow.com/questions/4279077/android-set-application-background
     
        // Variable with the path to the background
        String bg_path = "/sdcard/bg/background.png" // <-- This path can be whatever you like

        //Change background of Activity       
        getWindow().setBackgroundDrawable(Drawable.createFromPath(bg_path));
        
        Oh Don't forget to set you Layout background color to transparent in the XML file or you won't see the image. (this is valid to anything that fills parent window like a listview, for instance)
        
        <LinearLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout="@+id/m_layout"
    android:orientation="vertical"
    android:layout_width="fill_parent"
    android:layout_height="fill_parent"
    android.background="Color.TRANSPARENT"
    >
        
    http://stackoverflow.com/questions/4770258/creating-a-drawable-from-sd-card-to-set-as-a-background-in-android
    */
}
