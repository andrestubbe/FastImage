package fastimage;

/**
 * FastImageException - Dedicated unchecked exception for FastImage native imaging errors,
 * memory bounds violations, and initialization failures.
 */
public class FastImageException extends RuntimeException {

    /**
     * Constructs a new FastImageException with the specified error message.
     *
     * @param message Detailed description of the error cause.
     */
    public FastImageException(String message) {
        super(message);
    }

    /**
     * Constructs a new FastImageException with the specified error message and underlying cause.
     *
     * @param message Detailed description of the error cause.
     * @param cause The underlying throwable cause.
     */
    public FastImageException(String message, Throwable cause) {
        super(message, cause);
    }
}

