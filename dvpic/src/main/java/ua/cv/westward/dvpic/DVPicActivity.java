package ua.cv.westward.dvpic;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.security.ProviderInstaller;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;

import javax.net.ssl.SSLContext;

import ua.cv.westward.dvpic.db.DBAdapter;
import ua.cv.westward.dvpic.log.LogViewerActivity;
import ua.cv.westward.dvpic.service.WakeLockService;
import ua.cv.westward.dvpic.service.WorkerService;
import ua.cv.westward.dvpic.site.Gallery;
import ua.cv.westward.dvpic.site.SiteInfo;
import ua.cv.westward.dvpic.site.dialog.SiteSelectDialog;
import ua.cv.westward.dvpic.types.InfoButton;
import ua.cv.westward.dvpic.utils.ActivityUtils;
import ua.cv.westward.dvpic.utils.DialogUtils;
import ua.cv.westward.dvpic.utils.FileUtils;
import ua.cv.westward.dvpic.utils.InternetUtils;
import ua.cv.westward.dvpic.utils.WarningDialog;

public class DVPicActivity extends AppCompatActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener, SiteSelectDialog.OnSiteSelectCallback {

    private static final String URL_LOGO = "https://dimonvideo.ru/android/368838/";
    private static final int REQUEST_WRITE_EXTERNAL_STORAGE = 10001;

    // объявляем разрешение, которое нам нужно получить
    private static final String WRITE_EXTERNAL_STORAGE_PERMISSION = Manifest.permission.WRITE_EXTERNAL_STORAGE;

    private SharedPreferences mPreferences;
    private DBAdapter dbAdapter;

    private InfoButton mNewButton;
    private InfoButton mFavButton;
    private ProgressBar progressBar;
    SharedPreferences sharedPrefs;
    /* STARTUP */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Intent serviceIntent = new Intent(getApplicationContext(), WorkerService.class);

        serviceIntent.setAction(String.valueOf(WorkerService.CMD_LOAD_IMAGES));

        setContentView(R.layout.main);
        setupPreferences();
        setupButtons();
        setupMisc();
        // setup database
        dbAdapter = DBAdapter.getInstance( getApplicationContext() );
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        try {
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setApplicationId("1:100440464526:android:08a8e654a322dd7e703ddf") // Required for Analytics.
                    .setProjectId("dvpic-16c7c") // Required for Firebase Installations.
                    .build();
            FirebaseApp.initializeApp(this, options, "DVPic");
            FirebaseMessaging.getInstance().subscribeToTopic("all")
                .addOnCompleteListener(task -> {
                    Log.d("DVPIC", " === подписано ===");
                });
        } catch (Exception ignored) {
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {

            ShortcutManager shortcutManager = getSystemService(ShortcutManager.class);

            ShortcutInfo favShortcut = new ShortcutInfo.Builder(this, "shortcut_fav")
                    .setShortLabel(getString(R.string.btn_favorites_title))
                    .setIcon(Icon.createWithResource(this, R.drawable.icon))
                    .setIntents(
                            new Intent[]{
                                    new Intent(Intent.ACTION_MAIN, Uri.EMPTY, DVPicActivity.this, FlipViewerActivity.class).putExtra( PrefKeys.INTENT_GALLERY_ID, "FAV" ).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK),
                            })
                    .build();

            ShortcutInfo newShortcut = new ShortcutInfo.Builder(this, "shortcut_new")
                    .setShortLabel(getString(R.string.btn_new_images_title))
                    .setIcon(Icon.createWithResource(this, R.drawable.icon))
                    .setIntents(
                            new Intent[]{
                                    new Intent(Intent.ACTION_MAIN, Uri.EMPTY, DVPicActivity.this, FlipViewerActivity.class).putExtra( PrefKeys.INTENT_GALLERY_ID, "DV" ).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK),
                            })
                    .build();

