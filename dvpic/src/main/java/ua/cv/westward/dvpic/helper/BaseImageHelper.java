package ua.cv.westward.dvpic.helper;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.UserHandle;
import android.view.Display;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;

import javax.net.ssl.HttpsURLConnection;

import ua.cv.westward.dvpic.db.DBAdapter;
import ua.cv.westward.dvpic.helper.net.HttpHelper;
import ua.cv.westward.dvpic.helper.retrier.RetriableTask;
import ua.cv.westward.dvpic.site.AbstractImage;
import ua.cv.westward.dvpic.site.NetworkOptions;
import ua.cv.westward.dvpic.site.SiteParameters;
import ua.cv.westward.dvpic.utils.FileUtils;
import ua.cv.westward.dvpic.utils.InternetUtils;

/**
 * Базовый абстрактный класс загрузки изображений с веб-сайта.
 */
public abstract class BaseImageHelper extends Context {

    private final DBAdapter mDBAdapter;
    private final HttpHelper mHttpHelper;
    private int mConnectionType;

    protected final SiteParameters mSiteParams;   // Параметры сайта, подключения к сети

    /**
     * Конструктор класса.
     */
    public BaseImageHelper( Context context, SiteParameters params ) {
        mSiteParams = params;
        mDBAdapter = DBAdapter.getInstance( context );
        mHttpHelper = HttpHelper.getInstance( context );

        NetworkInfo netinfo = InternetUtils.getNetworkInfo( context );
        if( netinfo != null ) {
            mConnectionType = netinfo.getType();
        } else {
            mConnectionType = -1;
        }
    }

    /**
     * Загрузить новые изображения с сайта. Загрузка выполняется в обратном
     * порядке для того, чтобы самые новые изображения получили более позднюю
     * отметку даты и времени.
     */
    public int downloadImages() throws Exception {
        int count = 0;
        int max = mSiteParams.getStorageSize() > 50 ? 50 : 50;
       // sendProgressMessage( mSiteParams.getSiteTitle(), max, count );

        // Загрузить список изображений с сайта
        List<? extends AbstractImage> images = getImagesList();
        // вычислить, сколько изображений будет обрабатываться
        if( images.size() < max ) {
            max = images.size();
        }
        Intent intent = new Intent("myBroadcast");
        intent.putExtra("progressmax", String.valueOf(images.size())); // progress update


        // generate an iterator, start just after the last element
        ListIterator<? extends AbstractImage> li = images.listIterator( max );
        // iterate in reverse
        while( li.hasPrevious() ) {
            AbstractImage image = li.previous();
            intent.putExtra("progress", String.valueOf(count+1)); // progress update
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
            // determine image type
            String type = getImageType( image.getLink() );
            if( type.equals( "gif" )) {
                image.setOptions( AbstractImage.GIF_IMAGE );
                if( !isNetworkAllowed( mSiteParams.getAllowedGifNetwork() )) {
                    // skip this gif image because it isn't allowed
                    continue;
                }
            }
            // проверить, было ли изображение загружено ранее
            if(!mDBAdapter.isImageExists(image)) {
                // загрузить изображение, добавить информацию в базу данных
                downloadImage( image );
                mDBAdapter.insertImage( image );
                count += 1;

            }
        }
        return count;
    }



    /**
     * Получить список изображений сайта
     */
    protected abstract List<? extends AbstractImage> getImagesList() throws Exception;

    /**
     * Load image, repeat task 5 times with 1000 ms delay
     */
    private void downloadImage( final AbstractImage image ) throws Exception {
        new RetriableTask<Void>(() -> {
            getImage( image );
            return null;
        }).call();
    }

    /**
     * Загрузить изображение
     */
    protected void getImage( AbstractImage image ) throws IOException {
        // получить объект файловой системы
        String path = mSiteParams.getImagesFolder();
        String ext = image.getOption( AbstractImage.GIF_IMAGE ) ? "gif" : "jpg";
        String fname = FileUtils.makeFileName( path, image.getImageID(), ext );

        mHttpHelper.getFile( image.getLink(), new File( fname ));
        // сохранить полное имя файла, установить отметку даты загрузки
        image.setFilename( fname );
    }

