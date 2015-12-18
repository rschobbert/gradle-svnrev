package de.emesit.gradle.svnrev;

/**
 * @author Ruediger Schobbert, emesit GmbH & Co. KG
 */
public class SvnRevException extends RuntimeException {
	/** 
	 * Constructor with the specified parameters.
     */
    public SvnRevException() {
    }

    /**
     * Constructor with the specified parameters.
     * @param message
     */
    public SvnRevException(String message) {
        super(message);
    }

    /** 
     * Constructor with the specified parameters.
     * @param cause
     */
    public SvnRevException(Throwable cause) {
        super(cause);
    }

    /** 
     * Constructor with the specified parameters.
     * @param message
     * @param cause
     */
    public SvnRevException(String message, Throwable cause) {
        super(message, cause);
    }
}
