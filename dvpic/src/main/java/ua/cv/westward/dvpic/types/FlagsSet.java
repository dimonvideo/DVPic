package ua.cv.westward.dvpic.types;

/**
 * Convenience class to manipulate with bitwise flags, that packed into the
 * single integer value.
 *
 * @author Vladimir Kuts
*/
public class FlagsSet {

    private int mValues;

    /* */

    public FlagsSet() {}

    public FlagsSet( int value ) {
        setValue( value );
    }

    /* */

    public void append( int value ) {
        mValues |= value;
    }

    /**
     * Reset all switches.
     */
    public void clear() {
        mValues = 0;
    }

    /**
     * Returns all switches packed in the single integer value.
     * @return
     */
    public int getValue() {
        return mValues;
    }

    public boolean isAnySet() {
        return mValues > 0;
    }

    public boolean isSet( int mask ) {
        return (mValues & mask) > 0;
    }

    public void reset( int mask ) {
        mValues &= ~mask;
    }

    public void set( int mask ) {
        mValues |= mask;
    }

    public void set( int mask, boolean value ) {
        if( value ) {
            mValues |= mask;
        } else {
            mValues &= ~mask;
        }
    }

    public void setValue( int value ) {
        mValues = value;
    }
}