package ua.cv.westward.dvpic.helper.retrier;

public class MaxRetriesException extends Exception {

    private static final long serialVersionUID = -555064559584485806L;

    public MaxRetriesException( final String message ) {
        super( message );
    }
}
