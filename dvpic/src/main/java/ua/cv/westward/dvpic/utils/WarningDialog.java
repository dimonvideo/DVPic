package ua.cv.westward.dvpic.utils;

import ua.cv.westward.dvpic.PrefKeys;
import ua.cv.westward.dvpic.R;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;

public class WarningDialog extends DialogFragment {

    public static WarningDialog newInstance( int titleID, String msg ) {
        WarningDialog dlg = new WarningDialog();
        Bundle args = new Bundle();
        args.putInt( PrefKeys.DIALOG_ERR_TITLE, titleID );
        args.putString( PrefKeys.DIALOG_ERR_MSG, msg );
        dlg.setArguments( args );
        return dlg;
    }
    
    @Override
    public Dialog onCreateDialog( Bundle savedInstanceState ) {
        Bundle args = getArguments();
        int titleID = args.getInt( PrefKeys.DIALOG_ERR_TITLE );
        String msg = args.getString( PrefKeys.DIALOG_ERR_MSG );
        
        AlertDialog.Builder builder = new AlertDialog.Builder( getActivity() );
        builder.setIcon( R.drawable.ic_dialog_alert );
        builder.setTitle( titleID );
        builder.setMessage( msg );
        builder.setNeutralButton( R.string.btn_close_title,
            new DialogInterface.OnClickListener() {
                @Override
                public void onClick( DialogInterface dialog, int whichButton ) {
                    //dialog.cancel();
                }
            }
        );
        return builder.create();
    }
}
