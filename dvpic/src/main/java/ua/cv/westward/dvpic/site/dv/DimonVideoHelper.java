package ua.cv.westward.dvpic.site.dv;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.List;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;

import javax.net.ssl.HttpsURLConnection;

import ua.cv.westward.dvpic.helper.BaseImageHelper;
import ua.cv.westward.dvpic.site.AbstractImage;
import ua.cv.westward.dvpic.site.SiteParameters;

/**
 * Загрузчик изображений с сайта dimonvideo.ru.
 * @author Vladimir Kuts
 */
public class DimonVideoHelper extends BaseImageHelper {
        
    public DimonVideoHelper( Context context, SiteParameters params ) {
        super( context, params );
    }

    /**
     * Получить список фотографий RSS ленты
     * @throws Exception 
     */
    @Override
    protected List<? extends AbstractImage> getImagesList() throws Exception {
        // настроить соединение, загрузить xml rss ленту
        HttpsURLConnection connection = null;
        try {
            connection = (HttpsURLConnection) openConnection( mSiteParams.getFeedURL() );
            InputStream is = null;
            try {
                is = new BufferedInputStream( connection.getInputStream() );
                // get Simple XML framework parser instance, parse input XML
                Serializer serializer = new Persister();
                DimonVideoRss rss = serializer.read( DimonVideoRss.class, is, false );
                return (List<? extends AbstractImage>)rss.getList();
            } catch( XmlPullParserException e ) {
                // retrow parser exception with more human readable message
                throw new IOException( "XML parser: website feed is not valid" );
            } finally {
                if( is != null ) is.close();
            }
        } finally {
            if( connection != null ) connection.disconnect();
        }        
    }
    
//    private static final char[] tag = { '<', '?', 'x', 'm', 'l', ' ' };
    
//    /**
//     * Verify input stream (must begins with <feed>)
//     * @param is
//     * @return
//     * @throws IOException
//     */
//    private boolean verifyInputStream( InputStream is ) throws IOException {
//        if( is.markSupported() ) {
//            boolean result = false;
//            final char[] buffer = new char[6];
//            // set mark to return back
//            is.mark( 20 );
//            
//            Reader in = new InputStreamReader( is, "UTF-8" );
//            try {
//                int read = in.read( buffer, 0, 6 );
//                if( read > 0 ) {
//                    return Arrays.equals( buffer, tag );
//                }
//            } finally {
////                in.close();
//                // return input stream to the mark set earlier
//                is.reset();
//            }
//            return result;
//        } else {
//            return true;
//        }
//    }
}
