package ua.cv.westward.dvpic.service;

import ua.cv.westward.dvpic.PrefKeys;
import ua.cv.westward.dvpic.utils.MonitorUtils;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

/**
 * Обработка системного broadcast сообщения BOOT_COMPLETED.
 * 
 * После перезапуска система автоматически не восстанавливает ранее назначенные
 * с помощью AlarmManager задания. Поэтому, если указан интервал проверки
 * мониторов, его нужно заново установить.
 */
public class OnBootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive( Context context, Intent intent ) {
        SharedPreferences prefs = context.getSharedPreferences( PrefKeys.NAME, Context.MODE_PRIVATE );
        int restartTime;
        try {
            restartTime = Integer.parseInt( prefs.getString( PrefKeys.AUTO_RELOAD_TIME, "0" ));
        } catch( Exception e ) {
            restartTime = 0;
        }
        if( restartTime > 0 ) {
            MonitorUtils.setAlarm( context, restartTime );
        }
    }
}
