package ua.cv.westward.dvpic.utils;

import ua.cv.westward.dvpic.Preferences;
import ua.cv.westward.dvpic.R;
import ua.cv.westward.dvpic.widget.IconWidget;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

public class DialogUtils {

    /**
     * Вывести продолжительное Toast сообщение
     * @param ctx Context
     * @param resourceId Идентификатор строки сообщения
     */
    public static void showToast( Context ctx, int resourceId ) {
        Toast.makeText( ctx, resourceId, Toast.LENGTH_LONG ).show();
    }
    
    /**
     * Вывести Toast сообщение
     * @param ctx Context
     * @param message Строка сообщения
     */
    public static void showToast( Context ctx, String message ) {
        Toast.makeText( ctx, message, Toast.LENGTH_LONG ).show();
    }

    /**
     * Вывести короткое Toast сообщение
     * @param ctx Context
     * @param resourceId Идентификатор строки сообщения
     */
    public static void showShortToast( Context ctx, int resourceId ) {
        Toast.makeText( ctx, resourceId, Toast.LENGTH_SHORT ).show();
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
     * Обновить виджет приложения.
     * @param count
     */
    public static void updateWidget( Context ctx, int count ) {
        AppWidgetManager manager = AppWidgetManager.getInstance( ctx );
        RemoteViews updateViews = new RemoteViews( ctx.getPackageName(), R.layout.widget_icon2 );
        ComponentName widgetName = new ComponentName( ctx, IconWidget.class );
        
        if( count > 0 ) {
            updateViews.setTextViewText( R.id.counter, Integer.toString(count) );
            updateViews.setViewVisibility( R.id.counter, View.VISIBLE );
        } else {
            updateViews.setViewVisibility( R.id.counter, View.GONE );
        }

        Preferences prefs = Preferences.getInstance( ctx );
        Intent onClickIntent = prefs.getWidgetOnClickIntent();
        
        PendingIntent clickPI = PendingIntent.getActivity( ctx, 0,
                onClickIntent, PendingIntent.FLAG_UPDATE_CURRENT );        
        updateViews.setOnClickPendingIntent( R.id.widget, clickPI );        
        
        manager.updateAppWidget( widgetName, updateViews );
    }    
}
