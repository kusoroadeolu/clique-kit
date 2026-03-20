package io.github.kusoroadeolu.cliquekit.parser;

import com.github.javaparser.JavaToken;

public class CustomJavaToken extends JavaToken {
    public CustomJavaToken(int kind, String text) {
        super(kind, text);
    }

}
