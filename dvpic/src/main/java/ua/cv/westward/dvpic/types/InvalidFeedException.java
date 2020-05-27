package ua.cv.westward.dvpic.types;

public class InvalidFeedException extends Exception {

    private static final long serialVersionUID = -8158051442496729353L;

    public InvalidFeedException( String msg ) {
        super( msg );
    }
}
