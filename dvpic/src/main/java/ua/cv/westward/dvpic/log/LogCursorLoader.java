package ua.cv.westward.dvpic.log;

import android.content.Context;
import android.database.Cursor;
import ua.cv.westward.dvpic.db.DBAdapter;
import ua.cv.westward.dvpic.types.AbstractCursorLoader;

public class LogCursorLoader extends AbstractCursorLoader {

    public LogCursorLoader( Context context ) {
        super( context );
    }

    @Override
    protected Cursor buildCursor() {
        DBAdapter dba = DBAdapter.getInstance( getContext() );
        return dba.getLogCursor();
    }
}
