package ua.cv.westward.dvpic.log;

import ua.cv.westward.dvpic.db.DBAdapter;
import ua.cv.westward.dvpic.site.Site;
import android.content.ContentValues;
import android.database.Cursor;

/**
 * Класс объекта лог-записей базы данных. 
 * @author Vladimir Kuts
 */
public class LogRecord {
    
    /* Варианты результатов выполнения действий */
    public enum Result { OK, ERROR; }
    
    private final long   timestamp;
    private final String siteid;
    private Result result;
    private final String message;
    
    /**
     * Конструктор класса, создает новую запись лога.
     */
    public LogRecord( String siteid, Result result, String message ) {
        this.timestamp = System.currentTimeMillis();    // установить текущую дату
        this.siteid = siteid;
        this.result = result;
        this.message = message;
    }
    
    /**
     * Constructor, creates a new object from database cursor
     * @param cursor
     */
    public LogRecord( Cursor cursor ) {
        this.timestamp = cursor.getLong( 1 );
        this.siteid = cursor.getString( 2 );
        try {
            this.result = Result.valueOf( cursor.getString( 3 ));
        } catch( Exception e ) {
            this.result = Result.ERROR;
        }
        this.message = cursor.getString( 4 );
    }
    
    /**
     * Получить набор значений для записи в базу данных.
     * @return
     */
    public ContentValues getValues() {
        // создать новый набор значений для записи в базу данных
        ContentValues newValues = new ContentValues( 4 );        
        newValues.put( DBAdapter.KEY_TIMESTAMP, timestamp );
        newValues.put( DBAdapter.KEY_EVENT_SITEID, siteid );
        newValues.put( DBAdapter.KEY_RESULT, result.name() );
        newValues.put( DBAdapter.KEY_MESSAGE, message );
        return newValues;
    }
    
    public long getDate() {
        return timestamp;
    }
    
    public String getSiteName() {
        try {
            Site site = Site.valueOf( siteid );
            return site.getTitle();
        } catch( Exception e ) {
            return siteid;
        }
    }
    
    public boolean getResult() {
        return result == Result.OK;
    }
    
    public String getMessage() {
        return message;
    }
}
