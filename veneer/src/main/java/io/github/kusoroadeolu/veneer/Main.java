package io.github.kusoroadeolu.veneer;

import io.github.kusoroadeolu.veneer.theme.SyntaxThemes;

import java.io.IOException;
import java.nio.file.Path;

public class Main {
    public static void main(String[] args) throws IOException {
        new GoSyntaxHighlighter(SyntaxThemes.CATPPUCCIN_MOCHA).print(
                """
package main

func main() {
	ch := make(chan int)
	go func() {
		ch <- 42
	}()
	v := <-ch
	_ = v
}
                        """
        );
    }
}
