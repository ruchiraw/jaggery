package org.jaggeryjs.core;

import java.io.Reader;

public class JaggeryScript {

    private String id;
    private Reader reader;

    public JaggeryScript(String id, Reader reader) {
        this.id = id;
        this.reader = reader;
    }

    public String getId() {
        return id;
    }

    public Reader getReader() {
        return reader;
    }
}
