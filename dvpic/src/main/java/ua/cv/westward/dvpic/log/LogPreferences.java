package ua.cv.westward.dvpic.log;

import ua.cv.westward.dvpic.PrefKeys;
import android.content.Context;
import android.content.SharedPreferences;

public class LogPreferences {

    public static void setErrorFlag( Context context, boolean value ) {
        SharedPreferences prefs = context.getSharedPreferences( PrefKeys.NAME, Context.MODE_PRIVATE );
        SharedPreferences.Editor ed = prefs.edit();
        ed.putBoolean( PrefKeys.ERROR_FLAG, value );
        ed.commit();
    }
    
    public static boolean getErrorFlag( Context context ) {
        SharedPreferences prefs = context.getSharedPreferences( PrefKeys.NAME, Context.MODE_PRIVATE );
        return prefs.getBoolean( PrefKeys.ERROR_FLAG, false );
    }
}
