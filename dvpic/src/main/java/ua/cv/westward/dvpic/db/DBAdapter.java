package ua.cv.westward.dvpic.db;

import java.util.HashMap;

import ua.cv.westward.dvpic.log.LogRecord;
import ua.cv.westward.dvpic.site.AbstractImage;
import ua.cv.westward.dvpic.site.AppImage;
import ua.cv.westward.dvpic.site.SiteInfo;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Адаптер базы данных sqlite
 * @author Vladimir Kuts
 * 
 * Notes: db.execSQL("VACUUM") ??
 */
public class DBAdapter {

    private static final String DATABASE_NAME    = "dvpic.db";
    private static final int    DATABASE_VERSION = 5;
    private static final String IMAGES_TABLE     = "images";
    private static final String LOG_TABLE        = "log";

    /* IMAGES table fields */
    public static final String KEY_ID           = "_id";
    public static final String KEY_LINK         = "link";    
    public static final String KEY_LOAD_DATE    = "load_date";
    public static final String KEY_FILENAME     = "filename";
    public static final String KEY_VIEW_STATUS  = "view_status";
    public static final String KEY_SITEID       = "siteid";
    public static final String KEY_IMAGEID      = "imageid";
    public static final String KEY_TITLE        = "title";    
    public static final String KEY_SITE_DATE    = "site_date";
    public static final String KEY_AUTHOR       = "author";
    public static final String KEY_OPTIONS      = "options";
    
    /* LOG table fields */
    public static final String KEY_EVENT_ID     = "_id";
    public static final String KEY_TIMESTAMP    = "timestamp";
    public static final String KEY_EVENT_SITEID = "siteid";
    public static final String KEY_RESULT       = "result";
    public static final String KEY_MESSAGE      = "msg";
    
    private static final int ID_NEW_IMAGE       = 2;
    private static final int ID_MARKED_IMAGE    = 1;
    private static final int ID_OLD_IMAGE       = 0;
    
    /** DATABASE VERSION MANAGEMENT */
    
    private static class DBOpenHelper extends SQLiteOpenHelper {

        /**
         * Конструктор класса. 
         */
        public DBOpenHelper( Context context, String name,
                CursorFactory factory, int version ) {
            super( context, name, factory, version );
        }

        /**
         * SQL Statements to create a new database
         * 
         * VIEW_STATUS: 2 - Новое, только что загруженное изображение
         *              1 - Изображение, которое уже просмотрели, но просмотр галереи не закончен
         *              0 - Просмотренное изображение 
         */
        private static final String IMAGES_CREATE = "create table " +
            IMAGES_TABLE + " (" +
            KEY_ID            + " integer primary key autoincrement, " +
            KEY_LINK          + " text not null, " +
            KEY_LOAD_DATE     + " long not null, " +
            KEY_FILENAME      + " text not null, " +
            KEY_VIEW_STATUS   + " integer default 2, " +
            KEY_SITEID        + " text not null, " +
            KEY_IMAGEID       + " long not null, " +
            KEY_TITLE         + " text, " +
            KEY_SITE_DATE     + " long, " +
            KEY_OPTIONS       + " integer default 0);";

        private static final String LOG_CREATE = "create table " +
            LOG_TABLE + " (" +
            KEY_EVENT_ID      + " integer primary key autoincrement, " +
            KEY_TIMESTAMP     + " long not null, " +
            KEY_EVENT_SITEID  + " text not null, " +
            KEY_RESULT        + " text not null, " +
            KEY_MESSAGE       + " text not null);";

        @Override
        public void onCreate( SQLiteDatabase db ) {
            db.execSQL( IMAGES_CREATE );
            db.execSQL( LOG_CREATE );
        }

        @Override
        public void onUpgrade( SQLiteDatabase db, int oldVersion, int newVersion ) {

            // !! ВНИМАНИЕ !!            
            // Не использовать оператор break при переходе от младших версий
            // к старшим, чтобы последовательно подтянуть версию базы данных
            
            switch( oldVersion ) {
                case 1: {
                    // переход с версии 1 (8 декабря 2011)
                    db.execSQL( "ALTER TABLE images ADD COLUMN options integer default 0" );                
                }
                case 2:
                case 3: {
                    // переход с версий 2,3 (7 августа 2012)
                    db.execSQL( "DROP TABLE IF EXISTS " + LOG_TABLE );
                    db.execSQL( LOG_CREATE );
                    break;
                }
                default: {
                    // drop the old data
                    db.execSQL( "DROP TABLE IF EXISTS " + IMAGES_TABLE );
                    db.execSQL( "DROP TABLE IF EXISTS " + LOG_TABLE );
                    // create a new one
                    onCreate( db );
                    break;                    
                }            
            }            
        }        
    }
    
