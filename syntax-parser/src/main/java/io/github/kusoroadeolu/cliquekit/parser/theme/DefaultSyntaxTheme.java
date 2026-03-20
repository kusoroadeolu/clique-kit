package io.github.kusoroadeolu.cliquekit.parser.theme;

import io.github.kusoroadeolu.clique.Clique;
import io.github.kusoroadeolu.clique.spi.AnsiCode;

class DefaultSyntaxTheme implements SyntaxTheme {

    private static final AnsiCode KEYWORD        = Clique.rgb(204, 120, 50);
    private static final AnsiCode STRING         = Clique.rgb(106, 135, 89);
    private static final AnsiCode NUMBER_LITERAL = Clique.rgb(104, 151, 187);
    private static final AnsiCode COMMENT        = Clique.rgb(128, 128, 128);
    private static final AnsiCode ANNOTATION     = Clique.rgb(187, 181, 41);
    private static final AnsiCode METHOD         = Clique.rgb(255, 198, 109);
    private static final AnsiCode TYPES          = Clique.rgb(169, 183, 198);
    private static final AnsiCode CONSTANTS = Clique.rgb(152, 118, 170); // intellij purple

    @Override public AnsiCode keyword()       { return KEYWORD; }
    @Override public AnsiCode string()        { return STRING; }
    @Override public AnsiCode numberLiteral() { return NUMBER_LITERAL; }
    @Override public AnsiCode comment()       { return COMMENT; }
    @Override public AnsiCode annotation()    { return ANNOTATION; }
    @Override public AnsiCode method()        { return METHOD; }
    @Override public AnsiCode gutter()        { return COMMENT; }

    @Override
    public AnsiCode types() {
        return TYPES;
    }

    @Override
    public AnsiCode constants() {
        return CONSTANTS;
    }
}