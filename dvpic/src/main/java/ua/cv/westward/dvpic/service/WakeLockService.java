package ua.cv.westward.dvpic.service;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.PowerManager;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import java.util.Objects;

import ua.cv.westward.dvpic.R;

import static android.content.Intent.getIntent;

public abstract class WakeLockService extends IntentService {

    /** WakeLock - запрос на удержание процессора на время работы сервиса */
    private static volatile PowerManager.WakeLock wakeLock = null;
    @Override
    public void onCreate() {
        super.onCreate();

        int importance = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            importance = NotificationManager.IMPORTANCE_LOW;
        }

        Notification.Builder notificationBuilder = new Notification.Builder(getApplicationContext());
        notificationBuilder
                .setSmallIcon(R.drawable.ic_notify)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.icon))
                .setContentTitle(getString(R.string.app_name)) //
                .setContentText(getString(R.string.msg_download_start)) //
                .setPriority(importance)
                .setOnlyAlertOnce(true)
                .setOngoing(true)
                .setSound(null);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startMyOwnForeground();
        else startForeground(1, notificationBuilder.build());
    }
    // создать объект WakeLock
    synchronized private static PowerManager.WakeLock getWakeLock( Context context ) {
        if( wakeLock == null ) {
            PowerManager pm = (PowerManager) context.getSystemService( Context.POWER_SERVICE );
            assert pm != null;
            wakeLock = pm.newWakeLock( PowerManager.PARTIAL_WAKE_LOCK, context.getString(R.string.waklockpic));
            wakeLock.setReferenceCounted( true );
        }        
        return( wakeLock );
    }    
    
    // запуск сервиса с WakeLock запросом
    public static void start( Context context, Intent intent ) {
        getWakeLock( context );       // запросить WakeLock
        Log.v("DVPic", "!!!! ====== WAKELOCK ======== !!!! ");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        }else {
            context.startService(intent);
        }
    }

    
    // Конструктор класса.    
    public WakeLockService( String name ) {
        super( name );      // имя процесса имеет значение только для отладки          
    }

    @Override
    public int onStartCommand( Intent intent, int flags, int startId ) {
        if( !getWakeLock( getApplicationContext() ).isHeld() ) {  // fail-safe for crash restart
            getWakeLock( getApplicationContext() );
        }
        return super.onStartCommand( intent, flags, startId );
    }

    abstract protected void execute( Intent intent );
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void startMyOwnForeground(){
        int importance = NotificationManager.IMPORTANCE_LOW;
        NotificationChannel chan = new NotificationChannel(getString(R.string.default_notification_channel_id), getString(R.string.default_notification_channel_name), importance);
        chan.setLockscreenVisibility(Notification.VISIBILITY_SECRET);
        chan.setSound(null, null);
        chan.enableVibration(false);
        chan.setShowBadge(false);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getApplicationContext(), getString(R.string.default_notification_channel_id));
        Notification notification = notificationBuilder
                .setSmallIcon(R.drawable.ic_notify)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.icon))
                .setContentTitle(getString(R.string.app_name)) //
                .setContentText(getString(R.string.msg_download_start)) //
                .setPriority(importance)
                .setOnlyAlertOnce(true)
                .setOngoing(true)
                .setSound(null)
                .build();
        startForeground(1, notification);
    }
    @Override
    final protected void onHandleIntent( Intent intent ) {
        try {
            execute( intent );
        }
        finally {
            try {
                if (getWakeLock( getApplicationContext() ).isHeld()) getWakeLock( getApplicationContext() ).release();
            } catch (Throwable th) {
                // ignoring this exception, probably wakeLock was already released

            }
        }
    }    
}
