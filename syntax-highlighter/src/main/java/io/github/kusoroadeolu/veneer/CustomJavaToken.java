package io.github.kusoroadeolu.veneer;

import com.github.javaparser.JavaToken;

class CustomJavaToken extends JavaToken {
    public CustomJavaToken(int kind, String text) {
        super(kind, text);
    }

}
