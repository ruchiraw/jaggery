package org.jaggeryjs.core;

public interface JaggeryReader {
    public JaggeryFile getFile(String scriptId) throws JaggeryException;
}
