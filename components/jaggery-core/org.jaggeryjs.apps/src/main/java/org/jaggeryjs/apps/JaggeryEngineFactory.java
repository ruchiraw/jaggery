package org.jaggeryjs.apps;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.jaggeryjs.core.JaggeryEngine;
import org.jaggeryjs.core.JaggeryException;
import org.jaggeryjs.core.JaggeryFile;
import org.jaggeryjs.core.JaggeryReader;

import javax.servlet.ServletContext;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

public class JaggeryEngineFactory extends BasePooledObjectFactory<JaggeryEngine> {

    private ServletContext servletContext;

    public JaggeryEngineFactory(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    @Override
    public JaggeryEngine create() throws JaggeryException {
        Map<String, Object> globals = new HashMap<String, Object>();
        final JaggeryAppConfigs appConfigs = JaggeryAppConfigs.getInstance(servletContext);
        JaggeryReader reader = new JaggeryReader() {
            @Override
            public JaggeryFile getFile(final String path) throws JaggeryException {
                final String id = "apps:/" + appConfigs.getContextPath() + path;
                final InputStream in = servletContext.getResourceAsStream(path);
                return new JaggeryFile() {
                    @Override
                    public String getId() {
                        return id;
                    }

                    @Override
                    public boolean isExists() {
                        return in != null;
                    }

                    @Override
                    public Reader getReader() {
                        return new InputStreamReader(in);
                    }
                };
            }
        };
        globals.put(JaggeryConstants.CONTEXT_OBJECT, servletContext);
        return new JaggeryEngine(globals, appConfigs.getInitializer(), reader);
    }

    @Override
    public PooledObject<JaggeryEngine> wrap(JaggeryEngine engine) {
        return new DefaultPooledObject<JaggeryEngine>(engine);
    }

    @Override
    public void passivateObject(PooledObject<JaggeryEngine> pooledObject) {
        //pooledObject.getObject();
    }

}