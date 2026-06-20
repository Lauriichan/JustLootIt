package me.lauriichan.spigot.justlootit.util;

import java.util.Objects;

public class JLIInitializationException extends Exception {

    public static enum ErrorType {

        TOO_OLD_SERVER_VERSION,
        UNDETECTABLE_SERVER_VERSION,
        INVALID_PLATFORM_VERSION_COMBINATION;

    }

    private static final long serialVersionUID = 1L;

    private final ErrorType errorType;

    public JLIInitializationException(ErrorType errorType) {
        this.errorType = Objects.requireNonNull(errorType);
    }

    public JLIInitializationException(String message) {
        super(message);
        errorType = null;
    }

    public JLIInitializationException(String message, Throwable cause) {
        super(message, cause);
        errorType = null;
    }

    public ErrorType errorType() {
        return errorType;
    }

}
