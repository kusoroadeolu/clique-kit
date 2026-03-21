package io.github.kusoroadeolu.veneer.theme;

import io.github.kusoroadeolu.clique.spi.AnsiCode;

public interface SyntaxTheme {
    AnsiCode keyword();
    AnsiCode stringLiteral();
    AnsiCode numberLiteral();
    AnsiCode comment();
    AnsiCode annotation();
    AnsiCode method();
    AnsiCode gutter();
    AnsiCode types();
    AnsiCode constants();
}