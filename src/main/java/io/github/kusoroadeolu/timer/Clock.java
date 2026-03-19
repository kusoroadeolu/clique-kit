package io.github.kusoroadeolu.timer;

import io.github.kusoroadeolu.clique.core.display.Component;

public class Clock implements Component {
    private int hour;
    private int minutes;
    private int seconds;
    private volatile boolean done = false;

    Clock(Time time){
        hour = time.hours();
        minutes = time.minutes();
        seconds = time.seconds();
    }

    @Override
    public String get() {
        return "[ctp_mauve, bold]%02d:%02d:%02d[/]"
                .formatted(hour, minutes, seconds);
    }


    public void tick() throws InterruptedException {
        if (--seconds < 0) {
            seconds = 59;
            if (--minutes < 0) {
                minutes = 59;
                if (--hour < 0){
                    done = true;
                }
            }
        }
        Thread.sleep(1000);
    }

    public boolean isDone(){
        return done;
    }
}
