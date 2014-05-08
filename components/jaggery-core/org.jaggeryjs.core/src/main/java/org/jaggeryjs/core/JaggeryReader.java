package org.jaggeryjs.core;

import java.io.IOException;
import java.io.Reader;

public interface JaggeryReader {
    public Reader getReader(String scriptId) throws IOException;
}
