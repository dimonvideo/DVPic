package ua.cv.westward.dvpic.helper.net;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import android.content.Context;
import android.util.Xml;

/**
 * CookieManager is a simple utilty based to Ian Brown code for handling cookies
 * when working with java.net.URL and java.net.URLConnection objects.
 * 
 * This implementation handles cookies for the single domain.
 * 
 *     CookieManager cm = new CookieManager( context, domain );
 *     URL url = new URL("http://www.hccp.org/test/cookieTest.jsp");
 *      . . . 
 *     // getting cookies:
 *     URLConnection conn = url.openConnection();
 *     conn.connect();
 *     cm.gatCookies( conn );
 *
 *     // setting cookies
 *     cm.put(conn);
 *     cm.setCookies(url.openConnection());
 *     
 *     @author Vlad Kuts
 **/
public class CookieManager {
    
    private static final String SET_COOKIE = "Set-Cookie";
    private static final String SET_COOKIE_SEPARATOR="; ";
    private static final String COOKIE = "Cookie";
    private static final char   DOT = '.';
    
//    private static final String COOKIE_FILE_PREFIX = "@";
    
    private static final String XML_ROOT_TAG     = "cookies";
    private static final String XML_COOKIE_TAG   = "cookie";
    private static final String XML_NAME_ATTR    = "name";
    private static final String XML_VALUE_ATTR   = "value";
    private static final String XML_PATH_ATTR    = "path";
    private static final String XML_EXPIRES_ATTR = "expires";
    
    private final Context mContext;
    private final Map<String,HttpCookie> mDomainStore;
    private String mDomain;
        
    public CookieManager( Context context ) {
        mContext = context.getApplicationContext();
        mDomainStore = new HashMap<String,HttpCookie>();
//        mDomain = COOKIE_FILE_PREFIX + domain;
        
//        try {
//            readCookies();
//        } catch( Exception e ) {
//            // cookie file may not exists
//        }
    }
    
    /**
     * Retrieves and stores cookies returned by the host on the other side
     * of the the open java.net.URLConnection.
     *
     * The connection MUST have been opened using the connect()
     * method or a IOException will be thrown.
     *
     * @param conn a java.net.URLConnection - must be open, or IOException will be thrown
     * @throws java.io.IOException Thrown if conn is not open.
     */
    public void getCookies( URLConnection conn ) throws IOException {    
        if( conn == null ) {
            throw new IllegalArgumentException( "Connection is not open" );
        }
        
        // let's determine the domain from where these cookies are being sent
        String domain = getDomainFromHost( conn.getURL().getHost() );
        if( !domain.equals( mDomain )) {
            // retune cookies store to the new domain
            mDomain = domain;
            mDomainStore.clear();
        }
              
        // ok, now we are ready to get the cookies out of the URLConnection    
//        boolean hasNewCookies = false;        
        String headerName = null;
        for( int i = 1; (headerName = conn.getHeaderFieldKey(i)) != null; i++ ) {
//            String value = conn.getHeaderField( i );
            if( headerName.equalsIgnoreCase( SET_COOKIE )) {                
                HttpCookie cookie = HttpCookie.parse( conn.getHeaderField( i ));
//                if( Utils.isEmpty( cookie.getDomain() )) {
//                    cookie.setDomain( domain );
//                }
                mDomainStore.put( cookie.getName(), cookie );
//                // filter only important cookies                
//                if( cookie.getName().equals( "dv_user_id" ) || 
//                    cookie.getName().equals( "dv_password" )) {
//                    if( !hasNewCookies ) {
//                        // need to store new cookies: clear old & set flag
//                        // to prevent clearing storage in the next iterations 
//                        hasNewCookies = true;
//                        mDomainStore.clear();
//                    }
//                    mDomainStore.add( cookie );
//                }
            }
        }
        
//        // write cookies to the xml file named '@domain', create file if it
//        // doesn't exists, overwrite existing file
//        if( hasNewCookies ) {
//            writeCookies( toXml() );
//        }
    }
    
    /**
     * Prior to opening a URLConnection, calling this method will set all
     * unexpired cookies that match the path or subpaths for thi underlying URL
     *
     * The connection MUST NOT have been opened 
     * method or an IOException will be thrown.
     *
     * @param conn a java.net.URLConnection - must NOT be open, or IOException will be thrown
     * @throws java.io.IOException Thrown if conn has already been opened.
     */
    public void setCookies( URLConnection conn ) throws IOException {
    
        // let's determine the domain and path to retrieve the appropriate cookies
        URL url = conn.getURL();
        String domain = getDomainFromHost( url.getHost() );

        if( !domain.equals( mDomain )) return;
        if( mDomainStore.size() == 0 ) return;
        
        String path = url.getPath();
        StringBuilder sb = new StringBuilder();
        for( HttpCookie cookie : mDomainStore.values() ) {
            // check cookie to ensure path matches and cookie is not expired
            // if all is cool, add cookie to header string
            if( comparePaths( cookie.getPath(), path ) && !cookie.hasExpired() ) {
                if( sb.length() > 0 ) {
                    sb.append( SET_COOKIE_SEPARATOR );
                }
                sb.append( cookie.getName() );
                sb.append( '=' );
                sb.append( cookie.getValue() );
            }
        }        
        
        try {
            conn.setRequestProperty( COOKIE, sb.toString() );
        } catch( java.lang.IllegalStateException ise ) {
            IOException ioe = new IOException( "Illegal State! Cookies cannot be set on a URLConnection that is already connected. " ); 
            throw ioe;
        }
    }

