package ua.cv.westward.dvpic.helper.net;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * HTTP Helper: download HTTP URLs to a string or file. 
 * Exceptions are not handled and passed to a higher level.
 * 
 * Parameters:
 * Connection timeout: 20 seconds.
 * Reading data timeout: 20 seconds.
 * Used system User-Agent.
 * Stream encoding: UTF-8.
 * 
 */
public class HttpHelper {

    private static final int CONNECT_TIMEOUT = 10 * 1000;
    private static final int READ_TIMEOUT = 10 * 1000;
    private static final String DATA_ENCODING = "UTF-8";

    private static HttpHelper mInstance = null;
    private CookieManager mCookieManager;

    protected HttpHelper( Context context ) {        
        mCookieManager = new CookieManager( context.getApplicationContext() );
    }
    
    public static synchronized HttpHelper getInstance( Context context ) {
        if( mInstance == null ) {
            mInstance = new HttpHelper( context );
        }
        return mInstance;
    }
    
    /**
     * Create new http connection, check response, return OPENED connection.
     * Caller is responsible to close the opened connection.
     */
    public HttpURLConnection openConnection( String url ) throws IOException {

        HttpURLConnection connection = newConnection( url );
        checkResponse( connection );
        return connection;        
    }
    
    /**
     * Create the new http connection.
     */
    @SuppressWarnings("deprecation")
	public HttpURLConnection newConnection( String url ) throws IOException {
        URL addr = new URL( url );
        HttpURLConnection connection = (HttpURLConnection) addr.openConnection();
        connection.setConnectTimeout( CONNECT_TIMEOUT );
        connection.setReadTimeout( READ_TIMEOUT );
        connection.setInstanceFollowRedirects( true );
        connection.setRequestProperty("Connection", "Keep-Alive");
        connection.setRequestProperty( "User-Agent", "Mozilla/5.0 (Windows NT 6.1; rv:16.0) Gecko/20100101 Firefox/16.0" );
        String referer = addr.getProtocol() + "://" + addr.getHost() + "/";
        connection.setRequestProperty( "Referer", referer );
        // HTTP connection reuse which was buggy pre-froyo
        if (Integer.parseInt( Build.VERSION.SDK ) < Build.VERSION_CODES.FROYO ) {
            System.setProperty( "http.keepAlive", "false" );
        }
        // set cookies manager (connection must not have been opened here)
        if( mCookieManager != null ) {
            mCookieManager.setCookies( connection ); 
        }
        return connection;        
    }
    
    /**
     * Check server response. If responce is not '200 OK', close connection 
     * and throw an exception.
     */
    public void checkResponse( HttpURLConnection connection ) throws IOException {
        int responseCode = connection.getResponseCode();
        if( responseCode != HttpURLConnection.HTTP_OK ) {
            connection.disconnect();
            throw new IOException( "HTTP server response: " +
                                   connection.getResponseMessage() );
        }
        // get cookies from response header 
        mCookieManager.getCookies( connection );        
    }
    
    /**
     * Retrieve HTTP URL as string.
     */
    public String getString( String url ) throws IOException {
        // 
        HttpURLConnection connection = openConnection( url );        
        try {
            BufferedReader reader = null;
            try {                
                InputStream is = connection.getInputStream();
                // use buffered line-by-line reading
                reader = new BufferedReader( new InputStreamReader( is, DATA_ENCODING ));   
                String temp = null;
                StringBuilder sb = new StringBuilder();            
                while( (temp = reader.readLine()) != null ) {
                    sb.append( temp );
                }
                return sb.toString();                
            } finally {
                if( reader != null )  reader.close();
            }
        } finally {
            connection.disconnect();
        }
    }

    /**
     * Retrieve HTTP URL to a file.
     */
    public void getFile( String url, File file ) throws IOException {
        // настроить соединение
        HttpURLConnection connection = openConnection( url );

        try (InputStream is = connection.getInputStream(); FileOutputStream fos = new FileOutputStream(file)) {

            byte[] buffer = new byte[4096];
            int len1 = 0;
            while ((len1 = is.read(buffer)) > 0) {
                fos.write(buffer, 0, len1);
            }
        } finally {
            // release resources
            connection.disconnect();
        }
    }
        
    /**
     * Return information about active data connection
     */
    public static NetworkInfo getNetworkInfo( Context context ) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService( Context.CONNECTIVITY_SERVICE );
        assert cm != null;
        NetworkInfo netinfo = cm.getActiveNetworkInfo();
        if( netinfo != null && netinfo.isConnected() ) {
            return netinfo;
        }
        return null;
    }
        
    /**
     * Check if android system preference 'background data connection' is enabled
     */
    public static boolean isBackgroundDataEnabled( Context context ) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService( Context.CONNECTIVITY_SERVICE );
        assert cm != null;
        NetworkInfo info = cm.getActiveNetworkInfo();
        return info != null && info.getState() == NetworkInfo.State.CONNECTED;
    }    
    
    /**
     * Check if we have a data connection
     */
    public static boolean isConnected( Context context ) {
        NetworkInfo ni = getNetworkInfo( context );
        return ni != null && ni.isConnected();
    }
}
