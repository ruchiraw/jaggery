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

    private static final String READER_KEY = "reader";

    private static final String JAGGERY_KEY = "jaggery";

    private Invocable invocable;

    private JaggeryReader reader;

    private static ScriptEngineManager manager = new ScriptEngineManager();

    public JaggeryEngine(Map<String, Object> globals, JaggeryFile initializer, JaggeryReader reader)
            throws JaggeryException {
        ScriptEngine engine = manager.getEngineByName("js");
        //System.out.println(engine.getFactory().getEngineName());
        this.invocable = (Invocable) engine;
        this.reader = reader;

        Bindings bindings = new SimpleBindings();
        for (Map.Entry<String, Object> entry : globals.entrySet()) {
            bindings.put(entry.getKey(), entry.getValue());
        }
        bindings.put(ENGINE_KEY, engine);
        bindings.put(READER_KEY, this.reader);
        engine.put(JAGGERY_KEY, bindings);

        String oldScriptId = (String) engine.get(ScriptEngine.FILENAME);
        engine.put(ScriptEngine.FILENAME, initializer.getId());

        try {
            engine.eval(initializer.getReader());
        } catch (Exception e) {
            throw new JaggeryException("Error initializing JaggeryEngine using : " + initializer, e);
        } finally {
            engine.put(ScriptEngine.FILENAME, oldScriptId);
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
