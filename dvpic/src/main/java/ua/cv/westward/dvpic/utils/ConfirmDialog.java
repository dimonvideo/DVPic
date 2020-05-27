package ua.cv.westward.dvpic.utils;

import ua.cv.westward.dvpic.PrefKeys;
import ua.cv.westward.dvpic.R;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;

/**
 * Confirm диалог, на 99% скопированный из документации Android по DialogFragment.
 * OnClick события отрабатываются через интерфейс DialogListener.
 * 
 * @author Vladimir Kuts
 */
public class ConfirmDialog extends DialogFragment {

    public static ConfirmDialog newInstance( int titleID, String msg ) {
        ConfirmDialog dlg = new ConfirmDialog();
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
        builder.setTitle( titleID );
        builder.setMessage( msg );
        
        builder.setNegativeButton( R.string.btn_cancel_title,
            new DialogInterface.OnClickListener() {                    
                @Override
                public void onClick( DialogInterface dialog, int which ) {
                    //dialog.cancel();
                }
            }
        );

        builder.setPositiveButton( R.string.btn_ok_title,
            new DialogInterface.OnClickListener() {            
                @Override
                public void onClick( DialogInterface dialog, int which ) {
                    //
                    ((DialogListener)getActivity()).onPositiveClick();
                }
            }
        );

        return builder.create();
    }
}