    /** DATABASE ADAPTER */
    
    private static DBAdapter mInstance = null;  // singleton reference
    private SQLiteDatabase db;

    protected DBAdapter( Context context ) {
        DBOpenHelper helper = new DBOpenHelper( context.getApplicationContext(),
                                     DATABASE_NAME, null, DATABASE_VERSION );
        try {
            db = helper.getWritableDatabase();
        } catch( SQLiteException ex ) {
            db = helper.getReadableDatabase();
        }
    }
    
    /**
     * Получить экземпляр адаптера базы данных (singleton pattern)
     * @param ctx Контекст вызова
     * @return Ссылка на экземпляр класса DBAdapter
     */
    public static synchronized DBAdapter getInstance( Context ctx ) {
        if( null == mInstance ) {
            mInstance = new DBAdapter( ctx );
        }
        return mInstance;
    }
    
    /** ADD, UPDATE, DELETE IMAGES RECORDS */

    /**
     * Добавить изображение в базу.
     * @param image Объект изображения, загруженного с интернет-сайта.
     * @return
     */
    public boolean insertImage( AbstractImage image ) {
        long rowid = db.insert( IMAGES_TABLE, null, image.getNewValues() );
        return rowid == -1 ? false : true;
    }
    
    /**
     * Добавить изображения в базу. 
     * @param image Инфо-объект приложения.
     * @return
     */
    public boolean insertImage( AppImage image ) {
        long rowid = db.insert( IMAGES_TABLE, null, image.getNewValues() );
        return rowid == -1 ? false : true;
    }
    
    /**
     * Обновить данные изображения
     * @param image Объект изображения
     * @return
     */
    public boolean updateImage( AppImage image ) {
        String args[] = new String[] { Long.toString( image.getID() ) };
        return db.update( IMAGES_TABLE, image.getUpdateValues(), KEY_ID + "=?", args ) > 0;
    }
    
    /**
     * Изменить статус изображений на "просмотренный", если они отмечены как 
     * "предварительно просмотренные".
     * @return
     */
    public boolean updateImages() {
        String where = KEY_VIEW_STATUS + "=" + ID_MARKED_IMAGE;
        ContentValues updValues = new ContentValues();
        updValues.put( DBAdapter.KEY_VIEW_STATUS, AppImage.STATUS_OLD );        
        return db.update( IMAGES_TABLE, updValues, where, null ) > 0;
    }

    /**
     * Изменить статус всех изображений для указанных сайтов
     * @param siteids Массив ID сайтов.
     * @param status true: "новая картинка", false: "просмотренная" 
     * @return
     */
    public boolean updateImages( String[] siteids, boolean status ) {
        ContentValues updValues = new ContentValues();
        int st = status ? ID_NEW_IMAGE : ID_OLD_IMAGE; 
        updValues.put( DBAdapter.KEY_VIEW_STATUS, st );

        String where = String.format( KEY_SITEID + " IN(%s)", arrayToString( siteids ));
        return db.update( IMAGES_TABLE, updValues, where, null ) > 0;
    }
    
    /**
     * Удалить изображение из базы
     * @param image Объект изображения
     * @return
     */
    public boolean deleteImage( AppImage image ) {
        String where = KEY_ID + "=?";
        String args[] = new String[] { Long.toString( image.getID() ) };
        return db.delete( IMAGES_TABLE, where, args ) > 0;
    }
    
    /**
     * Удалить все устаревшие записи из таблицы IMAGES
     * @param date UNIX дата в миллисекундах
     * @return
     */
    public boolean deleteImages( long date ) {
        String args[] = new String[] { Long.toString( date ) };
        return db.delete( IMAGES_TABLE, KEY_LOAD_DATE + "<?", args ) > 0;
    }
    
    /**
     * Удалить записи ОДНОЙ указанной галереи, дата загрузки которых старше
     * указанной (меньше или равна). 
     * @param siteid ID сайта, для которого удаляются записи.
     * @param date Дата, начиная с которой удаляются записи.
     * @return
     */
    public boolean deleteImages( String siteid, long date ) {
        String where = KEY_SITEID + "=? AND " + KEY_LOAD_DATE + "<=?";
        String args[] = new String[] { siteid, Long.toString( date ) };        
        return db.delete( IMAGES_TABLE, where, args ) > 0;
    }
    
