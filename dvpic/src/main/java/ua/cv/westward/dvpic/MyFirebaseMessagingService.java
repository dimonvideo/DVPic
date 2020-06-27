package ua.cv.westward.dvpic;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import ua.cv.westward.dvpic.service.WakeLockService;
import ua.cv.westward.dvpic.service.WorkerService;


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
        if (getConnectionType(MyFirebaseMessagingService.this) > 0) {
            if (restartTime > 0) WakeLockService.start(MyFirebaseMessagingService.this, i);
        }
        //  Log.v("DVPic", "!!!! ====== NOTICE ======== !!!! " + restartTime);

    }

    @Override
    public void onNewToken(@NonNull String token) {
        Log.d("DVPIC", "Refreshed token: " + token);

    }

    @IntRange(from = 0, to = 3)
    public static int getConnectionType(Context context) {
        int result = 0; // Returns connection type. 0: none; 1: mobile data; 2: wifi
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (cm != null) {
                NetworkCapabilities capabilities = cm.getNetworkCapabilities(cm.getActiveNetwork());
                if (capabilities != null) {
                    if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                        result = 2;
                    } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                        result = 1;
                    } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)) {
                        result = 3;
                    }
                }
            }
        } else {
            if (cm != null) {
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                if (activeNetwork != null) {
                    // connected to the internet
                    if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                        result = 2;
                    } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                        result = 1;
                    } else if (activeNetwork.getType() == ConnectivityManager.TYPE_VPN) {
                        result = 3;
                    }
                }
            }
        }
        return result;
    }
}
