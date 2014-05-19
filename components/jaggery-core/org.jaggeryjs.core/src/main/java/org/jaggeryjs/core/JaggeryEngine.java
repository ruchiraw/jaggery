package org.jaggeryjs.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.script.Bindings;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;
import java.util.Map;

public class JaggeryEngine {

    private static final Log log = LogFactory.getLog(JaggeryEngine.class);

    private static final String EXEC_CALLBACK_KEY = "exec";

    private static final String ENGINE_KEY = "engine";

    private static final String HOME_KEY = "home";

    private static final String JAGGERY_KEY = "jaggery";

    private Invocable invocable;

    private static ScriptEngineManager manager = new ScriptEngineManager();

    public JaggeryEngine(String jaggeryHome, Map<String, Object> globals, JaggeryFile initializer)
            throws JaggeryException {
        ScriptEngine engine = manager.getEngineByName("js");
        this.invocable = (Invocable) engine;

        Bindings bindings = new SimpleBindings();
        bindings.put(HOME_KEY, jaggeryHome);
        bindings.put(ENGINE_KEY, engine);
        for (Map.Entry<String, Object> entry : globals.entrySet()) {
            bindings.put(entry.getKey(), entry.getValue());
        }
        engine.put(JAGGERY_KEY, bindings);

        String oldScriptId = (String) engine.get(ScriptEngine.FILENAME);
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
}
