package io.github.kusoroadeolu.cliquekit.logging;

import io.github.kusoroadeolu.clique.Clique;
import io.github.kusoroadeolu.clique.parser.AnsiStringParser;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.pattern.ConverterKeys;
import org.apache.logging.log4j.core.pattern.LogEventPatternConverter;
import org.apache.logging.log4j.core.pattern.PatternConverter;

//For log4j
@Plugin(name = "CliqueMessageConverter", category = PatternConverter.CATEGORY)
@ConverterKeys({"cm", "cliqueMsg"})
public class CliqueMessageConverter extends LogEventPatternConverter {

    private final AnsiStringParser parser = Clique.parser();

    protected CliqueMessageConverter() {
        super("CliqueMessage", "cliqueMsg");
    }

    public static CliqueMessageConverter newInstance(String[] options) {
        return new CliqueMessageConverter();
    }

    @Override
    public void format(LogEvent event, StringBuilder output) {
        String raw = event.getMessage().getFormattedMessage();
        output.append(parser.parse(raw));
    }
}