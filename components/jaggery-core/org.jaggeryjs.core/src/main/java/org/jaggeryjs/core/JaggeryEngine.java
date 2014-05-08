package org.jaggeryjs.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.script.*;
import java.io.Reader;

public class JaggeryEngine {

    private static final Log log = LogFactory.getLog(JaggeryEngine.class);

    private static final String EXEC_CALLBACK = "exec";

    private static final String READER = "reader";

    private String name;

    private Invocable invocable;

    private static ScriptEngineManager manager = new ScriptEngineManager();

    public JaggeryEngine(String name, Reader initializer, JaggeryReader reader) throws JaggeryException {
        ScriptEngine engine = manager.getEngineByName("js");
        try {
            //System.out.println(engine.getFactory().getEngineName());
            this.name = name;
            this.invocable = (Invocable) engine;
            engine.put(READER, reader);
            engine.eval(initializer);
        } catch (Exception e) {
            String error = "Error initializing JaggeryEngine " + this.name + " using : " + initializer;
            //log.info(error, e);
            throw new JaggeryException(error, e);
        }
    }

    public void exec(Bindings bindings) throws JaggeryException {
        try {
            this.invocable.invokeFunction(EXEC_CALLBACK, bindings);
        } catch (ScriptException e) {
            String error = "Error executing exec callback of JaggeryEngine " + this.name;
            //log.error(error, e);
            throw new JaggeryException(error, e);
        } catch (NoSuchMethodException e) {
            String error = "\"exec\" callback cannot be found in the JaggeryEngine " + this.name;
            //log.error(error, e);
            throw new JaggeryException(error, e);
        }
    }
}
