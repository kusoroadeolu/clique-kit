package io.github.kusoroadeolu.cliquekit.timer;

import com.google.gson.Gson;
import io.github.kusoroadeolu.clique.Clique;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static io.github.kusoroadeolu.cliquekit.timer.SoundPlayer.playSound;

public class Timer {
    private final Configuration config;
    private final LiveRenderer liveRenderer;
    private static final Path CONFIG_PATH = Path.of("C:\\.cli-timer\\config.json").normalize();
    private static final Gson GSON = new Gson();

    Timer() {
        var json = readConfig();
        if (json == null) config = Configuration.DEFAULT;
        else config = GSON.fromJson(json, Configuration.class);
        this.liveRenderer = new LiveRenderer(config);
    }

    public static void start() throws InterruptedException {
        new Timer().startTimer();
    }

    private void startTimer() throws InterruptedException {
        liveRenderer.render();
        playSound(config.audioPath());
    }

    String readConfig(){
        try {
           return Files.readString(CONFIG_PATH);
        } catch (IOException e) {
            Clique.parser().print("[ctp_red]Failed to read configuration from config path %s".formatted(CONFIG_PATH));
            return null;
        }
    }
}
