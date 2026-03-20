package io.github.kusoroadeolu.cliquekit.parser.theme;

import io.github.kusoroadeolu.clique.Clique;
import io.github.kusoroadeolu.clique.spi.AnsiCode;

public class CatppuccinMochaSyntaxTheme implements SyntaxTheme {

    private static final AnsiCode KEYWORD        = Clique.rgb(203, 166, 247); // ctp_mauve
    private static final AnsiCode STRING         = Clique.rgb(166, 227, 161); // ctp_green
    private static final AnsiCode NUMBER_LITERAL = Clique.rgb(250, 179, 135); // ctp_peach
    private static final AnsiCode COMMENT        = Clique.rgb(127, 132, 156); // ctp_overlay1
    private static final AnsiCode ANNOTATION     = Clique.rgb(249, 226, 175); // ctp_yellow
    private static final AnsiCode METHOD         = Clique.rgb(137, 180, 250); // ctp_blue
    private static final AnsiCode GUTTER         = Clique.rgb(88, 91, 112);   // ctp_surface2

    @Override public AnsiCode keyword()       { return KEYWORD; }
    @Override public AnsiCode string()        { return STRING; }
    @Override public AnsiCode numberLiteral() { return NUMBER_LITERAL; }
    @Override public AnsiCode comment()       { return COMMENT; }
    @Override public AnsiCode annotation()    { return ANNOTATION; }
    @Override public AnsiCode method()        { return METHOD; }
    @Override public AnsiCode gutter()        { return GUTTER; }
}