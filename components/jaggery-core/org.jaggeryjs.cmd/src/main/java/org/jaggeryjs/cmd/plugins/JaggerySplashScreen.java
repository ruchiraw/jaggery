package org.jaggeryjs.cmd.plugins;

import org.clamshellcli.api.Configurator;
import org.clamshellcli.api.Context;
import org.clamshellcli.api.IOConsole;
import org.clamshellcli.api.SplashScreen;

public class JaggerySplashScreen implements SplashScreen {
    private StringBuilder screen;

    @Override
    public void render(Context ctx) {
        IOConsole console = ctx.getIoConsole();
        console.println(screen.toString());
    }

    @Override
    public void plug(Context plug) {
        screen = new StringBuilder();
        screen.append(Configurator.VALUE_LINE_SEP)
                .append(Configurator.VALUE_LINE_SEP)
                .append("      _                                   ").append(Configurator.VALUE_LINE_SEP)
                .append("     | | __ _  __ _  __ _  ___ _ __ _   _ ").append(Configurator.VALUE_LINE_SEP)
                .append("  _  | |/ _` |/ _` |/ _` |/ _ | '__| | | |").append(Configurator.VALUE_LINE_SEP)
                .append(" | |_| | (_| | (_| | (_| |  __| |  | |_| |").append(Configurator.VALUE_LINE_SEP)
                .append("  \\___/ \\__,_|\\__, |\\__, |\\___|_|   \\__, |").append(Configurator.VALUE_LINE_SEP)
                .append("              |___/ |___/           |___/ ").append(Configurator.VALUE_LINE_SEP)
                .append(Configurator.VALUE_LINE_SEP)
                .append("                                                  http://jaggeryjs.org").append(Configurator.VALUE_LINE_SEP)
                .append(Configurator.VALUE_LINE_SEP)
                .append("Java version: ").append(System.getProperty("java.version")).append(Configurator.VALUE_LINE_SEP)
                .append("Java Home: ").append(System.getProperty("java.home")).append(Configurator.VALUE_LINE_SEP)
                .append("OS: ").append(System.getProperty("os.name")).append(", Version: ").append(System.getProperty("os.version"))
                .append(Configurator.VALUE_LINE_SEP)
                .append(Configurator.VALUE_LINE_SEP);
    }

    @Override
    public void unplug(Context plug) {
    }

}