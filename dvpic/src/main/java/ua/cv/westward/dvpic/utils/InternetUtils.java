package ua.cv.westward.dvpic.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.NonNull;

import ua.cv.westward.dvpic.DVPicActivity;

/**
 * Статические сервисные методы подключения к интернет-ресурсам. 
 * @author Vladimir Kuts
 */
public class InternetUtils {
    
    /**
     * Проверить наличие подключения к Интернет для передачи данных.
     * @param context
     * @return Есть/нет подключение
     */

    public static boolean isConnected(@NonNull Context context) {
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        assert connMgr != null;
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    public static boolean isWifiConnected(@NonNull Context context) {
        return isConnected(context, ConnectivityManager.TYPE_WIFI);
    }

    public static boolean isMobileConnected(@NonNull Context context) {
        return isConnected(context, ConnectivityManager.TYPE_MOBILE);
    }

    private static boolean isConnected(@NonNull Context context, int type) {
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            assert connMgr != null;
            NetworkInfo networkInfo = connMgr.getNetworkInfo(type);
            return networkInfo != null && networkInfo.isConnected();
        } else {
            assert connMgr != null;
            return isConnected(connMgr, type);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static boolean isConnected(@NonNull ConnectivityManager connMgr, int type) {
        Network[] networks = connMgr.getAllNetworks();
        NetworkInfo networkInfo;
        for (Network mNetwork : networks) {
            networkInfo = connMgr.getNetworkInfo(mNetwork);
            if (networkInfo != null && networkInfo.getType() == type && networkInfo.isConnected()) {
                return true;
            }
        }
        return false;
    }





/*
    public static boolean isConnected( Context context ) {


        ConnectivityManager cm = (ConnectivityManager) context.getSystemService( Context.CONNECTIVITY_SERVICE );
        NetworkInfo netinfo = cm.getActiveNetworkInfo();
        if( netinfo != null && netinfo.isConnected() ) {
            return true;
        }
        return false;
    }
*/
    /**
     * Получить информацию об активном сетевом соединении
     * @param context
     * @return Класс NetworkInfo
     */
    public static NetworkInfo getNetworkInfo( Context context ) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService( Context.CONNECTIVITY_SERVICE );
        assert cm != null;
        return cm.getActiveNetworkInfo();
    }
        
    /**
     * Проверить, разрешена ли фоновая передача данных (Android Settings).
     * @param context
     * @return разрешена/запрещена настройка андроид "фоновая передача данных"
     */
    public static boolean isBackgroundDataEnabled( Context context ) {
        return true;
    }    
}
