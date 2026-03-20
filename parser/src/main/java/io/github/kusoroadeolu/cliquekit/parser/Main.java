package io.github.kusoroadeolu.cliquekit.parser;

import io.github.kusoroadeolu.clique.parser.AnsiStringParser;
import io.github.kusoroadeolu.cliquekit.parser.theme.SyntaxThemes;

public class Main {
    void main(){

        AnsiStringParser parser = new JavaSyntaxParser();
        IO.println(parser.parse(codeSnippet));

    }


    String codeSnippet = """
            package com.example;
            
            import java.util.List;
            import java.util.Map;
            
            /**
             * Javadoc comment — tests comment coloring
             */
            @SuppressWarnings("unchecked")
            public class StressTest<T extends Comparable<T>> {
            
                private static final int MAX = 100;
                private static final String GREETING = "Hello, World!";
                private static final double PI = 3.14159;
                private static final long BIG = 100_000L;
                private static final int HEX = 0xFF;
            
                // single line comment
                public enum Status { ACTIVE, INACTIVE, PENDING }
            
                public record Point(int x, int y) {}
            
                @Override
                public String toString() {
                    return "StressTest{}";
                }
            
                public static void main(String[] args) {
                    /* multi-line
                       comment */
                    var list = List.of(1, 2, 3);
                    int result = switch (list.size()) {
                        case 1 -> 10;
                        case 2 -> 20;
                        default -> {
                            int val = list.size() * MAX;
                            yield val;
                        }
                    };
            
                    String text = ""\"
                            text block
                            line two
                            ""\";

                    for (int i = 0; i < 10; i++) {
                        if (i % 2 == 0) continue;
                        System.out.println(i);
                    }
                }
            }
            """;
}
