package ua.cv.westward.dvpic.site;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

import ua.cv.westward.dvpic.PrefKeys;
import ua.cv.westward.dvpic.utils.FileUtils;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

/**
 * Конфигурацинные параметры сайтов, с которых приложение загружает приложения.
 * @author Vladimir Kuts
 *
 */
public class SiteParameters {

    private final Site mSite;
    private int mStorageSize;
    private NetworkOptions mAllowedGifNetwork;

    /**
     * Конструктор
     * @param siteid Строковый идентификатор сайта
     */
    public SiteParameters( Context context, String siteid ) {
        mSite = Site.valueOf( siteid );
        //
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        try {
            StringBuilder sb = new StringBuilder( PrefKeys.STORAGE_TEMPLATE );
            sb.append( mSite.name() );
            mStorageSize = Integer.parseInt(Objects.requireNonNull(prefs.getString(sb.toString(), "200")));
        } catch( Exception e ) {
            mStorageSize = 100;
        }
        try {
            String s = prefs.getString( PrefKeys.GIF_NETWORK_TYPE, "WIFI" );
            mAllowedGifNetwork = NetworkOptions.valueOf( s );
        } catch( Exception e ) {
            mAllowedGifNetwork = NetworkOptions.WIFI;
        }
    }

    /* GETTERS */

    public String   getSiteTitle()      { return mSite.getTitle(); }
    public Class<?> getHelper()         { return mSite.getHelper(); }
    public String   getFeedURL()        { return mSite.getFeedURL(); }
    public String   getSiteID()         { return mSite.name(); }

    /**
     * Получить полный url веб-страницы картинки или null, если веб-страница
     * неизвестна или не может быть получена.
     */
    public String getWebPageURL( String imageID ) {
        if( mSite.getPageURL() == null )
            return null;

        StringBuilder sb = new StringBuilder( mSite.getPageURL() );
        sb.append( imageID );
        return sb.toString();
    }
    public String getVideoPageURL( String imageID ) {
        if( mSite.getPageURL() == null )
            return null;

        StringBuilder sb = new StringBuilder( mSite.getVideoURL() );
        sb.append( imageID );
        return sb.toString();
    }
    /**
     * Get website base path
     * @return
     */
    public String getBaseUrl() {
        return mSite.getPageURL();
    }

    /**
     * Получить полный путь к папке сайта на sd card.
     * @return
     * @throws IOException
     */
    public String getImagesFolder(Context context) throws IOException {
        return FileUtils.getPathSD( PrefKeys.SD_FILES_PATH, mSite.getFolder(), context );
    }

    /**
     * Получить значение настройки "Макс. количество картинок"
     * @return
     */
    public int getStorageSize() {
        return mStorageSize;
    }

    public NetworkOptions getAllowedGifNetwork() {
        return mAllowedGifNetwork;
    }
}