    /**
     * Проверить наличие изображения в базе
     * @param image Объект изображения
     * @return
     */
    public boolean isImageExists( AbstractImage image ) {
        boolean result = false;
        String where = KEY_SITEID + "=? AND " + KEY_IMAGEID + "=?";
        String[] columns = new String[] { KEY_ID };
        String[] args = new String[] { image.getSiteID(), image.getImageID() };
        Cursor c = db.query( IMAGES_TABLE, columns, where, args, null, null, null );
        try {
            if( c.getCount() > 0 ) {
                result = true;
            }
        } finally {
            c.close();
        }
        return result;
    }
    
    /** ADD, DELETE LOG RECORDS */

    /**
     * Добавить запись в таблицу лога.
     * @param record
     * @return
     */
    public boolean insertLog( LogRecord record ) {
        long rowid = db.insert( LOG_TABLE, null, record.getValues() );
        return rowid != -1 ? true : false;
    }
    
    /**
     * Удалить все устаревшие записи из таблицы LOG
     * @param time
     * @return
     */
    public boolean deleteLog( long time ) {
        String args[] = new String[] { Long.toString( time ) };
        return db.delete( LOG_TABLE, KEY_TIMESTAMP + "<?", args ) > 0;
    }
    
    /**
     * &#x423;&#x434;&#x430;&#x43b;&#x438;&#x442;&#x44c; &#x437;&#x430;&#x43f;&#x438;&#x441;&#x438; &#x438;&#x437; &#x442;&#x430;&#x431;&#x43b;&#x438;&#x446;&#x44b; LOG
     * @return
     */
    public boolean deleteLog( String siteid ) {
        String[] args = new String[] { siteid };
        return db.delete( LOG_TABLE, KEY_EVENT_SITEID + "=?", args ) > 0;
    }
    
    /** HELPER METHODS TO HANDLE QUERIES */
    
    private static final String SELECT_ALL_IMAGES_QUERY = KEY_SITEID + " IN(%s)";
    private static final String SELECT_NEW_IMAGES_QUERY = KEY_SITEID + " IN(%s) AND " + KEY_VIEW_STATUS + ">0";
    private static final String SELECT_IMAGES_VIEW_ORDER = KEY_LOAD_DATE + " DESC";
    
    /**
     * Получить курсор, включающий в себя записи изображений для указанных сайтов 
     * с сортировкой "вначале новые записи".
     * @return
     */
    public ImageCursorWrapper getImagesCursor( String[] ids, boolean newImages ) {
        // build the where query
        String where;
        if( newImages ) {
            where = String.format( SELECT_NEW_IMAGES_QUERY, arrayToString( ids ));
        } else {
            where = String.format( SELECT_ALL_IMAGES_QUERY, arrayToString( ids ));
        }        
        return new ImageCursorWrapper( db.query( IMAGES_TABLE, null,
                where, null, null, null, SELECT_IMAGES_VIEW_ORDER ));                
    }
    
    /**
     * Найти картинку по идентификаторам сайта и картинки. Вернуть объект
     * метаданных изображения или null, если картинка не найдена. 
     * @param siteid
     * @param imageid
     * @return
     */
    public AppImage findImage( String siteid, String imageid ) {
        final String where = KEY_IMAGEID + "=? AND " + KEY_SITEID + "=?";          
        String[] args = new String[] { imageid, siteid };
        Cursor c = db.query( IMAGES_TABLE, null, where, args, null, null, null );
        try {
            if( c.moveToFirst() ) {
                return new AppImage( new ImageCursorWrapper( c ));
            }
        } finally {
            c.close();
        }
        return null;
    }
        
    /**
     * Найти картинку по идентификатору картинки. Вернуть объект
     * метаданных изображения или null, если картинка не найдена. 
     * @param imageid
     * @return
     */
    public AppImage findImage( String imageid ) {
        final String where = KEY_IMAGEID + "=?";
        final String[] args = new String[] { imageid };
        Cursor c = db.query( IMAGES_TABLE, null, where, args, null, null, null );
        try {
            if( c.moveToFirst() ) {
                return new AppImage( new ImageCursorWrapper( c ) );
            }
        } finally {
            c.close();
        }
        return null;
    }
    
