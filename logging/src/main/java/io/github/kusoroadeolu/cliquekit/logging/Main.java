package io.github.kusoroadeolu.cliquekit.logging;

import io.github.kusoroadeolu.clique.Clique;
import io.github.kusoroadeolu.clique.config.BorderStyle;
import io.github.kusoroadeolu.clique.spi.AnsiCode;
import io.github.kusoroadeolu.clique.tables.TableType;

import java.util.HashMap;
import java.util.Map;

public class Main {
    static void main() {
        registerRainbow(32);
        registerRainbow(32);
        var style = BorderStyle.immutableBuilder().uniformStyle(Clique.rgb(255, 0, 128)).build();

        Clique.table(TableType.ROUNDED_BOX_DRAW,    style)
                .headers(
                        rainbow("Property", 32),
                        rainbow("Value", 32)
                )
                .row(rainbow("OS", 32), rainbow(System.getProperty("os.name"), 32))
                .row(rainbow("Java Version", 32), rainbow(System.getProperty("java.version"), 32))
                .row(rainbow("User", 32), rainbow(System.getProperty("user.name"), 32))
                .row(rainbow("Available Processors", 32), rainbow(String.valueOf(Runtime.getRuntime().availableProcessors()), 32))
                .row(rainbow("Max Memory (MB)", 32), rainbow(String.valueOf(Runtime.getRuntime().maxMemory() / 1024 / 1024), 32))
                .render();
    }

    public static String rainbow(String text, int steps) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            int index = i % steps;
            sb.append("[rainbow-").append(index).append("]")
                    .append(text.charAt(i))
                    .append("[/]");
        }
        return sb.toString();
    }

    public static void registerRainbow(int steps) {
            Map<String, AnsiCode> rainbow = new HashMap<>();

            for (int i = 0; i < steps; i++) {
                float hue = (float) i / steps;
                java.awt.Color color = java.awt.Color.getHSBColor(hue, 1f, 1f);
                rainbow.put("rainbow-" + i, Clique.rgb(color.getRed(), color.getGreen(), color.getBlue()));
            }

            Clique.registerStyles(rainbow);
    }

}
