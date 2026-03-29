package io.github.kusoroadeolu.cliquekit.logging;

import java.util.logging.Handler;
import java.util.logging.Logger;

public class CliqueLogging {

    public static void  install(){
        Logger root = Logger.getLogger("");
        for (Handler h : root.getHandlers()){
            h.setFormatter(new CliqueFormatter());
        }
    }
}
