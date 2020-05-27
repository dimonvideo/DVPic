package ua.cv.westward.dvpic.utils;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.view.WindowManager;

public class ActivityUtils {

    public enum OrientationType {
        AUTO( ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED ),
        PORTRAIT( ActivityInfo.SCREEN_ORIENTATION_PORTRAIT ),
        LANDSCAPE( ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE );
        
        private final int orientation;
        
        private OrientationType( int orientation ) {
            this.orientation = orientation;
        }
        
        public int getValue() { return orientation; }
    }
    
    /**
     * Установить новое значение ориентации экрана для указанной Activity.
     * @param activity
     * @param value Новое значение ориентации (настройка из Shared Preferences)
     */
    public static void setOrientation( Activity activity, String value ) {
        OrientationType orientation;
        try {
            orientation = OrientationType.valueOf( value );
        } catch( Exception e ) {
            orientation = OrientationType.AUTO;
        }
        activity.setRequestedOrientation( orientation.getValue() );
    }
    
    /**
     * Установить полноэкранный режим для указанной Activity.
     * @param activity
     * @param value
     */
    public static void setFullscreen( Activity activity, boolean value ) {
        if( value ) {
            activity.getWindow().setFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN,
                                           WindowManager.LayoutParams.FLAG_FULLSCREEN );
        }        
    }
}
