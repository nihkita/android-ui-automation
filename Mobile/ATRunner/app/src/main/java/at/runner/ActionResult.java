package at.runner;

/**
 * An enumeration of the different results of executing some action.
 */
public enum ActionResult {

    /**
     * When an exception was encountered.
     */
    Exception,

    /**
     * When something was found and clicked.
     */
    Found,

    /**
     * When something was not clickable.
     */
    NotClickable,

    /**
     * When something was not found.
     */
    NotFound,

    /**
     * When something was not found after searching.
     */
    NotFoundAfterSearching,

    /**
     * When something was not able to be processed.
     */
    NotProcessed,

    /**
     * When something was successfully processed.
     */
    Processed
}