    /**
     * Delete existing cookies
     */
    public void deleteCookies() {
        mDomainStore.clear();
//        File f = mContext.getFileStreamPath( mDomain );
//        f.delete();
    }
    
    private String getDomainFromHost( String host ) {
        if( host.indexOf( DOT ) != host.lastIndexOf( DOT )) {
            return host.substring( host.indexOf( DOT ) + 1 );
        } else {
            return host;
        }
    }

    private boolean comparePaths( String cookiePath, String targetPath ) {
        if( cookiePath == null ) {
            return true;
        } else if( cookiePath.equals( "/" )) {
            return true;
        } else if( targetPath.regionMatches( 0, cookiePath, 0, cookiePath.length() )) {
            return true;
        } else {
            return false;
        }    
    }
    
    /**
     * Returns a string representation of stored cookies organized by domain.
     */
    public String toString() {
        return mDomainStore.toString();
    }
    
    /* Cookies XML reading and parsing */
    
    @SuppressWarnings("unused")
    private void readCookies()
            throws IllegalArgumentException, XmlPullParserException, IOException {
        FileInputStream fis = mContext.openFileInput( mDomain );
        InputStream is = new BufferedInputStream( fis, 1024 );
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser xpp = factory.newPullParser();            
            
            xpp.setInput( is, "UTF-8" );
            int eventType = xpp.getEventType();
            while( eventType != XmlPullParser.END_DOCUMENT ) {
                if( eventType == XmlPullParser.START_TAG ) {
                    if( xpp.getName().equals( XML_COOKIE_TAG )) {
                        HttpCookie cookie = new HttpCookie( xpp.getAttributeValue( null, XML_NAME_ATTR ),
                                                            xpp.getAttributeValue( null, XML_VALUE_ATTR ) );
                        cookie.setPath( xpp.getAttributeValue( null, XML_PATH_ATTR ));
                        cookie.setExpires( xpp.getAttributeValue( null, XML_EXPIRES_ATTR ));
                        // add cookie to the list
                        mDomainStore.put( cookie.getName(), cookie );
                    }
                }
                eventType = xpp.next();
            }
        } finally {
            is.close();
        }
    }
    
    /* Cookies XML serializing and writing */
    
    /**
     * Serialize cookies to the XML string
     * @return cookies xml representation
     * @throws IOException
     */
    @SuppressWarnings("unused")
    private String toXml() throws IOException {
        XmlSerializer serializer = Xml.newSerializer();
        StringWriter writer = new StringWriter();
        try {
            serializer.setOutput( writer );
            serializer.startDocument( "UTF-8", true );
            serializer.text( "\n" );
            serializer.startTag( null, XML_ROOT_TAG );
            serializer.text( "\n" );
            
            for( HttpCookie cookie : mDomainStore.values() ) {
                serializer.startTag( null, XML_COOKIE_TAG );
                
                serializer.attribute( null, XML_NAME_ATTR, cookie.getName() );
                serializer.attribute( null, XML_VALUE_ATTR, cookie.getValue() );
                serializer.attribute( null, XML_PATH_ATTR, cookie.getPath() );
                serializer.attribute( null, XML_EXPIRES_ATTR, cookie.getExpires() );                                
                
                serializer.endTag( null, XML_COOKIE_TAG );
                serializer.text( "\n" );
            }
            
            serializer.endTag( null, XML_ROOT_TAG );
            serializer.endDocument();
            return writer.toString();
        } catch( Exception e ) {
            throw new IOException( e.getMessage() );
        }
    }
    
    /**
     * Write cookies, serialized to xml string, to file. Use domain as file name.
     * @param xml cookies, serialised to the xml string 
     * @throws IOException
     */
    @SuppressWarnings("unused")
    private void writeCookies( String xml ) throws IOException {
        FileOutputStream fos = mContext.openFileOutput( mDomain, Context.MODE_PRIVATE );
        OutputStream os = new BufferedOutputStream( fos, 1024 );
        try {
            os.write( xml.getBytes() );
        } finally {
            os.close();
        }
    }
}
