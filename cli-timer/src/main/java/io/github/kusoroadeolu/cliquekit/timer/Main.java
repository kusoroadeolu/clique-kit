package io.github.kusoroadeolu.cliquekit.timer;

import static io.github.kusoroadeolu.clique.Clique.registerTheme;

public class Main {
    void main() throws InterruptedException{
        registerTheme("catppuccin-mocha"); //Hardcoded rn, could make this a config option
        Timer.start();
    }



}