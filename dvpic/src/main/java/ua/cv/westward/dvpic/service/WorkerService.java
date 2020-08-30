package ua.cv.westward.dvpic.service;

import android.app.AlarmManager;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import androidx.preference.PreferenceManager;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.util.Locale;

import ua.cv.westward.dvpic.MyApplication;
import ua.cv.westward.dvpic.PrefKeys;
import ua.cv.westward.dvpic.db.DBAdapter;
import ua.cv.westward.dvpic.helper.BaseImageHelper;
import ua.cv.westward.dvpic.helper.ImagesCleaner;
import ua.cv.westward.dvpic.log.LogPreferences;
import ua.cv.westward.dvpic.log.LogRecord;
import ua.cv.westward.dvpic.log.LogRecord.Result;
import ua.cv.westward.dvpic.site.AppImage;
import ua.cv.westward.dvpic.site.Gallery;
import ua.cv.westward.dvpic.site.SiteParameters;
import ua.cv.westward.dvpic.utils.DialogUtils;
import ua.cv.westward.dvpic.utils.FileUtils;

/**
 * Сервис команд, которые выполняются продолжительное время.
 * L - load images, Загрузить изображения с указанных сайтов.
 * A - delete images, Удалить все изображения для указанных сайтов.
 * D - delete one image, Удалить одну картинку
 * C - copy one image, Скопировать файл в указанную папку.
 * F - favorite one image, Скопировать картинку в избранное.
 * G - delete one favorite image, Удалить картинку из избранного.
 *
 * Периодически вызывается через AlarmManager. Реализует функции удержания
 * процессора от перехода в спящий режим (WakeLock) на время своей работы.
 */
public class WorkerService extends WakeLockService {

    // service commands
    public static final char CMD_LOAD_IMAGES       = 'L';  // загрузить новые картинки
    public static final char CMD_DELETE_ALL_IMAGES = 'A';  // удалить все картинки
    public static final char CMD_DELETE_IMAGE      = 'D';  // удалить одну картинку
    public static final char CMD_COPY_IMAGE        = 'C';  // скопировать один файл
    public static final char CMD_FAVORITE_IMAGE    = 'F';  // скопировать картинку в избранное
    public static final char CMD_DELETE_FAVORITE   = 'G';  // удалить картинку из избранного

    private DBAdapter dbAdapter;

    /**
     * Конструктор сервиса
     * Имя, передаваемое родительскому классу, имеет значение только для отладки
     */
    public WorkerService() {
        super( "DVPicService" );
    }

