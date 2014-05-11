package org.jaggeryjs.cmd;

import org.clamshellcli.api.Context;
import org.clamshellcli.api.Plugin;
import org.clamshellcli.api.Shell;
import org.clamshellcli.core.Clamshell;
import org.clamshellcli.impl.CliConsole;
import org.clamshellcli.impl.CliShell;
import org.jaggeryjs.cmd.plugins.JaggerySplashScreen;
import org.jaggeryjs.cmd.plugins.JaggeryPrompt;
import org.jaggeryjs.cmd.plugins.ScriptExecutor;

import java.util.ArrayList;
import java.util.List;

public class Client {
    public static void main(String[] args) {
        final Context context = Clamshell.Runtime.getContext();

        List<Plugin> plugins = new ArrayList<Plugin>();
        Shell shell = new CliShell();
        plugins.add(shell);
        plugins.add(new CliConsole());
        plugins.add(new ScriptExecutor());
        plugins.add(new JaggerySplashScreen());
        plugins.add(new JaggeryPrompt());

        context.putValue(Context.KEY_PLUGINS, plugins);
        shell.plug(context);

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                System.out.println("Bye till we meet again ....");
                Shell s = context.getShell();
                if (s != null) {
                    s.unplug(context);
                }
            }
        });

    }
}

