package ua.cv.westward.dvpic.log;

import ua.cv.westward.dvpic.R;
import ua.cv.westward.dvpic.utils.Utils;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class LogListItem extends RelativeLayout {

    private ImageView mStatus;
    private TextView mDate;
    private TextView mSite;
    private TextView mMessage;
    
    public LogListItem( Context context ) {
        super( context );

        // Inflate the view from the layout resource.
        LayoutInflater li = LayoutInflater.from( context );
        View v = li.inflate( R.layout.log_item, this, true );
        int padding = Utils.convertDip2Pixels( context, 8 );
        v.setPadding( padding, padding, padding, padding );
        
        // get UI references
        mStatus = (ImageView) v.findViewById( R.id.status );
        mDate = (TextView) v.findViewById( R.id.date );
        mSite = (TextView) v.findViewById( R.id.site );
        mMessage = (TextView) v.findViewById( R.id.message );
    }
    
    /* SETTERS */
    
    public void setMessage( LogRecord record ) {
        int st = record.getResult() ? R.drawable.ic_status_green : R.drawable.ic_status_red;
        mStatus.setImageResource( st );
        mDate.setText( Utils.formatDate( record.getDate() ));
        mSite.setText( record.getSiteName() );
        mMessage.setText( record.getMessage() );
    }
}
