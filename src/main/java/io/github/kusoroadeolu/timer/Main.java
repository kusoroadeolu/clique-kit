package io.github.kusoroadeolu.timer;

import io.github.kusoroadeolu.clique.Clique;

public class Main {
    void main() throws InterruptedException{
        Clique.registerTheme("catppuccin-mocha"); //Hardcoded rn, could make this a config option
        Timer timer = new Timer();
        timer.start();
    }



}