package org.jaggeryjs.core;

import java.io.Reader;

public interface JaggeryFile {

    public String getId();

    public boolean isExists();

    public Reader getReader() throws JaggeryException;
}
