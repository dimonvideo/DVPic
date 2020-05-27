package ua.cv.westward.dvpic.db;

import android.database.Cursor;
import android.database.CursorWrapper;

public class ImageCursorWrapper extends CursorWrapper {

    // индексы колонок курсора
    public final int id;
    public final int link;
    public final int load_date;
    public final int filename;
    public final int viewStatus;
    public final int siteid;
    public final int imageid;
    public final int title;
    public final int site_date;
    public final int options;

    public ImageCursorWrapper( Cursor cursor ) {
        super( cursor );
        
        id = cursor.getColumnIndexOrThrow( DBAdapter.KEY_ID );
        link = cursor.getColumnIndexOrThrow( DBAdapter.KEY_LINK );
        load_date = cursor.getColumnIndexOrThrow( DBAdapter.KEY_LOAD_DATE );
        filename = cursor.getColumnIndexOrThrow( DBAdapter.KEY_FILENAME );
        viewStatus = cursor.getColumnIndexOrThrow( DBAdapter.KEY_VIEW_STATUS );
        siteid = cursor.getColumnIndexOrThrow( DBAdapter.KEY_SITEID );
        imageid = cursor.getColumnIndexOrThrow( DBAdapter.KEY_IMAGEID );
        title = cursor.getColumnIndexOrThrow( DBAdapter.KEY_TITLE );
        site_date = cursor.getColumnIndexOrThrow( DBAdapter.KEY_SITE_DATE );
        options = cursor.getColumnIndexOrThrow( DBAdapter.KEY_OPTIONS );
    }
}
