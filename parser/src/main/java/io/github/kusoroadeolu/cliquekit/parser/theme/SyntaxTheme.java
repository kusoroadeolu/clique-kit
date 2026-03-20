package io.github.kusoroadeolu.cliquekit.parser.theme;

import io.github.kusoroadeolu.clique.spi.AnsiCode;

public interface SyntaxTheme {
    AnsiCode keyword();
    AnsiCode string();
    AnsiCode numberLiteral();
    AnsiCode comment();
    AnsiCode annotation();
    AnsiCode method();
    AnsiCode gutter();
}