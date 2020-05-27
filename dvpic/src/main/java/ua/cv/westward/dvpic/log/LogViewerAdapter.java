package ua.cv.westward.dvpic.log;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;

import androidx.cursoradapter.widget.CursorAdapter;

public class LogViewerAdapter extends CursorAdapter {

    public LogViewerAdapter( Context context, Cursor c ) {
        super( context, c, 0 );
    }

    @Override
    public void bindView( View view, Context context, Cursor cursor ) {
        LogListItem item = (LogListItem) view;
        // use LogRecord object to retrieve data from cursor
        item.setMessage( new LogRecord( cursor ) );
    }

    @Override
    public View newView( Context context, Cursor cursor, ViewGroup parent ) {
        return new LogListItem( context );
    }    
}