    private String getImageType( String link ) {
        int i = link.lastIndexOf( '.' );
        if( i > 0 && i < link.length()-1 ) {
            return link.substring( i + 1 ).toLowerCase( Locale.US );
        } else {
            return "<Unknown>";
        }
    }

    private boolean isNetworkAllowed( NetworkOptions option ) {
        if( option == NetworkOptions.ANY ) {
            return true;
        } else if( option == NetworkOptions.WIFI && mConnectionType == ConnectivityManager.TYPE_WIFI ) {
            return true;
        } else if( option == NetworkOptions.GSM && mConnectionType == ConnectivityManager.TYPE_MOBILE ) {
            return true;
        } else {
            return false;
        }
    }

    /* HttpHelper wrapper methods */

    protected HttpsURLConnection openConnection(String url ) throws IOException {
        return (HttpsURLConnection) mHttpHelper.openConnection( url );
    }

    protected HttpsURLConnection newConnection( String url ) throws IOException {
        return (HttpsURLConnection) mHttpHelper.newConnection( url );
    }

    protected void checkResponse( HttpsURLConnection connection ) throws IOException {
        mHttpHelper.checkResponse( connection );
    }

    @Override
    public AssetManager getAssets() {
        return null;
    }

    @Override
    public Resources getResources() {
        return null;
    }

    @Override
    public PackageManager getPackageManager() {
        return null;
    }

    @Override
    public ContentResolver getContentResolver() {
        return null;
    }

    @Override
    public Looper getMainLooper() {
        return null;
    }

    @Override
    public Context getApplicationContext() {
        return null;
    }

    @Override
    public void setTheme(int resid) {

    }

    @Override
    public Resources.Theme getTheme() {
        return null;
    }

    @Override
    public ClassLoader getClassLoader() {
        return null;
    }

    @Override
    public String getPackageName() {
        return null;
    }

    @Override
    public ApplicationInfo getApplicationInfo() {
        return null;
    }

    @Override
    public String getPackageResourcePath() {
        return null;
    }

    @Override
    public String getPackageCodePath() {
        return null;
    }

    @Override
    public SharedPreferences getSharedPreferences(String name, int mode) {
        return null;
    }

    @Override
    public boolean moveSharedPreferencesFrom(Context sourceContext, String name) {
        return false;
    }

    @Override
    public boolean deleteSharedPreferences(String name) {
        return false;
    }

    @Override
    public FileInputStream openFileInput(String name) throws FileNotFoundException {
        return null;
    }

    @Override
    public FileOutputStream openFileOutput(String name, int mode) throws FileNotFoundException {
        return null;
    }

    @Override
    public boolean deleteFile(String name) {
        return false;
    }

    @Override
    public File getFileStreamPath(String name) {
        return null;
    }

    @Override
    public File getDataDir() {
        return null;
    }

    @Override
    public File getFilesDir() {
        return null;
    }

    @Override
    public File getNoBackupFilesDir() {
        return null;
    }

    @Nullable
    @Override
    public File getExternalFilesDir(@Nullable String type) {
        return null;
    }

    @Override
    public File[] getExternalFilesDirs(String type) {
        return new File[0];
    }

    @Override
    public File getObbDir() {
        return null;
    }

    @Override
    public File[] getObbDirs() {
        return new File[0];
    }

    @Override
    public File getCacheDir() {
        return null;
    }

    @Override
    public File getCodeCacheDir() {
        return null;
    }

    @Nullable
    @Override
    public File getExternalCacheDir() {
        return null;
    }

    @Override
    public File[] getExternalCacheDirs() {
        return new File[0];
    }

    @Override
    public File[] getExternalMediaDirs() {
        return new File[0];
    }

    @Override
    public String[] fileList() {
        return new String[0];
    }

    @Override
    public File getDir(String name, int mode) {
        return null;
    }

    @Override
    public SQLiteDatabase openOrCreateDatabase(String name, int mode, SQLiteDatabase.CursorFactory factory) {
        return null;
    }

