package org.jaggeryjs.core;

import org.jaggeryjs.core.JaggeryReader;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

public class JaggeryFileReader implements JaggeryReader {
    @Override
    public Reader getReader(String scriptId) throws IOException {
        return new FileReader(scriptId);
    }
}
