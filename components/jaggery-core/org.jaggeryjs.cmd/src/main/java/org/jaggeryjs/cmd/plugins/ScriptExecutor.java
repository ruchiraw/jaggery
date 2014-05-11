package org.jaggeryjs.cmd.plugins;

import org.clamshellcli.api.Configurator;
import org.clamshellcli.api.Context;
import org.clamshellcli.api.IOConsole;
import org.clamshellcli.core.AnInputController;
import org.jaggeryjs.core.JaggeryEngine;
import org.jaggeryjs.core.JaggeryReader;
import org.jaggeryjs.core.JaggeryScript;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ScriptExecutor extends AnInputController {

    private JaggeryEngine engine;
    private String cwd;

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
            cwd = new File("").getAbsolutePath();
            String scriptId = "/Users/ruchira/sources/github/forks/jaggery/engines/cmd/index.js";

            FileReader intializer = new FileReader(scriptId);
            JaggeryReader reader = new JaggeryReader() {
                @Override
                public JaggeryScript getScript(String scriptId) throws IOException {
                    String path = cwd + scriptId;
                    return new JaggeryScript(path, new FileReader(path));
                }
            };
            globals.put("cwd", cwd);
            globals.put("reader", reader);
            globals.put("writer", plug.getIoConsole().getWriter());
            globals.put("separator", Configurator.VALUE_LINE_SEP);
            engine = new JaggeryEngine(globals, new JaggeryScript(scriptId, intializer));
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public void unplug(Context plug) {
    }

}