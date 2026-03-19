package io.github.kusoroadeolu.cliquekit.timer;

import io.github.kusoroadeolu.clique.Clique;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;

import java.io.FileInputStream;
import java.io.IOException;

public class SoundPlayer {
    public static void playSound(String audioFPath){
            try(var fis = new FileInputStream(audioFPath)) {
                if (audioFPath.isBlank()) return;
                Player player = new Player(fis);
                player.play();
            } catch (IOException | JavaLayerException e) {
                Clique.parser().print("[bold, ctp_red]Failed to play audio file %s".formatted(audioFPath));
            }
    }

}
