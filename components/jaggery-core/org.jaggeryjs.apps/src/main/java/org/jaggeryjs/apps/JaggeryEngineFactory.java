package org.jaggeryjs.apps;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.jaggeryjs.core.JaggeryEngine;
import org.jaggeryjs.core.JaggeryException;
import org.jaggeryjs.core.JaggeryReader;
import org.jaggeryjs.core.JaggeryScript;

import javax.servlet.ServletContext;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class JaggeryEngineFactory extends BasePooledObjectFactory<JaggeryEngine> {

    private ServletContext servletContext;

    private static final String READER_KEY = "reader";

    public JaggeryEngineFactory(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    @Override
    public JaggeryEngine create() throws JaggeryException {
        Map<String, Object> globals = new HashMap<String, Object>();
        final JaggeryAppConfigs appConfigs = JaggeryAppConfigs.getInstance(servletContext);
        JaggeryReader reader = new JaggeryReader() {
            @Override
            public JaggeryScript getScript(String scriptId) throws IOException {
                InputStream in = servletContext.getResourceAsStream(scriptId);
                if (in == null) {
                    throw new IOException("script " + scriptId + " cannot be found at " + appConfigs.getContextPath());
                }
                return new JaggeryScript("apps:/" + appConfigs.getContextPath() + scriptId, new InputStreamReader(in));
            }
        };
        globals.put(READER_KEY, reader);
        globals.put(JaggeryConstants.CONTEXT_OBJECT, servletContext);
        return new JaggeryEngine(globals, appConfigs.getInitializer());
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