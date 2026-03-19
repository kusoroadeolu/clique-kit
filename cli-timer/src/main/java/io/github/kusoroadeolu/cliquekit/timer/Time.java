package io.github.kusoroadeolu.cliquekit.timer;

public record Time(int hours, int minutes, int seconds){
    public static final Time DEFAULT = new Time(0,0,0);
}
