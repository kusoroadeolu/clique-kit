package io.github.kusoroadeolu.timer;

import io.github.kusoroadeolu.clique.Clique;
import io.github.kusoroadeolu.clique.frame.Frame;

public class LiveRenderer {
    private final Frame frame;
    private final Clock clock;

    public LiveRenderer(int width, Time time) {
        this.frame = Clique
                .frame()
                .width(width);
        clock = new Clock(time);
    }

    public LiveRenderer(Time time){
        this.frame = Clique
                .frame();

        clock = new Clock(time);
    }


    public void render() throws InterruptedException {
        frame.title("[ctp_mauve]Pomo Timer[/]")
                .nest(clock);

        int lastSize = 0;
        while (!clock.isDone()) {
            if (lastSize > 0) System.out.print("\033[" + lastSize + "A");

            String rendered = frame.get();
            var lines = rendered.lines().toList();
            for (var line : lines) System.out.println("\r" + line);
            lastSize = lines.size();
            clock.tick();
        }
    }
}
