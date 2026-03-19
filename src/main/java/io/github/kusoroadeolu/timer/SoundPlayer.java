package io.github.kusoroadeolu.timer;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;

import java.io.FileInputStream;
import java.io.IOException;

public class SoundPlayer {
    public static void playSound(String audioFPath){
            try(var fis = new FileInputStream(audioFPath)) {
                Player player = new Player(fis);
                player.play();
            } catch (IOException | JavaLayerException e) {
                throw new RuntimeException(e);
            }
    }

}
