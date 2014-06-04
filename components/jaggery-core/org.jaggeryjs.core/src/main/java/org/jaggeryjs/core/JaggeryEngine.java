package org.jaggeryjs.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.script.*;
import java.util.Map;

public class JaggeryEngine {

    private static final Log log = LogFactory.getLog(JaggeryEngine.class);

    private static final String EXEC_CALLBACK_KEY = "exec";

    private static final String NAME_KEY = "name";

    private static final String ENGINE_KEY = "engine";

    private static final String HOME_KEY = "home";

    private static final String JAGGERY_KEY = "jaggery";

    private static final String SCRIPT_ENGINE_NAME = "jaggery";

    private static final String SCRIPT_ENGINE_FACTORY = "javax.script.ScriptEngineFactory";

    private Invocable invocable;

    private static ScriptEngineManager manager = new ScriptEngineManager();

    static {
        initEngineFactory(manager);
    }

    public JaggeryEngine(String name, String jaggeryHome, Map<String, Object> globals, JaggeryFile initializer)
            throws JaggeryException {
        ScriptEngine engine = manager.getEngineByName(SCRIPT_ENGINE_NAME);
        this.invocable = (Invocable) engine;

        Bindings bindings = new SimpleBindings();
        bindings.put(NAME_KEY, name);
        bindings.put(HOME_KEY, jaggeryHome);
        bindings.put(ENGINE_KEY, engine);
        for (Map.Entry<String, Object> entry : globals.entrySet()) {
            bindings.put(entry.getKey(), entry.getValue());
        }
        engine.put(JAGGERY_KEY, bindings);

        engine.put(ScriptEngine.FILENAME, initializer.getId());

        try {
            engine.eval(initializer.getReader());
        } catch (Exception e) {
            throw new JaggeryException("Error initializing JaggeryEngine using : " + initializer, e);
        }
    }

    public Object exec(Map<String, Object> options) throws JaggeryException {
        Bindings object = new SimpleBindings();
        for (Map.Entry<String, Object> entry : options.entrySet()) {
            object.put(entry.getKey(), entry.getValue());
        }
        try {
            return this.invocable.invokeFunction(EXEC_CALLBACK_KEY, object);
        } catch (ScriptException e) {
            throw new JaggeryException("Error executing exec callback of JaggeryEngine", e);
        } catch (NoSuchMethodException e) {
            throw new JaggeryException("\"exec\" callback cannot be found in the JaggeryEngine", e);
        }
    }

    private static void initEngineFactory(ScriptEngineManager manager) {
        String clazz = System.getProperty(SCRIPT_ENGINE_FACTORY);
        if (clazz == null) {
            manager.registerEngineName(SCRIPT_ENGINE_NAME, manager.getEngineByName("js").getFactory());
            return;
        }
        try {
            Class cl = Class.forName(clazz);
            ScriptEngineFactory factory = (ScriptEngineFactory) cl.newInstance();
            manager.registerEngineName(SCRIPT_ENGINE_NAME, factory);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
