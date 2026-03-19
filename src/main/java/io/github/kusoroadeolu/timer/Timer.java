package io.github.kusoroadeolu.timer;

import com.google.gson.Gson;
import io.github.kusoroadeolu.clique.Clique;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Timer {
    private final Configuration config;
    private final LiveRenderer liveRenderer;
    private static final Path CONFIG_PATH = Path.of("C:\\Users\\eastw\\Git Projects\\Personal\\clique-tests\\config.json");
    private static final Gson GSON = new Gson();

    public Timer() {
        var json = readConfig();
        if (json == null) config = Configuration.DEFAULT;
        else config = GSON.fromJson(json, Configuration.class);
        this.liveRenderer = new LiveRenderer(config.time());
    }

    public void start() throws InterruptedException {
        liveRenderer.render();
        SoundPlayer.playSound(config.audioPath());
    }

    static String readConfig(){
        try {
           return Files.readString(CONFIG_PATH);
        } catch (IOException e) {
            Clique.parser().print("[ctp_red]Failed to read configuration from config path %s".formatted(CONFIG_PATH));
            return null;
        }
    }
}
