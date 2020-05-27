package ua.cv.westward.dvpic.helper.retrier;

import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;

/**
 * This class allows repeat Callable task several times and make delay between
 * retries. 
 * @author Vladimir Kuts
 */
public class RetriableTask<T> implements Callable<T> {

    public static final int DEFAULT_RETRIES = 5;
    public static final long DEFAULT_TIMEOUT = 1000;
        
    private Callable<T> task;
    
    private int retries;
    private long timeout;
    
    public RetriableTask( Callable<T> task ) {
        this( DEFAULT_RETRIES, DEFAULT_TIMEOUT, task );
    }
    
    public RetriableTask( int numberOfRetries, long timeToWait, Callable<T> task ) {
        this.retries = numberOfRetries;
        this.timeout = timeToWait;
        this.task = task;
    }
    
    @Override    
    public T call() throws Exception {
        int retriesLeft = retries;
        while( true ) {
            try {
                return task.call();
            } catch( InterruptedException e ) {
                throw e;
            } catch( CancellationException e ) {
                throw e;
            } catch( RuntimeException e ) {
                throw e;
            } catch( Exception e ) {
                if( --retriesLeft == 0 ) {
                    throw e;
                }
                Thread.sleep( timeout );
            }
        }
    }
}
