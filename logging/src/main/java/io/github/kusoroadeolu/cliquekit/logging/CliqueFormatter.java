package io.github.kusoroadeolu.cliquekit.logging;

import io.github.kusoroadeolu.clique.parser.AnsiStringParser;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;

//For JUL, Java util logging
public class CliqueFormatter extends Formatter {

    private static final AnsiStringParser PARSER = AnsiStringParser.DEFAULT; //Thread safe parser

    @Override
    public String format(LogRecord record) {
        var msg = formatMessage(record);
        //var raw = PARSER.getOriginalString(msg); //Remove the red ansi from before
        return PARSER.parse(msg) + getThrowableString(record);
    }

    String getThrowableString(LogRecord record){
        var t = record.getThrown();
        if (t == null) return "\n";
        return "\n" + t + "\n";
    }
}
