package io.github.kusoroadeolu.cliquekit.logging;

import io.github.kusoroadeolu.clique.Clique;
import io.github.kusoroadeolu.clique.ansi.ColorCode;
import io.github.kusoroadeolu.clique.ansi.StyleCode;
import io.github.kusoroadeolu.clique.spi.AnsiCode;

import java.util.Arrays;
import java.util.logging.Logger;

public class Demo {
    //private static final Logger logger = LogManager.getLogger(Demo.class);
    private static final Logger logger = Logger.getLogger(Demo.class.getName());

    static {
        CliqueLogging.install();
    }

    public static void main(String[] args) {

        // Optional: register semantic styles
        Clique.registerStyle("ok",    new CompositeStyle(ColorCode.BRIGHT_GREEN, StyleCode.BOLD));
        Clique.registerStyle("error", new CompositeStyle(ColorCode.BRIGHT_RED,   StyleCode.BOLD));
        Clique.registerStyle("warn",  new CompositeStyle(ColorCode.YELLOW,       StyleCode.ITALIC));

        // Then just log normally with Clique markup
        logger.info("[green, bold]Server started[/] on port [cyan]8080[/]");
        //logger.warn("[warn]Retry {}/3[/] — connection timeout", 2);
        //logger.error("[error]Build failed:[/] could not bind to [red]{}[/]", 8080);
        logger.info("[ok]All tests passed[/]");
    }


    record CompositeStyle(AnsiCode... codes) implements AnsiCode{
        public String toString() {
            StringBuilder sb = new StringBuilder();
            Arrays.stream(codes).forEach(sb::append);
            return sb.toString();
        }
    }
}