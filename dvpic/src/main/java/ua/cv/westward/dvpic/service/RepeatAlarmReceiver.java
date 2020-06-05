package ua.cv.westward.dvpic.service;

import ua.cv.westward.dvpic.PrefKeys;
import ua.cv.westward.dvpic.Preferences;
import ua.cv.westward.dvpic.utils.InternetUtils;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import androidx.preference.PreferenceManager;

/**
 * Обработка broadcast сообщения на запуск сервиса
 * 
 * Вызывается в двух случаях: при перезагрузке и при срабатывании AlarmManager.
 * Перед запуском сервиса проверяется, разрешена ли настройка андроид "фоновая
 * передача данных" и имеется ли подключение к сети передачи данных.
 */
public class RepeatAlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive( Context context, Intent intent ) {

            // проверить наличие сетевого подключения и определить его тип
            NetworkInfo netinfo = InternetUtils.getNetworkInfo( context );
            if( netinfo != null && netinfo.isConnected() &&
                    isConnectionAllowed( context, netinfo )) {
                Log.v("DVPic", "!!!! ====== ALARM ======== !!!! ");

                Intent i = new Intent( context, WorkerService.class );
                i.putExtra( PrefKeys.INTENT_SERVICE_CMD, WorkerService.CMD_LOAD_IMAGES );
                i.putExtra( PrefKeys.INTENT_GALLERY_ID, "DV");
                WakeLockService.start( context, i );
            }

    }
    
    /**
     * Сравнить настройку NETWORK_TYPE и текущий тип подключения, принять решение
     * о запуске сервиса.
     */
    private boolean isConnectionAllowed( Context context, NetworkInfo netinfo ) {
        // получить настройку разрешенной сети передачи данных
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        String allowedNetwork = prefs.getString( PrefKeys.NETWORK_TYPE, "WIFI" );
        int connType = netinfo.getType();
        //
        Log.v("DVPic", "!!!! ====== Network ======== !!!! "+ connType);

        if( allowedNetwork.equals( "BOTH" )) {
            return true;
        } else if( allowedNetwork.equals( "WIFI" ) && connType == ConnectivityManager.TYPE_WIFI ) {
            return true;
        } else return allowedNetwork.equals("GSM") && connType == ConnectivityManager.TYPE_MOBILE;
    }
}
