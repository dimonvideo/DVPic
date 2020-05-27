package ua.cv.westward.dvpic.types;

import ua.cv.westward.dvpic.PrefKeys;
import android.content.SharedPreferences;

public class ViewerOptions extends FlagsSet {

    private static final int OPTION_SHOW_TITLE       = 0x00000001;
    private static final int OPTION_SHOW_SITE        = 0x00000002;
    private static final int OPTION_SHOW_IMAGE_ID    = 0x00000010;
    private static final int OPTION_SHOW_IMAGE_INDEX = 0x00000020;
    private static final int OPTION_SHOW_IMAGE_INFO  = 0x00000040;

    public ViewerOptions( SharedPreferences sp ) {
        String s = sp.getString( PrefKeys.SHOW_IMAGE_TITLE, "TITLE" );
        if( s.equals( "TITLE" )) {
            set( OPTION_SHOW_TITLE );
        }
        if( s.equals( "SITE" )) {
            set( OPTION_SHOW_SITE );
        }
        set( OPTION_SHOW_IMAGE_INDEX, sp.getBoolean( PrefKeys.SHOW_IMAGE_NUMBER, true ));
        set( OPTION_SHOW_IMAGE_INFO, sp.getBoolean( PrefKeys.SHOW_IMAGE_INFO, true ));
    }

    public boolean showTitle() {
        return isSet( OPTION_SHOW_TITLE );
    }

    public boolean showSite() {
        return isSet( OPTION_SHOW_SITE );
    }

    public boolean showImageIndex() {
        return isSet( OPTION_SHOW_IMAGE_INDEX );
    }

    public boolean showImageInfo() {
        return isSet( OPTION_SHOW_IMAGE_INFO );
    }

    public boolean isAnyImageOptions() {
        return isSet( OPTION_SHOW_IMAGE_ID | OPTION_SHOW_IMAGE_INDEX | OPTION_SHOW_IMAGE_INFO );
    }
}
