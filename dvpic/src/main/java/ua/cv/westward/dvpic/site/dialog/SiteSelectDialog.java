package ua.cv.westward.dvpic.site.dialog;

import java.util.HashMap;

import ua.cv.westward.dvpic.Preferences;
import ua.cv.westward.dvpic.R;
import ua.cv.westward.dvpic.db.DBAdapter;
import ua.cv.westward.dvpic.site.Site;
import ua.cv.westward.dvpic.site.SiteInfo;
import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;

public class SiteSelectDialog extends DialogFragment implements OnClickListener, OnItemClickListener {


    /* Callback interface */

    public interface OnSiteSelectCallback {
        public void onSiteSelect( String siteid );
    }

    /* Implementation */

    private static OnSiteSelectCallback sDummyCallback = new OnSiteSelectCallback() {
        @Override
        public void onSiteSelect( String siteid ) {
        }
    };

    private OnSiteSelectCallback mCallback = sDummyCallback;
    private ListView mListView;

    @Override
    public void onAttach( Activity activity ) {
        super.onAttach( activity );
        if( !(activity instanceof OnSiteSelectCallback) ) {
            throw new IllegalStateException( "Activity must implement dialogs's callbacks." );
        }
        mCallback = (OnSiteSelectCallback) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallback = sDummyCallback;
    }

    @Override
    public void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setStyle( DialogFragment.STYLE_NO_TITLE, R.style.Theme_App_Dialog );
    }

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState ) {
        View v = inflater.inflate( R.layout.dialog_holo_template, container, false );

        // set title
        TextView title = (TextView) v.findViewById( R.id.title );
        title.setText( R.string.dialog_site_select );

        // inflate dialog contents
        FrameLayout content = (FrameLayout) v.findViewById( R.id.content );
        inflater.inflate( R.layout.dialog_list_cancel, content );

        // setup listview
        mListView = (ListView) v.findViewById( R.id.listview );
        mListView.setOnItemClickListener( this );
        mListView.setAdapter( new SiteListAdapter() );

        // set onClick listeners for buttons
        v.findViewById( R.id.cancel_btn ).setOnClickListener( this );

        return v;
    }

    /**
     * ListView OnClick listener
     */
    @Override
    public void onItemClick( AdapterView<?> parent, View view, int position, long id ) {
        String siteid = (String) mListView.getAdapter().getItem( position );
        mCallback.onSiteSelect( siteid );
        dismiss();
    }

    /**
     * Ok, Cancel buttons OnClick listener
     */
    @Override
    public void onClick( View v ) {
        dismiss();
    }

    /* ViewHolder */

    private class ViewHolder {
        TextView title;
        TextView counter;
    }

    /* Site list adapter */

    private class SiteListAdapter extends BaseAdapter {

        private final LayoutInflater mInflater;
        private final String[] mSites;
        private final HashMap<String, SiteInfo> mCounters;

        public SiteListAdapter() {
            mInflater = LayoutInflater.from( getActivity() );

            Preferences prefs = Preferences.getInstance( getActivity() );
            mSites = prefs.getSiteIds();

            DBAdapter dba = DBAdapter.getInstance( getActivity() );
            mCounters = dba.getSitesInfo();
        }

        @Override
        public int getCount() {
            return mSites.length;
        }

        @Override
        public Object getItem( int position ) {
            return mSites[position];
        }

        @Override
        public long getItemId( int position ) {
            return position;
        }

        @Override
        public View getView( int position, View convertView, ViewGroup parent ) {
            ViewHolder vh;
            if( convertView == null ) {
                convertView = mInflater.inflate( R.layout.site_select_item, null );
                // cache UI reference
                vh = new ViewHolder();
                vh.title = (TextView) convertView.findViewById( android.R.id.text1 );
                vh.counter = (TextView) convertView.findViewById( R.id.counter );
                convertView.setTag( vh );
            } else {
                vh = (ViewHolder) convertView.getTag();
            }

            // bind data
            String id = (String) getItem( position );

            // title
            Site site = Site.valueOf( id );
            vh.title.setText( site.getTitle() );

            // counter
            SiteInfo si = mCounters.get( id );
            if( si != null ) {
                int c = si.getNewCounter();
                if( c > 0 ) {
                    vh.counter.setText( String.valueOf( c ));
                    vh.counter.setVisibility( View.VISIBLE );
                } else {
                    vh.counter.setVisibility( View.GONE );
                }
            } else {
                vh.counter.setVisibility( View.GONE );
            }
            return convertView;
        }
    }
}
