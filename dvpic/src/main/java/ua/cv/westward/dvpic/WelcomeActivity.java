package ua.cv.westward.dvpic;

import ua.cv.westward.dvpic.utils.Utils;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.welcome );

        // set version name field
        TextView version = (TextView) findViewById( R.id.version_text );
        version.setText( Utils.getVersionName( this ));
        
        // set close button onClick action - close activity
        View closeButton = findViewById( R.id.close_btn );
        closeButton.setOnClickListener( new View.OnClickListener() {            
            @Override
            public void onClick( View v ) {
                // store current version to preferences
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(WelcomeActivity.this );
                SharedPreferences.Editor ed = prefs.edit();
                ed.putInt( PrefKeys.APP_VERSION, Utils.getVersion( WelcomeActivity.this ));
                ed.commit();

                // finish this activity
                finish();
            }
        });
        
        // setup webview
        WebView ww = (WebView) findViewById( R.id.webview );
        ww.getSettings().setSupportZoom( false );
//        ww.setBackgroundColor( 0 ); // transparent background
        
        StringBuilder sb = new StringBuilder( "file:///android_asset/" );
        sb.append( getString( R.string.asset_welcome ));
        ww.loadUrl( sb.toString() );                
    }   
}
