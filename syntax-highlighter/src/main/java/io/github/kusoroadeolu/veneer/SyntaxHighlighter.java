package io.github.kusoroadeolu.veneer;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;

public interface SyntaxHighlighter {
    String highlight(String s);

    String highlight(Path path) throws IOException;

    void print(String s, PrintStream stream);

    void print(String s);

    void print(Path path, PrintStream stream) throws IOException;

    void print(Path path) throws IOException;
}
