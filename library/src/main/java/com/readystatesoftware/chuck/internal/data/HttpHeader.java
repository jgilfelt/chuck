package com.readystatesoftware.chuck.internal.data;

public class HttpHeader {

    private String name;
    private String value;

    HttpHeader(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }
}
