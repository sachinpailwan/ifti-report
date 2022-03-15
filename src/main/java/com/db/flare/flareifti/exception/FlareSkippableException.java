package com.db.flare.flareifti.exception;

public class FlareSkippableException extends Exception {

    public FlareSkippableException() {
        super();
    }

    public FlareSkippableException(String message) {
        super(message);
    }

    public FlareSkippableException(String message, Throwable t) {
        super(message, t);
    }
}