    @Override
    public SQLiteDatabase openOrCreateDatabase(String name, int mode, SQLiteDatabase.CursorFactory factory, @Nullable DatabaseErrorHandler errorHandler) {
        return null;
    }

    @Override
    public boolean moveDatabaseFrom(Context sourceContext, String name) {
        return false;
    }

    @Override
    public boolean deleteDatabase(String name) {
        return false;
    }

    @Override
    public File getDatabasePath(String name) {
        return null;
    }

    @Override
    public String[] databaseList() {
        return new String[0];
    }

    @Override
    public Drawable getWallpaper() {
        return null;
    }

    @Override
    public Drawable peekWallpaper() {
        return null;
    }

    @Override
    public int getWallpaperDesiredMinimumWidth() {
        return 0;
    }

    @Override
    public int getWallpaperDesiredMinimumHeight() {
        return 0;
    }

    @Override
    public void setWallpaper(Bitmap bitmap) throws IOException {

    }

    @Override
    public void setWallpaper(InputStream data) throws IOException {

    }

    @Override
    public void clearWallpaper() throws IOException {

    }

    @Override
    public void startActivity(Intent intent) {

    }

    @Override
    public void startActivity(Intent intent, @Nullable Bundle options) {

    }

    @Override
    public void startActivities(Intent[] intents) {

    }

    @Override
    public void startActivities(Intent[] intents, Bundle options) {

    }

    @Override
    public void startIntentSender(IntentSender intent, @Nullable Intent fillInIntent, int flagsMask, int flagsValues, int extraFlags) throws IntentSender.SendIntentException {

    }

    @Override
    public void startIntentSender(IntentSender intent, @Nullable Intent fillInIntent, int flagsMask, int flagsValues, int extraFlags, @Nullable Bundle options) throws IntentSender.SendIntentException {

    }

    @Override
    public void sendBroadcast(Intent intent) {

    }

    @Override
    public void sendBroadcast(Intent intent, @Nullable String receiverPermission) {

    }

    @Override
    public void sendOrderedBroadcast(Intent intent, @Nullable String receiverPermission) {

    }

    @Override
    public void sendOrderedBroadcast(@NonNull Intent intent, @Nullable String receiverPermission, @Nullable BroadcastReceiver resultReceiver, @Nullable Handler scheduler, int initialCode, @Nullable String initialData, @Nullable Bundle initialExtras) {

    }

    @Override
    public void sendBroadcastAsUser(Intent intent, UserHandle user) {

    }

    @Override
    public void sendBroadcastAsUser(Intent intent, UserHandle user, @Nullable String receiverPermission) {

    }

    @Override
    public void sendOrderedBroadcastAsUser(Intent intent, UserHandle user, @Nullable String receiverPermission, BroadcastReceiver resultReceiver, @Nullable Handler scheduler, int initialCode, @Nullable String initialData, @Nullable Bundle initialExtras) {

    }

    @Override
    public void sendStickyBroadcast(Intent intent) {

    }

    @Override
    public void sendStickyOrderedBroadcast(Intent intent, BroadcastReceiver resultReceiver, @Nullable Handler scheduler, int initialCode, @Nullable String initialData, @Nullable Bundle initialExtras) {

    }

    @Override
    public void removeStickyBroadcast(Intent intent) {

    }

    @Override
    public void sendStickyBroadcastAsUser(Intent intent, UserHandle user) {

    }

    @Override
    public void sendStickyOrderedBroadcastAsUser(Intent intent, UserHandle user, BroadcastReceiver resultReceiver, @Nullable Handler scheduler, int initialCode, @Nullable String initialData, @Nullable Bundle initialExtras) {

    }

    @Override
    public void removeStickyBroadcastAsUser(Intent intent, UserHandle user) {

    }

    @Nullable
    @Override
    public Intent registerReceiver(@Nullable BroadcastReceiver receiver, IntentFilter filter) {
        return null;
    }

    @Nullable
    @Override
    public Intent registerReceiver(@Nullable BroadcastReceiver receiver, IntentFilter filter, int flags) {
        return null;
    }

