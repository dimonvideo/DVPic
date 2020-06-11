package ua.cv.westward.dvpic;

import java.io.File;
import java.util.ArrayList;

import ua.cv.westward.dvpic.db.DBAdapter;
import ua.cv.westward.dvpic.db.ImageCursorWrapper;
import ua.cv.westward.dvpic.service.WakeLockService;
import ua.cv.westward.dvpic.service.WorkerService;
import ua.cv.westward.dvpic.site.AppImage;
import ua.cv.westward.dvpic.site.Gallery;
import ua.cv.westward.dvpic.site.SiteParameters;
import ua.cv.westward.dvpic.types.ImageToolbar;
import ua.cv.westward.dvpic.types.ViewerOptions;
import ua.cv.westward.dvpic.utils.ActivityUtils;
import ua.cv.westward.dvpic.utils.DialogUtils;
import ua.cv.westward.dvpic.utils.InternetUtils;
import ua.cv.westward.dvpic.utils.WarningDialog;
import ua.cv.westward.dvpic.viewer.BaseViewer;
import ua.cv.westward.dvpic.viewer.ImageViewer;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;

import android.text.ClipboardManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.fragment.app.DialogFragment;
import androidx.preference.PreferenceManager;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;


/**
 * Перелистывание набора ViewGroup по горизонтали с помощью touch жестов или
 * программных команд.
 *
 * @author Vladimir Kuts
 *
 * ViewPager, Inflater
 * http://stackoverflow.com/questions/6821320/custom-viewpager
 *
 * onTouch interference
 * http://stackoverflow.com/questions/6920137/android-viewpager-and-horizontalscrollview
 * http://stackoverflow.com/questions/3171452/scrollview-and-gallery-interfering
 */
