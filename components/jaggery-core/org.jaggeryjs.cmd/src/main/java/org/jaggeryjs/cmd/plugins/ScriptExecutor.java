package org.jaggeryjs.cmd.plugins;

import org.clamshellcli.api.Configurator;
import org.clamshellcli.api.Context;
import org.clamshellcli.api.IOConsole;
import org.clamshellcli.core.AnInputController;
import org.jaggeryjs.core.*;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ScriptExecutor extends AnInputController {

    private static final String jaggeryHome = System.getenv("JAGGERY_HOME");

    private static final String INITIALIZER = File.separator + "engines" + File.separator + "index.js";

    private JaggeryEngine engine;

    @Override
    public boolean handle(Context ctx) {
        IOConsole console = ctx.getIoConsole();
        String inputLine = (String) ctx.getValue(Context.KEY_COMMAND_LINE_INPUT);
        if (inputLine != null && !inputLine.isEmpty()) {
            Map<String, Object> scope = new HashMap<String, Object>();
            scope.put("source", inputLine);
            try {
                Object obj = engine.exec(scope);
                if (obj != null) {
                    console.print(obj.toString() + Configurator.VALUE_LINE_SEP);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    @Override
    public void plug(Context plug) {
        Map<String, Object> globals = new HashMap<String, Object>();
        try {
            String cwd = new File("").getAbsolutePath();
            globals.put("cwd", cwd);
            globals.put("writer", plug.getIoConsole().getWriter());
            globals.put("separator", Configurator.VALUE_LINE_SEP);
            engine = new JaggeryEngine("cmd", jaggeryHome, globals, new JaggeryDiskFile(resolvePath(INITIALIZER)));
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public void unplug(Context plug) {
    }

    private String resolvePath(String path) {
        return jaggeryHome + path;
    }

}