package ua.cv.westward.dvpic.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import ua.cv.westward.dvpic.R;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;


public class Utils {

    public static String toStr( EditText edit ) {
        return edit.getText().toString().trim();
    }

    public static boolean isEmpty( String str ) {
        return str == null || str.length() == 0;
    }

    public static boolean isEmpty( EditText edit ) {
        return isEmpty( toStr( edit ) );
    }

    /**
     * Изменить формат сообщения об ошибке: убрать префикс исключения
     * @param e Объект исключения
     * @return Результирующее сообщение об ошибке
     */
    public static String shortErrorMessage( Exception e ) {
        StringBuilder sb = new StringBuilder();
        String simpleName = e.getClass().getSimpleName();
        if( simpleName == null || simpleName.length() <= 0 ) {
            sb.append( e.getClass().getName() );
        } else {
            sb.append( simpleName );
        }
        sb.append( ": " );
        sb.append( e.getMessage() );
        return sb.toString();
    }

    /**
     * Get count of the days between the specified and current timestamps
     * @param ts The timestamp to compare with current time
     * @return Number of days between two timestamps
     */
    public static int diffDays( long ts ) {
        long now = System.currentTimeMillis();
        // вычислить разницу между двумя датами в днях
        return (int) ((now - ts) / (24 * 60 * 60 * 1000));
    }

    /**
     * Get count of the hours between the specified and current timestamps
     * @param ts
     * @return
     */
    public static int diffHours( long ts ) {
        long now = System.currentTimeMillis();
        return (int) ((now - ts) / (60 * 60 * 1000));
    }

    /**
     */
    public static int convertDip2Pixels( Context context, int dip ) {
        return (int)TypedValue.applyDimension( TypedValue.COMPLEX_UNIT_DIP,
                dip, context.getResources().getDisplayMetrics() );
    }

    /**
     */
    public static int getDrawable( Context context, String name ) {
        return context.getResources().getIdentifier( name, "drawable", context.getPackageName() );
    }

    /**
     * Switch menu item visibility
     * @param menu
     * @param id MenuItem id
     * @param value true to set menu item visible, false set menu item invisible
     */
    public static void setMenuItemVisible(Menu menu, int id, boolean value ) {
        MenuItem item = menu.findItem( id );
        if( item != null ) {
            item.setVisible( value );
        }
    }

    /**
     * Получить номер версии приложения
     * @return
     */
    public static int getVersion( Context ctx ) {
        PackageManager manager = ctx.getPackageManager();
        String pkg = ctx.getPackageName();
        try {
            PackageInfo info = manager.getPackageInfo( pkg, 0 );
            return info.versionCode;
        } catch( NameNotFoundException e ) {
            return 0;
        }
    }

    /**
     * Получить Version Name уже установленного приложения
     * @return
     */
    public static String getVersionName( Context ctx ) {
        PackageManager manager = ctx.getPackageManager();
        try {
            String pkg = ctx.getPackageName();
            PackageInfo info = manager.getPackageInfo( pkg, 0 );

            StringBuilder sb = new StringBuilder();
            sb.append( ctx.getString( R.string.app_version_title ));
            sb.append( ' ' );
            sb.append( info.versionName );
            return sb.toString();
        } catch( NameNotFoundException e ) {
            return "DVPic";
        }
    }

    /**
     * Format date for UI output
     * @param date
     * @return
     */
    public static String formatDate( long date ) {
        if( date > 0 ) {
            SimpleDateFormat sdf = new SimpleDateFormat();
            return sdf.format( date );
        } else {
            return "";
        }
    }

    /**
     * Smart format date for UI output (with today and yesterday)
     * @param ctx
     * @param date
     * @return
     */
    public static String formatDate( Context ctx, long date ) {
        if( date <= 0 ) {
            return "";
        }

        Calendar today = Calendar.getInstance();
        Calendar d = Calendar.getInstance();
        d.setTimeInMillis( date );

        if( today.get( Calendar.YEAR ) != d.get( Calendar.YEAR )) {
            return formatDate( date );
        }

        // check today
        if( today.get( Calendar.DAY_OF_YEAR ) == d.get( Calendar.DAY_OF_YEAR )) {
            return ctx.getString( R.string.msg_date_today );
        }
        // check yesterday
        today.add( Calendar.DAY_OF_YEAR, -1 ); // yesterday
        if( today.get( Calendar.DAY_OF_YEAR ) == d.get( Calendar.DAY_OF_YEAR )) {
            return ctx.getString( R.string.msg_date_yesterday );
        }
        // other days
        return formatDate( date );
    }
}
