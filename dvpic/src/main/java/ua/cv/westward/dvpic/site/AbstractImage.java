package ua.cv.westward.dvpic.site;

import ua.cv.westward.dvpic.db.DBAdapter;
import android.content.ContentValues;

/**
 * Базовый абстрактный класс изображения, которое загружается с веб-сайта и
 * сохраняется в базе данных.
 * @author Vladimir Kuts
 */
public abstract class AbstractImage {

    public static final int WEB_PAGE  = 0x00000002;   // "Для картинки можно открыть веб-страницу"
    public static final int GIF_IMAGE = 0x00000004;   // "GIF-image"

    protected long    timestamp;  // дата загрузки изображения
    protected String  filename;   // имя файла (sd card)
    protected int     options;    // набор битовых флагов (должен соответствовать AppImage.options)

    /**
     * Конструктор по умолчанию
     */
    public AbstractImage() {
        timestamp = 0;
        filename = null;
        options = 0;
    }

    /* SETTERS */

    /**
     * Установить имя файла изображения на sdcard.
     * @param fname Имя файла изображения.
     */
    public void setFilename( String fname ) {
        this.filename = fname;
        this.timestamp = System.currentTimeMillis();
    }

    /* GETTERS */

    /**
     * Получить SITE ID, идентификатор сайта для записей базы данных.
     * @return
     */
    public abstract String getSiteID();

    /**
     * Получить идентификатор изображения
     * @return
     */
    public abstract String getImageID();

    /**
     * Получить URL изображения
     * @return
     */
    public abstract String getLink();

    /**
     * Получить набор значений для записи нового изображения в базу данных.
     * @return
     */
    public ContentValues getNewValues() {
        // создать новый набор значений для записи в базу данных
        ContentValues newValues = new ContentValues();
        newValues.put( DBAdapter.KEY_LOAD_DATE, timestamp );
        newValues.put( DBAdapter.KEY_FILENAME, filename );
        newValues.put( DBAdapter.KEY_OPTIONS, options );
        return newValues;
    }

    public int getOptions() {
        return options;
    }

    public boolean getOption( int flag ) {
        return (options & flag) > 0;
    }

    public void setOptions( int options ) {
        this.options |= options;
    }
}
