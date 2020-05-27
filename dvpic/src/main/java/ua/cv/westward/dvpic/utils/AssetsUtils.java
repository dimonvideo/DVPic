package ua.cv.westward.dvpic.utils;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.annotation.SuppressLint;
import android.content.Context;

public class AssetsUtils {

    /**
     * Returns text file in assets folder as String
     * @param context Application context
     * @param filename filename in 
     * @return
     * @throws IOException
     */
    @SuppressLint("WorldReadableFiles")
    @SuppressWarnings("deprecation")
    public static String getString( Context context, String filename )
            throws IOException {
        
        StringBuilder sb = new StringBuilder();
        InputStream is = null;
        BufferedReader reader = null;
        try {
            is = context.getResources().getAssets().open( filename, Context.MODE_WORLD_READABLE );
            reader = new BufferedReader( new InputStreamReader( is ), 1024 );
            
            String line = "";
            while(( line = reader.readLine()) != null ) {
                sb.append( line );
                sb.append( "\r\n" );
            }
        } finally {
            if( reader != null ) {
                reader.close();
            }
            if( is != null ) {
                is.close();
            }
        }
        return sb.toString();
    }
}