            assert shortcutManager != null;
            new Thread(() -> shortcutManager.setDynamicShortcuts(Arrays.asList(favShortcut, newShortcut))).start();

        }

        progressBar = DVPicActivity.this.findViewById(R.id.progressBar);
        progressBar.setVisibility(ProgressBar.INVISIBLE);
        // проверяем разрешения: если они уже есть,
        // то приложение продолжает работу в нормальном режиме
        if (isPermissionGranted()) {

            appUpdateDialog();

            try {
                ProviderInstaller.installIfNeeded(getApplicationContext());
                SSLContext sslContext;
                sslContext = SSLContext.getInstance("TLSv1.2");
                sslContext.init(null, null, null);
                sslContext.createSSLEngine();
            } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException
                    | NoSuchAlgorithmException | KeyManagementException e) {
                e.printStackTrace();
            }

            try {

                FileUtils.checkBaseAppPath(getApplicationContext());

                if (InternetUtils.isWifiConnected(getApplicationContext())) reloadImages();

            } catch (Exception e) {
                String sb = getString(R.string.msg_sdcard_access_error) + "\n\n" +
                        DialogUtils.shortErrorMessage(e);
                DialogFragment errorDialog = WarningDialog.newInstance(R.string.dialog_error_title, sb);
                errorDialog.show(getSupportFragmentManager(), "ERROR");
            }
        } else {
            // иначе запрашиваем разрешение у пользователя
            requestPermission();
        }

        SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            reloadImages();
            swipeRefreshLayout.setRefreshing(false);

        });

        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(mLocalBroadcast ,
                new IntentFilter("myBroadcast"));

    }

    private void appUpdateDialog() {
        try {

            if (appWasUpdated(this)) {
                AlertDialog.Builder alert = new AlertDialog.Builder(DVPicActivity.this);
                alert.setTitle(getString(R.string.welcome));

                WebView wv = new WebView(DVPicActivity.this);

                wv.loadUrl("file:///android_asset/" + getString(R.string.asset_welcome));

                wv.setWebViewClient(new WebViewClient()
                {
                    public boolean shouldOverrideUrlLoading(WebView view, String url)
                    {
                        view.loadUrl(url);

                        return true;
                    }
                });

                alert.setView(wv);

                alert.setNegativeButton(android.R.string.ok,
                        (dialog, which) -> dialog.dismiss());
                alert.show();

            } else {
                View parentLayout = findViewById(android.R.id.content);
                Snackbar snackbar = Snackbar.make(parentLayout, getString(R.string.new_version), Snackbar.LENGTH_LONG);
                snackbar.setAction(getString(R.string.download), view -> {

                    String link = "https://dimonvideo.ru/android/1";
                    if (BuildConfig.GOOGLE) {
                        link = "https://play.google.com/store/apps/details?id=ru.dimonvideo.videos";
                    }

                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
                    startActivity(browserIntent);

                });

                snackbar.setActionTextColor(Color.YELLOW);
                snackbar.show();
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    private final BroadcastReceiver mLocalBroadcast = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // take values from intent which contains in intent if you putted their
            // here update the progress bar and testier
            int progress = Integer.parseInt(Objects.requireNonNull(intent.getStringExtra("progress")));
            int progressmax = Integer.parseInt(Objects.requireNonNull(intent.getStringExtra("progressmax")));
            progressBar = DVPicActivity.this.findViewById(R.id.progressBar);
            progressBar.setMax(progressmax);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                progressBar.setProgressTintList(ColorStateList.valueOf(Color.BLACK));
            }
            runOnUiThread(() -> progressBar.setProgress(progress));
            if (progress == progressmax) progressBar.setVisibility(ProgressBar.INVISIBLE);

        }
    };
    private boolean isPermissionGranted() {
        // проверяем разрешение - есть ли оно у нашего приложения
        int permissionCheck = ActivityCompat.checkSelfPermission(getApplicationContext(), DVPicActivity.WRITE_EXTERNAL_STORAGE_PERMISSION);
        return permissionCheck == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_WRITE_EXTERNAL_STORAGE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                try {
                    FileUtils.checkBaseAppPath(getApplicationContext());
                    if (InternetUtils.isWifiConnected(getApplicationContext())) reloadImages();
                    appUpdateDialog();

                } catch (Exception e) {
                    String sb = getString(R.string.msg_sdcard_access_error) + "\n--\n" +
                            DialogUtils.shortErrorMessage(e);
                    DialogFragment errorDialog = WarningDialog.newInstance(R.string.dialog_error_title, sb);
                    errorDialog.show(getSupportFragmentManager(), "ERROR");
                }
            } else {
                Toast.makeText(DVPicActivity.this, "Разрешения не получены", Toast.LENGTH_LONG).show();
                View parentLayout = findViewById(android.R.id.content);
                Snackbar snackbar = Snackbar.make(parentLayout, getString(R.string.new_version), Snackbar.LENGTH_LONG);
                snackbar.setAction(getString(R.string.download), view -> {

                    String link = "https://dimonvideo.ru/android/1";
                    if (BuildConfig.GOOGLE) {
                        link = "https://play.google.com/store/apps/details?id=ru.dimonvideo.videos";
                    }

                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
                    startActivity(browserIntent);

                });

                snackbar.setActionTextColor(Color.YELLOW);
                snackbar.show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void requestPermission() {
        // запрашиваем разрешение
        ActivityCompat.requestPermissions(DVPicActivity.this, new String[]{DVPicActivity.WRITE_EXTERNAL_STORAGE_PERMISSION}, DVPicActivity.REQUEST_WRITE_EXTERNAL_STORAGE);
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {

        public SettingsFragment() {
        }

        @Override
        public void onCreatePreferences(Bundle bundle, String rootKey) {
            setPreferencesFromResource(R.xml.preferences, rootKey);
        }
    }
    @Override
    protected void onDestroy() {
        // убрать callback на изменение настроек
        mPreferences.unregisterOnSharedPreferenceChangeListener( this );
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(mLocalBroadcast );
        super.onDestroy();
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    protected void onStart() {
        super.onStart();

        registerReceiver( onServiceUpdate, new IntentFilter( PrefKeys.INTENT_SERVICE_UPD ));

        // выполнение команды, если activity вызвано с extra параметрами
        Intent callingIntent = getIntent();
        if( callingIntent.hasExtra( PrefKeys.INTENT_APP_CMD )) {
            // получить команду и сразу же убрать extra parameter из интента,
            // чтобы при перезапуске activity снова не начала выполнять эту же команду
            char cmd = callingIntent.getCharExtra( PrefKeys.INTENT_APP_CMD, '?' );
            callingIntent.removeExtra( PrefKeys.INTENT_APP_CMD );
            // команда NEW: показать новые картинки
            if (cmd == PrefKeys.CMD_SHOW_NEW) {
                showImages(Gallery.NEW.name());
            }
        }
        // extra параметров нет
        else {
            // установить новое значение счетчиков на кнопках
            setButtonCounters();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // установить глобальный для приложения флаг "application in foreground"
        MyApplication.isForeground = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        // снять глобальный для приложения флаг "application in foreground"
        MyApplication.isForeground = false;
    }

    @Override
    protected void onStop() {
        super.onStop();

        try {
            unregisterReceiver( onServiceUpdate );
        } catch( IllegalArgumentException e ) {
            // I can't or don't want to control number of times I call
            // unregisterReceiver on the same receiver.
        }
    }


    /** SETUP */

    private void setupPreferences() {

       mPreferences = PreferenceManager.getDefaultSharedPreferences( this);
        // зарегистрировать callback на изменение настроек
        mPreferences.registerOnSharedPreferenceChangeListener( this );
        // установить ориентацию экрана
        ActivityUtils.setOrientation( this, mPreferences.getString( PrefKeys.ORIENTATION, "AUTO" ));
    }

    /**
     * Стартовая настройка кнопок выбора галереи
     */
    private void setupButtons() {
//        // create Map for quick access to InfoButton references
//        mButtons = new HashMap<String,InfoButton>();

        // new images button
        mNewButton = findViewById( R.id.new_btn );
        if( mNewButton != null ) {
            mNewButton.setOnClickListener(v -> showImages( "NEW" ));
        }
        // favorites images button
        mFavButton = findViewById( R.id.favorites_btn );
        if( mFavButton != null ) {
            mFavButton.setOnClickListener(v -> showImages( "FAV" ));
        }

        // sites button
        InfoButton ib = findViewById( R.id.sites_btn );
        if( ib != null ) {
            ib.setOnClickListener(v -> showImages( "DV" ));
            ib.setCounter( 0, 0 );
        }
    }

    /**
     * Стартовая настройка прочих UI элементов
     */
    private void setupMisc() {
        // назначить обработчик onClick для logo
        View logo = findViewById( R.id.logo );
        if( logo != null ) {
            logo.setOnClickListener(v -> {
                Intent i = new Intent( "android.intent.action.VIEW", Uri.parse( URL_LOGO ));
                startActivity( i );
            });
        }
    }
    /** SHARED PREFERENCES */

    @Override
    public void onSharedPreferenceChanged( SharedPreferences prefs, String key ) {
   if( key.equals( PrefKeys.ORIENTATION )) {
            ActivityUtils.setOrientation( this, prefs.getString( key, "AUTO" ));
        }
    }

    /**
     * Создать и настроить меню приложения.
     */
    @SuppressLint("RestrictedApi")
    @Override
    public boolean onCreateOptionsMenu( Menu menu ) {
        super.onCreateOptionsMenu( menu );
        // convert the XML resource into a programmable object
        getMenuInflater().inflate( R.menu.main_menu, menu );
        if(menu instanceof MenuBuilder){
            MenuBuilder m = (MenuBuilder) menu;
            //noinspection RestrictedApi
            m.setOptionalIconsVisible(true);
        }
        return true;
    }

    /**
     * Выбор какого-либо пункта системного меню.
     */
    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item ) {
        super.onOptionsItemSelected( item );

        switch( item.getItemId() ) {
            case R.id.menu_reload_all: {
                reloadImages();
                return true;
            }
            case R.id.menu_log: {
                startActivity( new Intent( DVPicActivity.this, LogViewerActivity.class ));
                return true;
            }
            case R.id.menu_preferences: {
                startActivity( new Intent( DVPicActivity.this, SettingsActivity .class ));
                return true;
            }
            case R.id.menu_about: {
                startActivity( new Intent( DVPicActivity.this, WelcomeActivity.class ));
                return true;
            }
            case R.id.menu_about2: {

                String link = "https://dimonvideo.ru/android/14/DimonVideo/0/0";
                if (BuildConfig.GOOGLE) {
                    link = "https://play.google.com/store/apps/dev?id=6091758746633814135";
                }

                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
                startActivity(browserIntent);
                return true;
            }
            default:
                return false;
        }
    }


    /*************************** Broadcast Receiver ***************************/

    BroadcastReceiver onServiceUpdate = new BroadcastReceiver() {

        @RequiresApi(api = Build.VERSION_CODES.S)
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
                if( cmd == WorkerService.CMD_LOAD_IMAGES ) {
                    // установить счетчики картинок для кнопок сайтов
                    setButtonCounters();
                    // получить количество загруженных картинок
                    int count = intent.getIntExtra( PrefKeys.INTENT_COUNT, 0 );
                    if( count > 0 ) {
                        // вывести toast сообщение с количеством новых картинок
                        String s = String.format( getString( R.string.msg_download_count), count );
                        DialogUtils.showToast( DVPicActivity.this, s );
                        progressBar = DVPicActivity.this.findViewById(R.id.progressBar);
                        progressBar.setVisibility(ProgressBar.INVISIBLE);
                    } else {
                        DialogUtils.showToast( DVPicActivity.this, R.string.msg_download_zero );
                        progressBar = DVPicActivity.this.findViewById(R.id.progressBar);
                        progressBar.setVisibility(ProgressBar.INVISIBLE);

                    }
                }
            }
        }
    };

    /**
     * Начать просмотр изображений.
     * @param site ID сайта, для которого нужно выполнить просмотр изображений.
     */
    private void showImages( String site ) {
        Intent intent = new Intent( DVPicActivity.this, FlipViewerActivity.class );
        intent.putExtra( PrefKeys.INTENT_GALLERY_ID, site );
        startActivity( intent );
    }

    /**
     * Обновить изображения со всех сайтов
     */
    private void reloadImages() {
        if( InternetUtils.isConnected( this )) {
            //
            progressBar = DVPicActivity.this.findViewById(R.id.progressBar);
            progressBar.setVisibility(ProgressBar.VISIBLE);


            Preferences prefs = Preferences.getInstance( this );
            //

            Intent intent = new Intent( DVPicActivity.this, WorkerService.class );
            intent.putExtra( PrefKeys.INTENT_SERVICE_CMD, WorkerService.CMD_LOAD_IMAGES );
            intent.putExtra( PrefKeys.INTENT_GALLERY_ID, prefs.getSiteIds() );
            WakeLockService.start( DVPicActivity.this, intent );

            //
        } else {
            DialogUtils.showToast( this, R.string.msg_no_connection );
        }
    }

    /**
     * Установить счетчики картинок для кнопок сайтов.
     */
    @RequiresApi(api = Build.VERSION_CODES.S)
    private void setButtonCounters() {
        // fav images button
        if( mFavButton != null ) {
            HashMap<String, SiteInfo> map = dbAdapter.getSitesInfo();
            SiteInfo si = map.get( Gallery.FAV.name() );
            if( si != null ) {
                mFavButton.setCounter( 0, si.getTotalCounter() );
            } else {
                mFavButton.setCounter( 0, 0 );
            }
        }
        // new images button
        int total = dbAdapter.getNewCount();
        if( mNewButton != null ) {
            mNewButton.setCounter( 0, total );
        }
        // обновить виджет
        DialogUtils.updateWidget( this, total );
    }

    /* WELCOME DIALOG */

    /* SITE SELECTION DIALOG */

    @Override
    public void onSiteSelect( String siteid ) {
        showImages( siteid );
    }

    // is new version
    public boolean appWasUpdated(Context context) throws PackageManager.NameNotFoundException {
        PackageManager manager = context.getPackageManager();
        PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
        int versionCode = info.versionCode;
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        if (sharedPrefs.getInt("last_version_code", 1) != versionCode) {
            sharedPrefs.edit().putInt("last_version_code", versionCode).apply();
            return true;
        }
        return false;
    }
}
