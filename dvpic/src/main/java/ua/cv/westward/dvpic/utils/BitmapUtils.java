package ua.cv.westward.dvpic.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class BitmapUtils {

    /**
     * Открыть битмап, выполнить resize если необходимо 
     * @return
     * @throws IOException
     */
    public static Bitmap decodeBitmap( File file, int maxSize )
            throws IOException {
                
        // decode image size
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;

        FileInputStream fis = new FileInputStream( file );
        BitmapFactory.decodeStream( fis, null, o );
        fis.close();
        
        // calculate scale factor
        int scale = 1;
        if( o.outHeight > maxSize || o.outWidth > maxSize ) {
            scale = (int)Math.pow(2, (int) Math.round(Math.log(maxSize / (double) Math.max(o.outHeight, o.outWidth)) / Math.log(0.5)));
        }

        // decode with inSampleSize
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        fis = new FileInputStream( file );
        Bitmap bitmap = BitmapFactory.decodeStream( fis, null, o2 );
        fis.close();
       
        return bitmap;
    }    
}
