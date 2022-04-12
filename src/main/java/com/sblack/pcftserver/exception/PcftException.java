package com.sblack.pcftserver.exception;

public class PcftException extends Exception {
    public PcftException(String s, Exception e) {
        super(s, e);
    }

    public PcftException(String s) {
        super(s);
    }
}
