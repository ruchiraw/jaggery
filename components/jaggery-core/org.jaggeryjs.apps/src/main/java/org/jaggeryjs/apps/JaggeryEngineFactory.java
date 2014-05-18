package org.jaggeryjs.apps;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.jaggeryjs.core.JaggeryEngine;
import org.jaggeryjs.core.JaggeryException;

import javax.servlet.ServletContext;
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
        globals.put(JaggeryConstants.CONTEXT_OBJECT, servletContext);
        return new JaggeryEngine(appConfigs.getJaggeryHome(), globals, appConfigs.getInitializer());
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