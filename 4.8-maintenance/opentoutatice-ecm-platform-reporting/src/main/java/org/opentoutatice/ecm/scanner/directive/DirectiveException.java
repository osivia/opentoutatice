/**
 * 
 */
package org.opentoutatice.ecm.scanner.directive;


/**
 * @author david
 *
 */
public class DirectiveException extends Exception {

    private static final long serialVersionUID = -1981453832980876042L;

    /**
     * Default constructor.
     */
    public DirectiveException() {
        super();
    }

    /**
     * @param message
     */
    public DirectiveException(String message) {
        super(message);
    }

    /**
     * @param cause
     */
    public DirectiveException(Throwable cause) {
        super(cause);
    }

    /**
     * @param message
     * @param cause
     */
    public DirectiveException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param message
     * @param cause
     * @param enableSuppression
     * @param writableStackTrace
     */
    public DirectiveException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