    /**
     * Получить настройки, касающиеся работы сервиса
     */
    private void getPreferences() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences( getApplicationContext());
    }

    /**
     * Выполнение реальной работы сервиса (background thread)
     */
    @Override
    protected void execute( Intent intent ) {
        // startup: get preferences, database instance
        getPreferences();
        dbAdapter = DBAdapter.getInstance( getApplicationContext() );
        Log.v("DVPic", "!!!! ====== WORKER SERVICE START ======== !!!! ");


        try {
            // проверить доступность sd карты
            if(!FileUtils.isStorageMounted())
                return;
            // выполнить указанную команду
            char cmd = intent.getCharExtra( PrefKeys.INTENT_SERVICE_CMD, '?' );
            int count = 0;
            switch( cmd ) {
                case CMD_LOAD_IMAGES: {
                    count = downloadImages();
                    break;
                }
                case CMD_DELETE_ALL_IMAGES: {
                    String[] siteIDs = intent.getStringArrayExtra( PrefKeys.INTENT_GALLERY_ID );
                    deleteImages( siteIDs );
                    break;
                }
                case CMD_COPY_IMAGE: {
                    AppImage image = intent.getParcelableExtra( PrefKeys.INTENT_IMAGE );
                    assert image != null;
                    copyImage( image );
                    break;
                }
                case CMD_FAVORITE_IMAGE: {
                    AppImage image = intent.getParcelableExtra( PrefKeys.INTENT_IMAGE );
                    favoriteImage( image );
                    break;
                }
                case CMD_DELETE_FAVORITE: {
                    AppImage image = intent.getParcelableExtra( PrefKeys.INTENT_IMAGE );
                    assert image != null;
                    deleteFavoriteImage( image );
                    break;
                }
                case CMD_DELETE_IMAGE: {
                    AppImage image = intent.getParcelableExtra( PrefKeys.INTENT_IMAGE );
                    assert image != null;
                    deleteImage( image );
                    break;
                }
            }
            // выполнить финишный код  внутри блока try-catch
            afterExecute( cmd, count );
        }
        catch( Exception e ) {
            String msg = DialogUtils.shortErrorMessage( e );
            // write error log
            LogRecord r = new LogRecord( "Служба", Result.ERROR, msg );
            dbAdapter.insertLog( r );
            // set error flag in preferences
            LogPreferences.setErrorFlag( getApplicationContext(), true );

            showError( msg );
        }
    }

    /**
     * Выполнить код после выполнения основной команды, внутри блока try-catch.
     */
    private void afterExecute( char cmd, int count ) {
        // Broadcast сообщение о завершении работы сервиса
        Intent i = new Intent( PrefKeys.INTENT_SERVICE_UPD );
        i.putExtra( PrefKeys.INTENT_SERVICE_CMD, cmd );
        i.putExtra( PrefKeys.INTENT_COUNT, count );
        sendBroadcast( i );
    }

    /**
     * Команда CMD_UPDATE_IMAGES.
     * Загрузить изображения с указанных сайтов.
     */
    private int downloadImages() {

        ImagesCleaner cleaner = new ImagesCleaner( dbAdapter );

        // цикл обработки запросов к сайтам
        SiteParameters siteParams = null;
        int count = 0;
        String siteid = "DV";
            try {
                // получить параметры сайта
                siteParams = new SiteParameters( getApplicationContext(), siteid );
                // создать экземпляр класса загрузки изображений
                Class<?> cls = siteParams.getHelper();
                Constructor<?> constructor = cls.getConstructor( Context.class, SiteParameters.class );
                BaseImageHelper helper = (BaseImageHelper) constructor.newInstance( getApplicationContext(), siteParams );
                // выполнить загрузку изображений
                int c = helper.downloadImages(getApplicationContext());
                count = count + c;



                // cleanup: удаление старых изображений
//                showProgress( getString( R.string.msg_download_cleanup ), 1, 1 );
                cleaner.deleteImages( siteid, siteParams.getStorageSize() );

                // write success log
                if( c > 0 ) {
                    String msg = String.format( Locale.US, "Загружено %d изображений", c );
                    LogRecord r = new LogRecord( siteParams.getSiteID(), Result.OK, msg );
                    dbAdapter.insertLog( r );

                } else {
                    String msg = String.format( Locale.US, "Новых изображений нет", c );
                    LogRecord r = new LogRecord( siteParams.getSiteID(), Result.OK, msg );
                    dbAdapter.insertLog( r );
                }
            } catch( Exception e ) {
                String msg = DialogUtils.shortErrorMessage( e );
                // write error log
                assert siteParams != null;
                LogRecord r = new LogRecord( siteParams.getSiteID(), Result.ERROR, msg );
                dbAdapter.insertLog( r );
                // set error flag in preferences
                LogPreferences.setErrorFlag( getApplicationContext(), true );
            }



        // delete old log entries
        cleaner.deleteOldLogs();

        // получить общее количество новых картинок
        int total = dbAdapter.getNewCount();


        // обновить виджет
        DialogUtils.updateWidget( getApplicationContext(), total );
        return count;
    }

    /**
     * Команда CMD_DELETE_IMAGES.
     * Удалить все изображения для указанных сайтов.
     */
    private void deleteImages( String[] siteIDs ) throws IOException {
        if( siteIDs == null )
            return;
        // цикл удаления изображений из папок сайтов
        for( String siteid: siteIDs ) {
            SiteParameters siteParams = new SiteParameters( getApplicationContext(), siteid );
            FileUtils.deleteFiles( siteParams.getImagesFolder(getApplicationContext()) );
        }
        // удалить записи в базе данных
        dbAdapter.deleteImages( System.currentTimeMillis() );
        // получить общее количество новых картинок, обновить виджет
        int total = dbAdapter.getNewCount();
        DialogUtils.updateWidget( getApplicationContext(), total );
    }

    /**
     * Команда CMD_COPY_IMAGE.
     * Скопировать файл в папку download на sdcard.
     */
    private void copyImage( AppImage image ) throws IOException {
        // собрать имя результирующего файла
        String path = FileUtils.getPathSD( PrefKeys.SD_COPY_FOLDER, null, getApplicationContext() );

        String sb = path + File.separatorChar + FileUtils.splitFileName(image.getFilename());


        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {

            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, image.getFilename());
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);


            ContentResolver resolver = getApplicationContext().getContentResolver();
            Bitmap bitmap;
            Uri uri;
            try {
                // Requires permission WRITE_EXTERNAL_STORAGE
                bitmap = BitmapFactory.decodeFile( image.getFilename());
                uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
            } catch (Exception e) {
                Log.e("LOG_TAG", "Error inserting picture in MediaStore: " + e.getMessage());
                return;
            }
            assert uri != null;
            try (OutputStream stream = resolver.openOutputStream(uri)) {
                if (!bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)) {
                    throw new IOException("Error compressing the picture.");
                }
            } catch (Exception e) {
                resolver.delete(uri, null, null);
                Log.e("LOG_TAG", "Error adding picture to gallery: " + e.getMessage());
            }

        } else FileUtils.copyFile( image.getFilename(), sb);
    }

    /**
     * Команда CMD_FAVORITE_IMAGE
     * Скопировать запись базы данных, скопировать картинку в избранное.
     */
    private void favoriteImage( AppImage image ) throws IOException {
        // сохранить измененные метаданные в базу данных
        dbAdapter.updateImage( image );

        // получить параметры папки Избранное
        SiteParameters siteParams = new SiteParameters( getApplicationContext(), Gallery.FAV.name() );

        // собрать новое полное имя файла
        String src = image.getFilename();
        String dst = siteParams.getImagesFolder(getApplicationContext()) + File.separatorChar +
                FileUtils.splitFileName(src);

        // скопировать файл картинки в папку Избранное
        FileUtils.copyFile( src, dst );

        // сделать копию записи базы данных
        AppImage newImage = new AppImage( image, siteParams.getSiteID(), dst );
        dbAdapter.insertImage( newImage );

        // cleanup: удаление старых изображений
        ImagesCleaner cleaner = new ImagesCleaner( dbAdapter );
        cleaner.deleteImages( siteParams.getSiteID(), siteParams.getStorageSize() );
    }

    /**
     * Команда CMD_DELETE_IMAGE
     * Удалить одну картинку и соответствующую запись базы данных.
     */
    private void deleteImage( AppImage image ) {
        FileUtils.deleteFile( image.getFilename() );
        dbAdapter.deleteImage( image );
    }

    /**
     * Команда CMD_DEL_FAVORITE
     * Удалить картинку и запись метаданных из избранного.
     *
     * Удалить одну картинку и соответствующую запись базы данных из избранного.
     * Выполняется поиск в избранном по параметрам указанной картинки, удаляется
     * не текущая картинка, а соответствующая ей картинка и запись избранного.
     * @param image Объект метаданных картинки.
     */
    private void deleteFavoriteImage( AppImage image ) {
        // если команда запрошена из папки Избранное
        if( image.getSiteID().equals( Gallery.FAV.name() )) {
            // удалить файл и метаданные картинки
            FileUtils.deleteFile( image.getFilename() );
            dbAdapter.deleteImage( image );
            // найти исходную запись данных картинки, снять отметку "Избранное"
            AppImage galleryImage = dbAdapter.findImage( image.getImageID() );
            if( galleryImage != null ) {
                galleryImage.setOption( AppImage.FAVORITE, false );
                dbAdapter.updateImage( galleryImage );
            }
        }
        // если команда запрошена из папки какого-либо сайта
        else {
            // сохранить измененные метаданные в базу данных
            dbAdapter.updateImage( image );
            // найти картинку и метаданные в Избранном, удалить
            AppImage favImage = dbAdapter.findImage( Gallery.FAV.name(), image.getImageID() );
            if( favImage != null ) {
                FileUtils.deleteFile( favImage.getFilename() );
                dbAdapter.deleteImage( favImage );
            }
        }
    }

    /**
     * Show error message
     */
    private void showError( String msg ) {
        // если приложение активно, вывести диалог, а не нотификацию
        if( MyApplication.isForeground ) {
            // Broadcast сообщение об ошибке
            Intent i = new Intent( PrefKeys.INTENT_SERVICE_UPD );
            i.putExtra( PrefKeys.INTENT_ERROR_MSG, msg );
            sendBroadcast( i );
        }
    }


}
