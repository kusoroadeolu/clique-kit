package io.github.kusoroadeolu.timer;

public record Configuration(Time time, String audioPath) {
    public static final Configuration DEFAULT = new Configuration(new Time(0,0, 0), "");
}

