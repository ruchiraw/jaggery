package org.jaggeryjs.core;

public class JaggeryException extends Exception {

    public JaggeryException(String message, Throwable cause) {
        super(message, cause);
    }

    public JaggeryException(String message) {
        super(message);
    }

    public JaggeryException(Throwable cause) {
        super(cause);
    }
}
