package at.runner;

/**
 * An enumeration of the different interval weights that can be used.
 */
public enum IntervalWeight {

    /**
     * An interval of heavy indicates watching videos as fast as possible with no sleeping and
     * fast forwarding through the app trailers (not ads).
     */
    Heavy,

    /**
     * An interval of light indicates watching videos as slow as possible with watching the
     * app trailers (not ads) and sleeping between the app trailers.
     */
    Light,

    /**
     * An interval of medium indicates watching videos with watching the app trailers (not ads) but
     * not sleeping between the app trailers.
     */
    Medium
}