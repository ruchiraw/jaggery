package org.jaggeryjs.cmd.plugins;

import org.clamshellcli.api.Context;
import org.clamshellcli.api.Prompt;

public class JaggeryPrompt implements Prompt {
    @Override
    public String getValue(Context context) {
        return "jaggery> ";
    }

    @Override
    public void plug(Context context) {

    }

    @Override
    public void unplug(Context context) {

    }
}
