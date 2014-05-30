package org.jaggeryjs.core;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;

public class JaggeryDiskFile implements JaggeryFile {

    private String id;
    private File file;
    private FileReader reader;

    public JaggeryDiskFile(String id) throws JaggeryException {
        this.id = id;
        try {
            this.file = new File(id);
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
