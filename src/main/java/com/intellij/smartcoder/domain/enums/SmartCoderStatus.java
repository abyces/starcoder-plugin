package com.intellij.smartcoder.domain.enums;

import java.util.stream.Stream;

public enum SmartCoderStatus {
    UNKNOWN(0,"Unknown"),
    OK(200,"OK"),
    BAD_REQUEST(400,"Bad request/token"),
    NOT_FOUND(404,"404 Not found"),
    TOO_MANY_REQUESTS(429,"Too many requests right now"),
    BAD_GATEWAY(502, "Bad gateway"),
    UNAVAILABLE(503,"Service unavailable");

    private int code;
    private String displayValue;

    SmartCoderStatus(int i, String s) {
        code = i;
        displayValue = s;
    }

    public int getCode() {
        return code;
    }

    public String getDisplayValue() {
        return displayValue;
    }

    public static SmartCoderStatus getStatusByCode(int code) {
        return Stream.of(SmartCoderStatus.values())
                .filter(s -> s.getCode() == code)
                .findFirst()
                .orElse(SmartCoderStatus.UNKNOWN);
    }
}
