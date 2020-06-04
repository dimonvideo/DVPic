package ua.cv.westward.dvpic.utils;

import ua.cv.westward.dvpic.R;
import ua.cv.westward.dvpic.service.RepeatAlarmReceiver;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

public class MonitorUtils {
    /**
     * Управление AlarmManager: включить или выключить периодический запуск сервиса. 
     * 
     * @param context  Контекст приложения
     * @param interval Интервал выполнения периодического задания (в миллисекундах)
     *                 или 0, чтобы отключить выполнение.
     */
    public static void setAlarm( Context context, long interval ) {        
        AlarmManager alarmManager = (AlarmManager) context.getSystemService( Context.ALARM_SERVICE );
        Intent intent = new Intent( context, RepeatAlarmReceiver.class );
        PendingIntent pi = PendingIntent.getBroadcast( context, 0, intent, 0 );
        
        if( interval > 0 ) {
            // установить неточный вызов сервиса
            assert alarmManager != null;
            alarmManager.setInexactRepeating( AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() + AlarmManager.INTERVAL_HOUR,
                                              interval, pi );            
            DialogUtils.showToast( context, R.string.msg_alarm_start );
        } else {
            assert alarmManager != null;
            alarmManager.cancel( pi );
            DialogUtils.showToast( context, R.string.msg_alarm_stop );
        }
    }
}
