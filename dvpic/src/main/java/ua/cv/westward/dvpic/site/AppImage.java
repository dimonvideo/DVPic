package ua.cv.westward.dvpic.site;

import java.text.SimpleDateFormat;

import ua.cv.westward.dvpic.db.DBAdapter;
import ua.cv.westward.dvpic.db.ImageCursorWrapper;
import android.content.ContentValues;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Объект картинки, которая читается или записывается в базу данных.
 * @author Vladimir Kuts
 */
public class AppImage implements Parcelable {

    public static final int STATUS_NEW  = 2;            // новая картинка
    public static final int STATUS_MARK = 1;            // картинка отмечена как просмотренная
    public static final int STATUS_OLD  = 0;            // старая картинка

    public static final int FAVORITE    = 0x00000001;   // флаг "Избранная картинка"
    public static final int WEB_PAGE    = 0x00000002;   // "Для картинки можно открыть веб-страницу"
    public static final int GIF_IMAGE   = 0x00000004;

    /* ВНИМАНИЕ! при изменении полей корректировать parcelable код !! */

    private final long    _id;        // идентификатор в базе данных
    private final String  link;       // url изображения
    private final long    loadDate;   // дата загрузки изображения
    private final String  filename;   // полное имя файла (sd card)
    private int           viewStatus; //
    private final String  siteid;     // идентификатор сайта, с которого загружено изображение
    private final String  imageid;    // идентификатор изображения
    private final String  title;      // заголовок изображения
    private final long    siteDate;   // дата размещения на сайте (ms)
    private int           options;    // набор битовых полей (флагов)

    /**
     * Конструктор, который создает объект изображения из курсора базы данных.
     * @param c Cursor wrapper базы данных
     */
    public AppImage( ImageCursorWrapper c ) {
        _id = c.getLong( c.id );
        link = c.getString( c.link );
        loadDate = c.getLong( c.load_date );
        filename = c.getString( c.filename );
        viewStatus = c.getInt( c.viewStatus );
        siteid = c.getString( c.siteid );
        title = c.getString( c.title );
        siteDate = c.getLong( c.site_date );
        options = c.getInt( c.options );
        imageid = c.getString( c.imageid );
    }

    /**
     * Конструктор, копирующий данные из другого объекта AppImage.
     * @param src Исходный объект изображения.
     * @param siteid Новый ID сайта.
     * @param filename Новый полный путь к файлу изображения.
     */
    public AppImage( AppImage src, String siteid, String filename ) {
        this._id = 0;
        this.filename = filename;
        this.imageid = src.imageid;
        this.link = src.link;
        this.loadDate = System.currentTimeMillis(); //= src.loadDate;
        this.options = src.options;
        this.siteDate = src.siteDate;
        this.siteid = siteid;
        this.title = src.title;
        this.viewStatus = src.viewStatus;
    }

    /* SETTERS */

    /**
     * Изменить статус просмотра изображения.
     * @param status Новое значение статуса.
     */
    public void setViewStatus( int status ) {
        this.viewStatus = status;
    }

    /**
     * Сервисный метод установки или сброса флага переменной options.
     * @param flag Флаг, который необходимо изменить.
     * @param value (Boolean) Установить или сбросить флаг.
     */
    public void setOption( int flag, boolean value ) {
        if( value ) {
            options |= flag;
        } else {
            options &= ~flag;
        }
    }

    /* GETTERS */

    public long    getID()          { return _id; };
    public String  getFilename()    { return filename; }
    public String  getSiteID()      { return siteid; }
    public String  getImageID()     { return imageid; }
    public String  getLink()        { return link; }
    public String  getTitle()       { return title; }
    public int     getStatus()      { return viewStatus; }

    public String getSiteDate() {
        if( siteDate > 0 ) {
            SimpleDateFormat sdf = new SimpleDateFormat();
            String date = sdf.format( siteDate );
            return date;
        } else {
            return null;
        }
    }

    /**
     * Сервисный метод получения значения флага переменной options.
     * @param flag Флаг, который необходимо проверить.
     * @return
     */
    public boolean getOption( int flag ) {
        return (options & flag) > 0;
    }

    /* DATABASE SUPPORT */

    /**
     * Получить набор значений для обновления метаданных изображения
     * @return
     */
    public ContentValues getUpdateValues() {
        ContentValues updValues = new ContentValues();
        updValues.put( DBAdapter.KEY_VIEW_STATUS, viewStatus );
        updValues.put( DBAdapter.KEY_OPTIONS, options );
        return updValues;
    }

    /**
     * Получить набор значений для копирования метаданных изображения
     * @return
     */
    public ContentValues getNewValues() {
        ContentValues cv = new ContentValues();
        cv.put( DBAdapter.KEY_FILENAME, filename );
        cv.put( DBAdapter.KEY_IMAGEID, imageid );
        cv.put( DBAdapter.KEY_LINK, link );
        cv.put( DBAdapter.KEY_LOAD_DATE, loadDate );
        cv.put( DBAdapter.KEY_OPTIONS, options );
        cv.put( DBAdapter.KEY_SITE_DATE, siteDate );
        cv.put( DBAdapter.KEY_SITEID, siteid );
        cv.put( DBAdapter.KEY_TITLE, title );
        cv.put( DBAdapter.KEY_VIEW_STATUS, viewStatus );
        return cv;
    }

    /* PARCELABLE IMPLEMENTATION */

    private AppImage( Parcel in ) {
        _id = in.readLong();
        filename = in.readString();
        imageid = in.readString();
        link = in.readString();
        loadDate = in.readLong();
        options = in.readInt();
        siteDate = in.readLong();
        siteid = in.readString();
        title = in.readString();
        viewStatus = in.readInt();
    }

    @Override
    public void writeToParcel( Parcel out, int flags ) {
        out.writeLong( _id );
        out.writeString( filename );
        out.writeString( imageid );
        out.writeString( link );
        out.writeLong( loadDate );
        out.writeInt( options );
        out.writeLong( siteDate );
        out.writeString( siteid );
        out.writeString( title );
        out.writeInt( viewStatus );
    }

    public static final Parcelable.Creator<AppImage> CREATOR =
            new Parcelable.Creator<AppImage>() {
                public AppImage createFromParcel( Parcel in ) {
                    return new AppImage( in );
                }

                public AppImage[] newArray( int size ) {
                    return new AppImage[size];
                }
            };

    @Override
    public int describeContents() {
        return 0;
    }
}