@SuppressWarnings("deprecation")
public class FlipViewerActivity extends AppCompatActivity implements
        SharedPreferences.OnSharedPreferenceChangeListener {

    public static final int ID_OPEN_PAGE        = 0;
    public static final int ID_COPY_URL         = 1;
    public static final int ID_COPY_BBCODE_URL  = 2;
    public static final int ID_MARK_READ_ALL    = 3;
    public static final int ID_DELETE_ALL    = 4;

    private DBAdapter    dbAdapter;
    private ViewPager mViewPager;     // ViewPager: листание страниц
    private ViewAdapter  mAdapter;       // адаптер ViewPager
    private TextView     mEmptyView;
    private ImageToolbar mToolbar;

    private Gallery      mGallery;
    private String[]     mSiteIds;
    private boolean      mVolumeNavigation;
    private boolean      mOnEndExitApp;
    private boolean      mImagesStatusChanged;

    private ViewerOptions mOptions;
    private String mSiteTitle;

    /** STARTUP */

    @Override
    public void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );


        // get Intent's extraData: site IDs to view
        Intent callingIntent = getIntent();
        mGallery = Gallery.valueOf( callingIntent.getStringExtra( PrefKeys.INTENT_GALLERY_ID ));
        if( mGallery == Gallery.NEW ) {
            Preferences prefs = Preferences.getInstance( this );
            mSiteIds = new String[] {"DV"};
        } else {
            mSiteIds = new String[] { mGallery.name() };
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this );
        // получить настройки
        if( prefs.getBoolean( PrefKeys.VOLUME_BUTTONS, false )) {
            mVolumeNavigation = true;
        }
        if( prefs.getBoolean( PrefKeys.NEW_EXIT, false ) && mGallery == Gallery.NEW ) {
            mOnEndExitApp = true;
        }

        // set activity layout depending by preferences
        setupLayout( prefs );

        // setup database
        dbAdapter = DBAdapter.getInstance( this );

        setupViewPager();
    }

    @Override
    protected void onStart() {
        PreferenceManager.getDefaultSharedPreferences(this).
                registerOnSharedPreferenceChangeListener(this);
        super.onStart();
        registerReceiver( onServiceUpdate, new IntentFilter( PrefKeys.INTENT_SERVICE_UPD ));
    }

    @Override
    protected void onResume() {
        // Register OnSharedPreferenceChangeListener
        PreferenceManager.getDefaultSharedPreferences(this).
                registerOnSharedPreferenceChangeListener(this);
        super.onResume();
        // установить глобальный для приложения флаг "application in foreground"
        MyApplication.isForeground = true;

    }

    @Override
    protected void onPause() {
        PreferenceManager.getDefaultSharedPreferences(this).
                unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
        // снять глобальный для приложения флаг "application in foreground"
        MyApplication.isForeground = false;
    }

    @Override
    protected void onStop() {
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
        super.onStop();
        try {
            unregisterReceiver( onServiceUpdate );
        } catch( IllegalArgumentException e ) {
            // I can't or don't want to control number of times I call
            // unregisterReceiver on the same recevier.
        }
        // update widget
        if( mImagesStatusChanged ) {
            int count = dbAdapter.getNewCount();
            DialogUtils.updateWidget( this, count );

        }
    }

    @Override
    public void onBackPressed() {
        // изменить статус предварительно отмеченных картинок
        dbAdapter.updateImages();
        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).
                registerOnSharedPreferenceChangeListener(this);
        super.onBackPressed();
    }
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if( key.equals( PrefKeys.FULLSCREEN )) {
            restartActivity();
        }
    }
    public void restartActivity() {
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }
    /**
     * Стартовая настройка внешнего вида
     */
    private void setupLayout( SharedPreferences prefs ) {
        // установить ориентацию экрана
        ActivityUtils.setOrientation( this, prefs.getString( PrefKeys.ORIENTATION, "AUTO" ));
        // настройка "полноэкранный режим"
        ActivityUtils.setFullscreen( this, prefs.getBoolean( PrefKeys.FULLSCREEN, true ));
        // настройка "вид панели кнопок (toolbar)"
        setContentView( getLayout( prefs.getString( PrefKeys.IMAGE_TOOLBAR, "OVER" )));

        mToolbar = (ImageToolbar) findViewById( R.id.toolbar );
        if( mToolbar != null ) {
            mToolbar.setOnClickListener(v -> {
                int cmd = (Integer) v.getTag();
                switch( cmd ) {
                    case ImageToolbar.ID_BACK: {
                        showPrevious();
                        break;
                    }
                    case ImageToolbar.ID_FORWARD: {
                        showNext();
                        break;
                    }
                    case ImageToolbar.ID_ZOOM_IN: {
                        Log.d("DVPIC", " === не подписано ===");
                        BaseViewer viewer = mAdapter.getCurrentViewer();
                        if( viewer != null ) {
                            viewer.zoomIn();

                        }
                        break;
                    }
                    case ImageToolbar.ID_ZOOM_OUT: {
                        BaseViewer viewer = mAdapter.getCurrentViewer();
                        if( viewer != null ) {
                            viewer.zoomOut();
                        }
                        break;
                    }
                }
            });
        }

        // получить флаги внешнего вида ImageViewer'ов
        mOptions = new ViewerOptions( prefs );
    }


    /**
     * Стартовая настройка ViewPager
     */
    private void setupViewPager() {
        mViewPager = (ViewPager) findViewById( R.id.viewpager );
        mViewPager.setPageMargin( convertDip2Pixels( this, 10 ));

        boolean newImages = mGallery.onlyNewImages();

        ImageCursorWrapper c = dbAdapter.getImagesCursor( mSiteIds, newImages );
        startManagingCursor( c );

        mAdapter = new ViewAdapter( c );
        mViewPager.setAdapter( mAdapter );

        // настроить вывод сообщения для пустой галереи
        mEmptyView = (TextView) findViewById( R.id.empty_message );
        mEmptyView.setText( mGallery.getEmptyMessage() );

        // если нечего показывать, выключить панель кнопок и вывести сообщение
        // для пустой галереи
        updateView( mAdapter.getCount() > 0 );
    }

    /* СИСТЕМНОЕ МЕНЮ */

    /**
     * Создать и настроить меню приложения.
     */
    @SuppressLint("RestrictedApi")
    @Override
    public boolean onCreateOptionsMenu( Menu menu ) {
        super.onCreateOptionsMenu( menu );

        // convert the XML resource into a programmable object
        getMenuInflater().inflate( mGallery.getSystemMenu(), menu );
        if(menu instanceof MenuBuilder){
            MenuBuilder m = (MenuBuilder) menu;
            //noinspection RestrictedApi
            m.setOptionalIconsVisible(true);
        }
        return true;
    }

    /**
     * Динамическое изменение системного меню.
     */
    @Override
    public boolean onPrepareOptionsMenu( Menu menu ) {
        // Получить текущий объект метаданных приложения
        AppImage image = getCurrentImage();
        // запретить команды работы с текущей картинкой для пустой галереи
        boolean enabled = image != null ? true : false;
        menu.setGroupEnabled( R.id.menu_image_actions, enabled );
        menu.setGroupEnabled( R.id.menu_image_group, enabled );
        menu.findItem(R.id.menu_show_first).setIcon(R.drawable.ic_menu_back);
        return true;
    }

    /**
     * Выбор какого-либо пункта системного меню.
     */
    @Override
    public boolean onOptionsItemSelected( MenuItem item ) {
        super.onOptionsItemSelected( item );

        switch( item.getItemId() ) {
            case R.id.menu_share: {         // Отправить в...
                shareImage( getCurrentImage() );
                return true;
            }
            case R.id.menu_command: {       // Диалог команд для сайта DimonVideo
                AppImage image = getCurrentImage();
                DialogFragment commandDialog = CommandDialog.newInstance( image );
                commandDialog.show( getSupportFragmentManager(), "DV_COMMAND" );
                return true;
            }
            case R.id.menu_save: {          // Скопировать картинку на sd card
                saveImage( getCurrentImage() );
                return true;
            }
            case R.id.menu_reload: {        // Обновить галерею
                reloadImages();
                return true;
            }
            case R.id.menu_show_first: {    // Перейти в начало галереи
                showFirst();
                return true;
            }
            case R.id.menu_show_last: {     // Перейти в конец галаереи
                showLast();
                return true;
            }
            case R.id.menu_favorite: {
                BaseViewer viewer = mAdapter.getCurrentViewer();
                if( viewer.isFavorite() ) {
                    dropFavorite( viewer );
                } else {
                    setFavorite( viewer );
                }
                return true;
            }
            case R.id.menu_preferences: {
                startActivity( new Intent( this, SettingsActivity .class ));
                return true;
            }
            default:
                return false;
        }
    }

    /* НАЖАТИЕ КНОПОК */

    @Override
    public boolean dispatchKeyEvent( KeyEvent event ) {
        if( !mVolumeNavigation ) {
            return super.dispatchKeyEvent( event );
        }

        int action = event.getAction();
        switch( event.getKeyCode() ) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                if( action == KeyEvent.ACTION_DOWN ) {
                    showPrevious();
                }
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if( action == KeyEvent.ACTION_DOWN ) {
                    showNext();
                }
                return true;
            default:
                return super.dispatchKeyEvent( event );
        }
    }

    /* ВЫПОЛНЕНИЕ КОМАНД */

    /**
     * Показать предыдущую картинку или Toast сообщение, если больше нет картинок.
     */
    private void showPrevious() {
        int pos = mViewPager.getCurrentItem();
        if( pos > 0 ) {
            mViewPager.setCurrentItem( pos - 1, true );
        } else {
            DialogUtils.showShortToast( this, R.string.msg_image_limit );
            // if option 'NEW_EXIT' set, exit app
            if( mOnEndExitApp ) {
                exitApp();
            }
        }
    }

    /**
     * Показать следующую картинку или Toast сообщение, если больше нет картинок.
     */
    private void showNext() {
        int pos = mViewPager.getCurrentItem();
        if( pos < mAdapter.getCount() - 1 ) {
            mViewPager.setCurrentItem( pos + 1, true );
        } else {
            DialogUtils.showShortToast( this, R.string.msg_image_limit );
            // if option 'NEW_EXIT' set, exit app
            if( mOnEndExitApp ) {
                exitApp();
            }
        }
    }

    /**
     * Показать первую картинку в галерее.
     */
    private void showFirst() {
        mViewPager.setCurrentItem( 0, false );
    }

    /**
     * Показать последнюю картинку в галерее.
     */
    private void showLast() {
        mViewPager.setCurrentItem( mAdapter.getCount() - 1 , false );
    }

    /**
     * Обновить текущую галерею изображений.
     */
    private void reloadImages() {
        if( InternetUtils.isConnected( this )) {
            // вызвать сервис для загрузки новых картинок
            Intent intent = new Intent( this, WorkerService.class );
            intent.putExtra( PrefKeys.INTENT_SERVICE_CMD, WorkerService.CMD_LOAD_IMAGES );
            intent.putExtra( PrefKeys.INTENT_GALLERY_ID, mSiteIds );
            WakeLockService.start( this, intent );
            //
            DialogUtils.showToast( this, R.string.msg_download_start );
        } else {
            DialogUtils.showToast( this, R.string.msg_no_connection );
        }
    }

    /**
     * Отправить изображение в другое приложение ( gmail, twitter, ... )
     * @param image Объект метаданных изображения.
     */
    private void shareImage( AppImage image ) {
        File file = new File( image.getFilename() );
        Uri uri = Uri.fromFile( file );

        Intent i = new Intent( Intent.ACTION_SEND );
        i.setType( "image/*" );
        //Subject for the message or Email
        //i.putExtra(Intent.EXTRA_SUBJECT, "My Picture");
        i.putExtra( Intent.EXTRA_STREAM, uri );
        startActivity( Intent.createChooser( i, getString( R.string.dialog_share_title )));
    }

    /**
     * Выполнение команды для картинки
     * @param cmd
     * @param image
     */
    private void doImageCommand( int cmd, AppImage image ) {
        switch( cmd ) {
            case ID_OPEN_PAGE: {
                openWebPage( image );
                break;
            }
            case ID_COPY_URL: {
                copyImageLink( image, false );
                break;
            }
            case ID_COPY_BBCODE_URL: {
                copyImageLink( image, true );
                break;
            }
            case ID_MARK_READ_ALL: {
                setAllImagesStatus( false );
                break;
            }
            case ID_DELETE_ALL: {
                deleteAllImages();
                break;
            }
        }
    }
    private void deleteAllImages() {
        Preferences prefs = Preferences.getInstance( this );
        //
        Intent intent = new Intent( this, WorkerService.class );
        intent.putExtra( PrefKeys.INTENT_SERVICE_CMD, WorkerService.CMD_DELETE_ALL_IMAGES );
        intent.putExtra( PrefKeys.INTENT_GALLERY_ID, prefs.getSiteIds() );
        WakeLockService.start( this, intent );
        DialogUtils.showToast( this, R.string.msg_delete_start );
    }
    /**
     * Открыть веб-страницу изображения в браузере.
     * @param image Объект метаданных изображения.
     */
    private void openWebPage( AppImage image ) {
        try {
            // получить параметры сайта
            SiteParameters siteParams = new SiteParameters( this, "DV" );
            String url = siteParams.getWebPageURL( image.getImageID() );
            if( url != null ) {
                Intent i = new Intent( "android.intent.action.VIEW", Uri.parse( url ));
                startActivity( i );
            } else {
                DialogUtils.showToast( this, R.string.msg_no_page_url );
            }
        } catch( Exception e ) {
            // перестраховка, если по какой-то причине в конструктор будет передан
            // несуществующий siteid (enum)
            DialogUtils.showToast( this, String.format( getString( R.string.msg_siteid_error ), "DV" ));
        }
    }

    /**
     * Скопировать в clipboard ссылку на картинку.
     * @param image Объект метаданных изображения.
     * @param bbcode Добавить теги [img][/img] к ссылке.
     */
    private void copyImageLink( AppImage image, boolean bbcode ) {
        StringBuilder sb = new StringBuilder();
        if( bbcode ) {
            sb.append( "[img]" );
        }
        sb.append( image.getLink() );
        if( bbcode ) {
            sb.append( "[/img]" );
        }
        ClipboardManager clipboard = (ClipboardManager) getSystemService( Context.CLIPBOARD_SERVICE );
        clipboard.setText( sb.toString() );

    }

    /**
     * Сохранить изображение в папку Downloads на sd card.
     * @param image Объект метаданных изображения.
     */
    private void saveImage( AppImage image ) {
        // вызвать сервис для копирования картинки
        startService( WorkerService.CMD_COPY_IMAGE, image );
        DialogUtils.showToast( this, R.string.msg_file_copy_start );
    }

    /**
     * Скопировать картинку текущего вьювера в Избранное.
     * @param viewer
     */
    private void setFavorite( BaseViewer viewer ) {
        viewer.setFavorite( true );
        // вызвать сервис для копирования картинки в избранное
        startService( WorkerService.CMD_FAVORITE_IMAGE, viewer.getImage() );
    }

    /**
     * Удалить картинку текущего вьювера из Избранного
     * @param viewer
     */
    private void dropFavorite( BaseViewer viewer ) {
        viewer.setFavorite( false );
        // вызвать сервис для удаления картинки из избранного
        startService( WorkerService.CMD_DELETE_FAVORITE, viewer.getImage() );
    }

    /**
     * Изменить статус всех картинок в галерее
     * @param status true: "новая картинка", false: "просмотренная"
     */
    private void setAllImagesStatus( boolean status ) {
        dbAdapter.updateImages( mSiteIds, status );
        refresh();
    }

    /* SERVICE METHODS */

    private void exitApp() {
        // изменить статус предварительно отмеченных картинок
        dbAdapter.updateImages();
        // завершить работу приложения
        Intent i = new Intent();
        i.setAction( Intent.ACTION_MAIN );
        i.addCategory( Intent.CATEGORY_HOME );
        startActivity( i );
        finish();
    }

    public static int convertDip2Pixels( Context context, int dip ) {
        return (int)TypedValue.applyDimension( TypedValue.COMPLEX_UNIT_DIP,
                dip, context.getResources().getDisplayMetrics() );
    }

    /**
     * Получить текущий объект изображения или null, если изображение отсутствует
     * @return
     */
    public AppImage getCurrentImage() {
        //
        BaseViewer currentViewer = mAdapter.getCurrentViewer();
        if( currentViewer != null ) {
            return currentViewer.getImage();
        } else {
            return null;
        }
    }

    /**
     * Изменить внешний вид для пустой галереи
     * @param notEmpty true: Галерея не пустая
     */
    private void updateView( boolean notEmpty ) {
        if( notEmpty ) {
            mEmptyView.setVisibility( View.GONE );
            mViewPager.setVisibility( View.VISIBLE );
        } else {
            mEmptyView.setVisibility( View.VISIBLE );
            mViewPager.setVisibility( View.GONE );
        }
        showToolbar( notEmpty );
    }

    /**
     * Переключить видимость панели инструментов.
     * @param value true: включить тулбар
     */
    private void showToolbar( boolean value ) {
       if( mToolbar != null ) {
           int v = value ? View.VISIBLE : View.GONE;
           mToolbar.setVisibility( v );
       }
    }

    /**
     * Запустить сервис для выполнения указанной команды
     * @param command
     * @param image
     */
    private void startService( char command, AppImage image ) {
        Intent intent = new Intent( this, WorkerService.class );
        intent.putExtra( PrefKeys.INTENT_SERVICE_CMD, command );
        intent.putExtra( PrefKeys.INTENT_IMAGE, image );
        WakeLockService.start( this, intent );
    }

    /**
     * Адаптер вызывает этот метод для обновления одной записи базы данных.
     * @param image Объект метаданных изображения.
     */
    private void updateImage( AppImage image ) {
        dbAdapter.updateImage( image );
        // перезапросить данные, чтобы адаптер при перелистывании следующих
        // страниц использовал актуальные данные
        mAdapter.getCursor().requery();
        mAdapter.notifyDataSetChanged();
    }

    /**
     * Обновить данные, адаптер
     */
    private void refresh() {
        mAdapter.getCursor().requery();
        mAdapter.notifyDataSetChanged();

        // если нечего показывать, выключить панель кнопок и вывести сообщение
        // для пустой галереи
        updateView( mAdapter.getCount() > 0 );
    }

    /*************************** Broadcast Receiver ***************************/

    /**
     * Сервис завершил выполнение команды.
     */
    BroadcastReceiver onServiceUpdate = new BroadcastReceiver() {

        @Override
        public void onReceive( Context context, Intent intent ) {
            // если при выполнении команды произошла ошибка
            if( intent.hasExtra( PrefKeys.INTENT_ERROR_MSG )) {
                String msg = intent.getStringExtra( PrefKeys.INTENT_ERROR_MSG );
                DialogFragment errorDialog = WarningDialog.newInstance( R.string.dialog_error_title, msg );
                errorDialog.show( getSupportFragmentManager(), "dialog" );
            }

            // команда выполнена
            else if( intent.hasExtra( PrefKeys.INTENT_SERVICE_CMD )) {
                char cmd = intent.getCharExtra( PrefKeys.INTENT_SERVICE_CMD, '?' );
                switch( cmd ) {
                    case WorkerService.CMD_LOAD_IMAGES: {
                        onLoadImages( intent.getIntExtra( PrefKeys.INTENT_COUNT, 0 ));
                        break;
                    }
                    case WorkerService.CMD_DELETE_FAVORITE:
                    case WorkerService.CMD_FAVORITE_IMAGE: {
                        refresh();
                        break;
                    }
                }
            }
        }

        // обработать сообщение о загрузке картинок
        private void onLoadImages( int count ) {
            if( count > 0 ) {
                refresh();
                // вывести toast сообщение с количеством новых картинок
                String s = String.format( getString( R.string.msg_download_count), count );
                DialogUtils.showToast( FlipViewerActivity.this, s );
            } else {
                DialogUtils.showToast( FlipViewerActivity.this, R.string.msg_download_zero );
            }
        }
    };

    /**************************** ViewAdapter *********************************/

    private class ViewAdapter extends PagerAdapter {

        private ImageCursorWrapper cursor = null;
        private BaseViewer currentViewer = null;

        public ViewAdapter( ImageCursorWrapper c ) {
            this.cursor = c;
        }

        /**
         * Вернуть курсор, который использует адаптер.
         * @return
         */
        public Cursor getCursor() {
            return cursor;
        }

        /**
         * Return the number of views available.
         */
        @Override
        public int getCount() {
            return cursor.getCount();
        }

        /**
         * Вернуть текущий ImageViewer или null, если по какой-либо причине
         * объекты ImageViewer не созданы.
         */
        public BaseViewer getCurrentViewer() {
            return currentViewer;
        }

        /**
         * Create the page for the given position.  The adapter is responsible
         * for adding the view to the container given here, although it only
         * must ensure this is done by the time it returns from

         * @param position The page position to be instantiated.
         * @return Returns an Object representing the new page.  This does not
         * need to be a View, but can be some other container of the page.
         */
        @Override
        public Object instantiateItem( ViewGroup collection, int position ) {
            BaseViewer iv;

            if( cursor.moveToPosition( position ) ) {
                AppImage image = new AppImage( cursor );
                iv = new ImageViewer( FlipViewerActivity.this, mOptions );

                iv.setData( image, position, getCount() );
            } else {
                iv = new ImageViewer( FlipViewerActivity.this, mOptions );
            }

            collection.addView( iv, 0 );
            return iv;
        }

        /**
         * Remove a page for the given position.  The adapter is responsible
         * for removing the view from its container, although it only must ensure

         * {@link #instantiateItem(View, int)}.
         */
        @Override
        public void destroyItem( ViewGroup collection, int position, Object view ) {
            BaseViewer iv = (BaseViewer) view;
            collection.removeView( iv );
            // recycle bitmap memory
            iv.recycle();
        }

        /**
         * ViewPager вызывает этот метод, когда новый View становится текущим.
         * @param container The containing View from which the page will be removed.
         * @param position The page position that is now the primary.
         * @param object The same object that was returned by instantiateItem(View, int)
         */
        @Override
        public void setPrimaryItem( ViewGroup container, int position, Object object ) {
            BaseViewer newViewer = (BaseViewer) object;
            if( currentViewer != newViewer ) {
                if( newViewer != null ) {
                    // получить метаданные новой картинки
                    AppImage newImgData = newViewer.getImage();
                    if( newImgData.getStatus() == AppImage.STATUS_NEW ) {
                        // записать в DB статус "просмотрена, отмечена как не-новая"
                        newImgData.setViewStatus( AppImage.STATUS_MARK );
                        updateImage( newImgData );
                        // установить флаг "статус картинок изменился"
                        mImagesStatusChanged = true;

                    }
                    // уведомить viewer о том, что он становится активным
                    newViewer.notifySetActive( position, getCount() );
                }
                // ViewPager кеширует два соседних View (слева и справа от текущего).
                // Также, в ViewPager нет обработчика события "View перестал
                // быть текущим". Данный обработчик в настоящий момент является
                // наиболее удобной точкой для уведомления View о том, что
                // он становится неактивным.
                // нужно сбросить статус для изображения, которое было текущим.
                if( currentViewer != null ) {
                    currentViewer.notifySetInactive();
                }
                // сохранить reference на новый ImageViewer
                currentViewer = newViewer;
            }
        }

        /**
         * Called when the host view is attempting to determine if an item's position
         * has changed. Returns {@link #POSITION_UNCHANGED} if the position of the given
         * item has not changed or {@link #POSITION_NONE} if the item is no longer present
         * in the adapter.
         *
         * @param object Object representing an item, previously returned by a call to
         *               {@link #instantiateItem(View, int)}.
         * @return object's new position index from [0, {@link #getCount()}),
         *         {@link #POSITION_UNCHANGED} if the object's position has not changed,
         *         or {@link #POSITION_NONE} if the item is no longer present.
         */
        @Override
        public int getItemPosition( Object object ) {
            int count = cursor.getCount();
            if( count == 0 )
                return POSITION_NONE;

            // Оптимизация работы: если позиция ImageViewer == -1, это считается
            // признаком объекта, который удален. Соответственно, нет смысла искать
            // его позицию в курсоре.
            BaseViewer viewer = (BaseViewer) object;
            int viewerPos = viewer.getPosition();
            if( viewerPos == -1 ) {
                return POSITION_NONE;
            }

            // Установить стартовую позицию курсора. Вначале попытаться установить
            // ранее известную позицию, если это не получается, установить курсор
            // в начало. Установка курсора в начало для позиции 1 вьювера -
            // оптимизация для удаления самой первой картинки, чтобы избежать
            // просмотра всего курсора.
            int pos = cursor.getPosition();
            if( pos == -1 || pos >= count ) {
                if( viewerPos == 1 || !cursor.moveToPosition(viewerPos))
                    cursor.moveToFirst();
            }
            // получить ID объекта (ImageViewer), позицию которого нам надо узнать
            long objectID = viewer.getImage().getID();
            // поиск позиции в курсоре, id которой соответствует id объекта
            while( count > 0 ) {
                if( cursor.getLong( cursor.id ) == objectID ) {
                    // найдена позиция объекта, обновить данные и внешний вид
                    // вьювера (сейчас только статус "новая картинка")
                    viewer.setStatus( cursor.getInt( cursor.viewStatus ));
                    return cursor.getPosition();
                }
                if(!cursor.moveToNext()) {
                    cursor.moveToFirst();
                }
                count -= 1;
            }
            return POSITION_NONE;
        }

        @Override
        public boolean isViewFromObject( View view, Object object ) {
            return view == object;
        }


        @Override
        public void restoreState(Parcelable arg0, ClassLoader arg1) {}

        @Override
        public Parcelable saveState() {
            return null;
        }

    }

    /********************** Site/Gallery Commands Dialog **********************/

    public static class CommandDialog extends DialogFragment {

        private AppImage        mImage;
        private CommandAdapter  mAdapter;

        public static CommandDialog newInstance( AppImage image ) {
            CommandDialog dlg = new CommandDialog();
            Bundle args = new Bundle();
            args.putParcelable( "image", image );
            dlg.setArguments( args );
            return dlg;
        }

        @Override
        public Dialog onCreateDialog( Bundle savedInstanceState ) {
            mImage = getArguments().getParcelable( "image" );
            mAdapter = new CommandAdapter( getActivity(), mImage );

            return new AlertDialog.Builder( getActivity() )
                .setTitle( R.string.dialog_command_title )
                .setAdapter( mAdapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick( DialogInterface dialog, int which ) {
                        int cmd = (int) mAdapter.getItemId(which);
                        ((FlipViewerActivity) getActivity()).doImageCommand( cmd, mImage );
                        dialog.cancel();
                    }
                })
                .create();
        }

        /**
         * Простой класс-контейнер параметров команды картинки.
         */
        private static class ImageCommand {
            private final int mID;        // id команды
            private final int mTitleID;   // resource id названия команды

            public ImageCommand( int id, int title ) {
                this.mID = id;
                this.mTitleID = title;
            }
        }

        /**
         * ListAdapter списка команд картинки.
         */
        private class CommandAdapter extends BaseAdapter {

            private final Context mCtx;
            private final int mLayoutID;
            private final LayoutInflater mInflater;
            private final ArrayList<ImageCommand> mList;

            public CommandAdapter( Context ctx, AppImage image ) {
                mCtx = ctx;
                mLayoutID = android.R.layout.select_dialog_item;
                // кеширование inflater'а, чтобы не получать этот объект при каждом выводе
                mInflater = LayoutInflater.from( ctx );

                mList = new ArrayList<ImageCommand>();

                if( image.getOption( AppImage.WEB_PAGE )) {
                    mList.add( new ImageCommand( ID_OPEN_PAGE, R.string.command_open_page ));
                }
                mList.add( new ImageCommand( ID_COPY_URL, R.string.command_copy_url ));
                mList.add( new ImageCommand( ID_COPY_BBCODE_URL, R.string.command_copy_bb_url ));
                mList.add( new ImageCommand( ID_MARK_READ_ALL, R.string.command_mark_read_all ));
                mList.add( new ImageCommand( ID_DELETE_ALL, R.string.pref_service_delete_all ));
            }

            @Override
            public int getCount() {
                return mList.size();
            }

            @Override
            public Object getItem( int position ) {
                return mList.get( position );
            }

            @Override
            public long getItemId( int position ) {
                return mList.get( position ).mID;
            }

            @Override
            public View getView( int position, View convertView, ViewGroup parent ) {
                TextView text1;
                if( convertView == null ) {
                    convertView = mInflater.inflate( mLayoutID, null );
                    // кешировать ссылку на UI элемент
                    text1 = (TextView) convertView.findViewById( android.R.id.text1 );
                    convertView.setTag( text1 );
                } else {
                    text1 = (TextView) convertView.getTag();
                }
                // bind data
                String s = mCtx.getString( mList.get(position).mTitleID );
                text1.setText( s );

                return convertView;
            }
        }
    }

    /*************************** LayoutOptions enum ***************************/

    // варианты внешнего вида activity

    /**
     * Get layout for this activity
     * @param value Настройка внешнего вида activity
     * @return Layout resource id
     */
    private static int getLayout( String value ) {
        try {
            LayoutOptions opt = LayoutOptions.valueOf( value );
            return opt.layoutID;
        } catch( Exception e ) {
            return R.layout.viewpager_relative;
        }
    }

    private static enum LayoutOptions {
        OFF( R.layout.viewpager_flat ),
        BOTTOM( R.layout.viewpager_linear ),
        OVER( R.layout.viewpager_relative );

        private final int layoutID;

        private LayoutOptions( int resID ) {
            this.layoutID = resID;
        }
    }

}