    /**
     * Получить курсор, включающий в себя все записи лога
     * @return
     */
    public Cursor getLogCursor() {
//        final String where = KEY_EVENT_SITEID + "=? "; 
//        String[] args = new String[] { siteid };
        return db.query( LOG_TABLE, null, null, null, null, null,
                KEY_TIMESTAMP + " DESC" );
    }
        
    /**
     * Получить статистику по загруженным картинкам.
     * @return Map с объектам SiteInfo информации по загруженным картинкам.  
     */
    public HashMap<String, SiteInfo> getSitesInfo() {
        
        HashMap<String, SiteInfo> map = new HashMap<String, SiteInfo>();
        
        final String[] cols = { KEY_SITEID, "COUNT(siteid) AS total", "COUNT(CASE WHEN view_status=2 THEN 1 ELSE null END) AS new" };
        Cursor c = db.query( IMAGES_TABLE, cols, null, null, KEY_SITEID, null, null );         
        try {
            if( (c.getCount() > 0) && c.moveToFirst() ) {
                do {
                    SiteInfo si = new SiteInfo( c.getInt( c.getColumnIndex( "total" )),
                                                c.getInt( c.getColumnIndex( "new" )) );
                    String key = c.getString( c.getColumnIndex( KEY_SITEID ));
                    map.put( key, si );
                } while( c.moveToNext() );
            }
        } finally {
            c.close();
        }
        return map;
    }
    
    /**
     * Получить общее количество новых картинок
     * @return
     */
    public int getNewCount() {
        final String[] cols = { "COUNT(_id) AS count" };
        final String selection = KEY_VIEW_STATUS + "=" + ID_NEW_IMAGE;
        Cursor c = db.query( IMAGES_TABLE, cols, selection, null, null, null, null );
        try {
            if( c.moveToFirst() ) {
                return c.getInt( 0 );   // column index = 0
            }
        } finally {
            c.close();
        }
        return 0;
    }
    
    /** SERVICE METHODS */
    
    /**
     * Преобразовать массив строк в escaped строку 
     * @param array Исходный массив строк
     * @return Результирующая строка
     */
    private static String arrayToString( String[] array ) {
        StringBuilder sb = new StringBuilder();
        int len = array.length;

        if( len > 0 ) {
            appendEscapedString( sb, array[0] );
            for( int i = 1; i < len; i++ ) {
                sb.append( ',' );
                appendEscapedString( sb, array[i] );
            }
        }
        return sb.toString();
    }
    
    /**
     * Добавить в буфер строку, ограниченную escape символами
     * @param sb Строковый буфер.
     * @param str Строка, которая добавляется в буфер.
     */
    private static void appendEscapedString( StringBuilder sb, String str ) {
        sb.append( '\'' );
        sb.append( str );
        sb.append( '\'' );
    }
    
    /** DEPRECATED */
    
    /**
     * Добавить массив строк в StringBuilder. Строки разделяются символом ','.
     * @param sb
     * @param array
     * @return
     */
/*    private static StringBuilder appendArray( StringBuilder sb, String[] array ) {
        int len = array.length;
        if( len > 0 ) {
            appendEscapedString( sb, array[0] );
            for( int i = 1; i < len; i++ ) {
                sb.append( ',' );
                appendEscapedString( sb, array[i] );
            }
        }
        return sb;
    }*/

    //  private static final String SELECTION_IMAGES_ID = KEY_SITEID + "=?"; 
    
  /**
   * Получить курсор, включающий в себя записи изображений для указанного сайта
   * @param siteid ID сайта
   * @return
   */
/*    public Cursor getImagesCursor( int siteid ) {
      String[] columns = new String[] { "*" };
      String[] args = new String[] { Integer.toString(siteid) };
      return db.query( IMAGES_TABLE, columns, SELECTION_IMAGES_ID, args, null,
              null, KEY_LOAD_DATE + " DESC" );
  }*/
  
//  private static final String SELECTION_IMAGES_DATE = KEY_LOAD_DATE + "<?"; 

  /**
   * Получить курсор, включающий в себя записи изображений для всех сайтов,
   * старше указанной даты 
   * @param date UNIX дата в миллисекундах
   * @return
   */
/*    public ImageCursorWrapper getImagesCursor( long date ) {
      String[] args = new String[] { Long.toString(date) };
      return new ImageCursorWrapper( db.query( IMAGES_TABLE, null,
              SELECTION_IMAGES_DATE, args, null, null, null ));
  }*/
  
}
