package ua.cv.westward.dvpic.widget;

import ua.cv.westward.dvpic.DVPicActivity;
import ua.cv.westward.dvpic.R;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.RemoteViews;

/**
 * Defines the basic methods that allow you to programmatically interface with
 * the App Widget, based on broadcast events. Through it, you will receive
 * broadcasts when the App Widget is updated, enabled, disabled and deleted.
 */
public class IconWidget extends AppWidgetProvider {
        
    @Override
    public void onUpdate( Context context, AppWidgetManager appWidgetManager,
            int[] appWidgetIds ) {
        
        // получить идентификатор виджета, который необходимо обновить
        ComponentName me = new ComponentName( context, IconWidget.class );
        // обновить все экземпляры виджета
        appWidgetManager.updateAppWidget(me, buildUpdate( context, appWidgetIds ));
    }

    private RemoteViews buildUpdate( Context context, int[] appWidgetIds ) {
        // создать виджет, сделать невидимым счетчик
        RemoteViews updateViews = new RemoteViews( context.getPackageName(),
                                                   R.layout.widget_icon2 );
        updateViews.setViewVisibility( R.id.counter, View.GONE );
        
        Intent clickIntent = new Intent( context, DVPicActivity.class );
        PendingIntent clickPI = PendingIntent.getActivity( context, 0,
                clickIntent, PendingIntent.FLAG_UPDATE_CURRENT );        
        updateViews.setOnClickPendingIntent( R.id.widget, clickPI );        
        return updateViews;
    }
}
