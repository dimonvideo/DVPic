package ua.cv.westward.dvpic.helper.net;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.StringTokenizer;

public class HttpCookie {

    /**
     * Constructs a cookie from a string. The string should comply with
     * set-cookie header format as specified in
     * <a href="http://www.ietf.org/rfc/rfc2965.txt">RFC 2965</a>.
     *
     * @param header a set-cookie header line.
     * @return a constructed cookies
     * @throws IllegalArgumentException
     *             if the string does not comply with cookie specification, or
     *             the cookie name contains illegal characters, or reserved
     *             tokens of cookie specification appears
     * @throws NullPointerException
     *             if header is null
     */
    public static HttpCookie parse( String header ) {
        return new CookieParser( header ).parse();
    }
    
    static class CookieParser {
        private static final String COOKIE_VALUE_DELIMITER = ";";
        private static final char NAME_VALUE_SEPARATOR = '=';
        private static final String DATE_FORMAT = "EEE, dd-MMM-yyyy hh:mm:ss z";
        
        private final DateFormat mDateFormat;
        private final String mInput;

        CookieParser( String header ) {
            mInput = header;
            mDateFormat = new SimpleDateFormat( DATE_FORMAT, Locale.US );
        }
        
        public HttpCookie parse() {
            StringTokenizer st = new StringTokenizer( mInput, COOKIE_VALUE_DELIMITER );

            // the specification dictates that the first name/value pair
            // in the string is the cookie name and value, so let's handle
            // them as a special case:
            
            if( st.hasMoreTokens() == false ) {
                throw new IllegalArgumentException( "No cookies in " + mInput );
            }
            String token  = st.nextToken();
            int cut = token.indexOf( NAME_VALUE_SEPARATOR );
            if( cut <= 0 ) {
                // cookie does not contains a name/value separator
                throw new IllegalArgumentException( "Incorrect cookie name/value pair in " + token );
            }
            HttpCookie cookie = new HttpCookie( token.substring( 0, cut ),
                                                token.substring( cut + 1, token.length() ));
            
            while( st.hasMoreTokens() ) {
                token  = st.nextToken().trim();
                cut = token.indexOf( NAME_VALUE_SEPARATOR );
                if( cut > 0 ) {
                    // store only complete name/value parameters
                    setAttribute( cookie, token.substring( 0, cut ).toLowerCase(),
                            token.substring( cut + 1, token.length()) );
                }
            }
            return cookie;
        }
        
        /**
         * Skip unknown cookie attributes, store correct values 
         */
        private void setAttribute( HttpCookie cookie, String name, String value ) {
//            if( name.equals( "domain" )) {
//                cookie.mDomain = value;
            if( name.equals( "path" )) {
                cookie.mPath = value;
            } else if( name.equals( "expires" )) {
                try {
                    cookie.mExpires = mDateFormat.parse( value );
                } catch( ParseException e ) {
                    // assume that cookie never expires
                    cookie.mExpires = null;
                }
            }
        }
    }
    
    private String mName;
    private String mValue;
//    private String mDomain;
    private String mPath;
    private Date   mExpires;
    
    public HttpCookie( String name, String value ) {
        if( name == null || value == null ) {
            throw new IllegalArgumentException( "Invalid arguments to create a HttpCookie" );
        }
        this.mName = name;
        this.mValue = value;
    }
    
//    /**
//     * Returns the Domain attribute
//     * @return
//     */
//    public String getDomain() {
//        return mDomain;
//    }
    
    /**
     * Returns the expiration date in milliseconds, or -1l if expiration date is
     * not set. If cookie does not have an expiration date, it must be deleted
     * at the end of current session.
     * @return
     */
    public long getExpirationDate() {
        return mExpires != null ? mExpires.getTime() : -1l;
    }
    
    /**
     * Return the expiration date in milliseconds, in the string representation 
     * @return
     */
    public String getExpires() {
        if( mExpires != null ) {
            return String.valueOf( mExpires.getTime() );
        } else {
            return "";
        }
    }
    
    /**
     * Returns the name of this cookie.
     */
    public String getName() {
        return mName;
    }

    /**
     * Returns the Path attribute. This cookie is visible to all subpaths.
     */
    public String getPath() {
        return mPath == null ? "" : mPath;
    }

    /**
     * Returns the value of this cookie.
     */
    public String getValue() {
        return mValue;
    }
        
    /**
     * Returns true if this cookie has expired
     * @return
     */
    public boolean hasExpired() {
        if( mExpires == null ) return false;
        
        Date now = new Date();
        return( now.compareTo( mExpires )) > 0;
    }
    
    /**
     * Set the Path attribute of this cookie
     * @param path
     */
    public void setPath( String path ) {
        mPath = path;
    }
    
    /**
     * Set the expiration date of this cookie
     * @param value
     */
    public void setExpires( String value ) {
        try {        
            mExpires = new Date( Long.parseLong( value ));
        } catch( NumberFormatException e ) {            
            // assume that cookie never expires
            mExpires = null;
        }
    }
    
//    /**
//     * Set the Domain attribute of this cookie. HTTP clients send
//     * cookies only to matching domains.
//     */
//    public void setDomain( String pattern ) {
//        mDomain = pattern == null ? null : pattern.toLowerCase( Locale.US );
//    }
}
