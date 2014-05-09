package org.jaggeryjs.core;

import java.io.FileReader;
import java.io.IOException;

public class JaggeryFileReader implements JaggeryReader {
    @Override
    public JaggeryScript getScript(String scriptId) throws IOException {
        return new JaggeryScript(scriptId, new FileReader(scriptId));
    }
}
