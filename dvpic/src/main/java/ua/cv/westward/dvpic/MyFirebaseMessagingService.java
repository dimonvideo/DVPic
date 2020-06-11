package ua.cv.westward.dvpic;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import ua.cv.westward.dvpic.service.WakeLockService;
import ua.cv.westward.dvpic.service.WorkerService;
import ua.cv.westward.dvpic.utils.InternetUtils;


public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Intent i = new Intent( getApplicationContext(), WorkerService.class );
        i.putExtra( PrefKeys.INTENT_SERVICE_CMD, WorkerService.CMD_LOAD_IMAGES );
        i.putExtra( PrefKeys.INTENT_GALLERY_ID, "DV");
        int restartTime;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        restartTime = Integer.parseInt( prefs.getString( PrefKeys.AUTO_RELOAD_TIME, "1" ));
        // проверить наличие сетевого подключения и определить его тип
        NetworkInfo netinfo = InternetUtils.getNetworkInfo( getApplicationContext() );
        if( netinfo != null && netinfo.isConnected() &&
                isConnectionAllowed( getApplicationContext(), netinfo )) {


            if (restartTime > 0) WakeLockService.start(getApplicationContext(), i);
        }

        assert netinfo != null;
      //  Log.v("DVPic", "!!!! ====== NOTICE ======== !!!! " + restartTime);

    }

    @Override
    public void onNewToken(@NonNull String token) {
        Log.d("DVPIC", "Refreshed token: " + token);

    }
    /**
     * Сравнить настройку NETWORK_TYPE и текущий тип подключения, принять решение
     * о запуске сервиса.
     */
    private boolean isConnectionAllowed(Context context, NetworkInfo netinfo ) {
        // получить настройку разрешенной сети передачи данных
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String allowedNetwork = prefs.getString( PrefKeys.NETWORK_TYPE, "WIFI" );
        int connType = netinfo.getType();
        //
       // Log.v("DVPic", "!!!! ====== Network ======== !!!! "+ connType);

        if( allowedNetwork.equals( "BOTH" )) {
            return true;
        } else if( allowedNetwork.equals( "WIFI" ) && connType == ConnectivityManager.TYPE_WIFI ) {
            return true;
        } else return allowedNetwork.equals("GSM") && connType == ConnectivityManager.TYPE_MOBILE;
    }
}
