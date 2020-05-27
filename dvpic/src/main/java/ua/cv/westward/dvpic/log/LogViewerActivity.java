package ua.cv.westward.dvpic.log;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;

import ua.cv.westward.dvpic.R;

public class LogViewerActivity extends AppCompatActivity
    implements LoaderManager.LoaderCallbacks<Cursor> {

    private ListView mListView;
    private TextView mEmptyView;
    private LogViewerAdapter mAdapter;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.log_viewer );

        // setup ListView        
        mListView = (ListView) findViewById( R.id.listview );
        mEmptyView = (TextView) findViewById( R.id.empty_view );

        mAdapter = new LogViewerAdapter( this, null );
        mListView.setAdapter((ListAdapter) mAdapter);

        // reset error flag in preferences
        LogPreferences.setErrorFlag( this, false );

        LoaderManager lm = getSupportLoaderManager();        
        lm.initLoader( 0, null, this );        
    }

    /* LOADER CALLBACK EVENTS */

    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle args ) {
        return new LogCursorLoader( this );
    }

    @Override
    public void onLoadFinished( Loader<Cursor> loader, Cursor cursor ) {
        if( cursor != null && cursor.getCount() > 0 ) {
            mAdapter.changeCursor( cursor );
        } else {
            mListView.setEmptyView( mEmptyView );
        }
    }

    @Override
    public void onLoaderReset( Loader<Cursor> loader ) {
        mAdapter.changeCursor( null );
    }    
}
