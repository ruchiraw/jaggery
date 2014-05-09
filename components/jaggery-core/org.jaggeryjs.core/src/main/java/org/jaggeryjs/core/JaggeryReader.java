package org.jaggeryjs.core;

import java.io.IOException;

public interface JaggeryReader {
    public JaggeryScript getScript(String scriptId) throws IOException;
}
