package com.tfc.pdf.pdfdancer.api.common.util;

public class ExceptionUtils {
    public static RuntimeException wrapCheckedException(Exception e) {
        if (e instanceof RuntimeException) return (RuntimeException) e;
        else return new RuntimeException(e);
    }
}
