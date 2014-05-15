package org.jaggeryjs.core;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.net.URL;

public class JaggeryDiskFile implements JaggeryFile {

    private String id;
    private File file;
    private FileReader reader;

    public JaggeryDiskFile(String id) throws JaggeryException {
        this.id = id;
        try {
            this.file = FileUtils.toFile(new URL(id));
            this.reader = new FileReader(this.file);
        } catch (Exception e) {
            throw new JaggeryException(e.getMessage(), e);
        }
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public boolean isExists() {
        return file.exists();
    }

    @Override
    public Reader getReader() {
        return this.reader;
    }
}
