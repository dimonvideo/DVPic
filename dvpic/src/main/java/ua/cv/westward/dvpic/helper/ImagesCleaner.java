package ua.cv.westward.dvpic.helper;

//import java.util.Calendar;
//import java.util.Date;

import ua.cv.westward.dvpic.db.DBAdapter;
import ua.cv.westward.dvpic.db.ImageCursorWrapper;
import ua.cv.westward.dvpic.utils.FileUtils;

/**
 * Автоудаление изображений.
 * @author Vladimir Kuts
 */
public class ImagesCleaner {
    
    private DBAdapter dbAdapter;
    
    /**
     * Конструктор класса
     * @param dba Адаптер базы данных
     */
    public ImagesCleaner( DBAdapter dba ) { //, int storageAmount ) {
        this.dbAdapter = dba;
    }
    
    /**
     * Удалить изображения для указанного сайта
     * @param siteid Идентификатор сайта
     */
    public void deleteImages( String siteid, int storageSize ) {
        // получить курсор, отсортированный по дате загрузки (load_date)
        String[] id = new String[] { siteid };
        ImageCursorWrapper c = dbAdapter.getImagesCursor( id, false );
        try {
            if( (c.getCount() > storageSize) && c.moveToPosition( storageSize ) ) {
                // запомнить дату первого из устаревших изображений
                long date = c.getLong( c.load_date );
                do {
                    String fname = c.getString( c.filename );
                    // удалить файл изображения
                    FileUtils.deleteFile( fname );
                } while( c.moveToNext() );
                // удалить записи базы данных, дата которых старше указанной
                dbAdapter.deleteImages( siteid, date );
            }
        } finally {
            c.close();
        }
    }
    
    /**
     * Remove error messages older than 7 days
     */
    public void deleteOldLogs() {
        // get date to remove older error messages
        long keep = 7 * 24 * 60 * 60 * 1000;   // 7 days
        long now = System.currentTimeMillis();
        long date = now - keep; 
        
        dbAdapter.deleteLog( date );
    }
}
