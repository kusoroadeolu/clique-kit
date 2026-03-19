package io.github.kusoroadeolu.cliquekit.timer;

public record Configuration(Time time, String audioPath, String title) {
    public static final Configuration DEFAULT = new Configuration(Time.DEFAULT, "", "Live Timer");
}