    @Nullable
    @Override
    public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter, @Nullable String broadcastPermission, @Nullable Handler scheduler) {
        return null;
    }

    @Nullable
    @Override
    public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter, @Nullable String broadcastPermission, @Nullable Handler scheduler, int flags) {
        return null;
    }

    @Override
    public void unregisterReceiver(BroadcastReceiver receiver) {

    }

    @Nullable
    @Override
    public ComponentName startService(Intent service) {
        return null;
    }

    @Nullable
    @Override
    public ComponentName startForegroundService(Intent service) {
        return null;
    }

    @Override
    public boolean stopService(Intent service) {
        return false;
    }

    @Override
    public boolean bindService(Intent service, @NonNull ServiceConnection conn, int flags) {
        return false;
    }

    @Override
    public void unbindService(@NonNull ServiceConnection conn) {

    }

    @Override
    public boolean startInstrumentation(@NonNull ComponentName className, @Nullable String profileFile, @Nullable Bundle arguments) {
        return false;
    }

    @Override
    public Object getSystemService(@NonNull String name) {
        return null;
    }

    @Nullable
    @Override
    public String getSystemServiceName(@NonNull Class<?> serviceClass) {
        return null;
    }

    @SuppressLint("WrongConstant")
    @Override
    public int checkPermission(@NonNull String permission, int pid, int uid) {
        return 0;
    }

    @SuppressLint("WrongConstant")
    @Override
    public int checkCallingPermission(@NonNull String permission) {
        return 0;
    }

    @SuppressLint("WrongConstant")
    @Override
    public int checkCallingOrSelfPermission(@NonNull String permission) {
        return 0;
    }

    @SuppressLint("WrongConstant")
    @Override
    public int checkSelfPermission(@NonNull String permission) {
        return 0;
    }

    @Override
    public void enforcePermission(@NonNull String permission, int pid, int uid, @Nullable String message) {

    }

    @Override
    public void enforceCallingPermission(@NonNull String permission, @Nullable String message) {

    }

    @Override
    public void enforceCallingOrSelfPermission(@NonNull String permission, @Nullable String message) {

    }

    @Override
    public void grantUriPermission(String toPackage, Uri uri, int modeFlags) {

    }

    @Override
    public void revokeUriPermission(Uri uri, int modeFlags) {

    }

    @Override
    public void revokeUriPermission(String toPackage, Uri uri, int modeFlags) {

    }

    @SuppressLint("WrongConstant")
    @Override
    public int checkUriPermission(Uri uri, int pid, int uid, int modeFlags) {
        return 0;
    }

    @SuppressLint("WrongConstant")
    @Override
    public int checkCallingUriPermission(Uri uri, int modeFlags) {
        return 0;
    }

    @SuppressLint("WrongConstant")
    @Override
    public int checkCallingOrSelfUriPermission(Uri uri, int modeFlags) {
        return 0;
    }

    @SuppressLint("WrongConstant")
    @Override
    public int checkUriPermission(@Nullable Uri uri, @Nullable String readPermission, @Nullable String writePermission, int pid, int uid, int modeFlags) {
        return 0;
    }

    @Override
    public void enforceUriPermission(Uri uri, int pid, int uid, int modeFlags, String message) {

    }

    @Override
    public void enforceCallingUriPermission(Uri uri, int modeFlags, String message) {

    }

    @Override
    public void enforceCallingOrSelfUriPermission(Uri uri, int modeFlags, String message) {

    }

    @Override
    public void enforceUriPermission(@Nullable Uri uri, @Nullable String readPermission, @Nullable String writePermission, int pid, int uid, int modeFlags, @Nullable String message) {

    }

    @Override
    public Context createPackageContext(String packageName, int flags) {
        return null;
    }

    @Override
    public Context createContextForSplit(String splitName) {
        return null;
    }

    @Override
    public Context createConfigurationContext(@NonNull Configuration overrideConfiguration) {
        return null;
    }

    @Override
    public Context createDisplayContext(@NonNull Display display) {
        return null;
    }

    @Override
    public Context createDeviceProtectedStorageContext() {
        return null;
    }

    @Override
    public boolean isDeviceProtectedStorage() {
        return false;
    }
}
