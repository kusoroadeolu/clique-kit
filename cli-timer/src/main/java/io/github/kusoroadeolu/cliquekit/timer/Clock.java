package io.github.kusoroadeolu.cliquekit.timer;

import io.github.kusoroadeolu.clique.Clique;
import io.github.kusoroadeolu.clique.core.display.Component;

public class Clock implements Component {
    private int hours;
    private int minutes;
    private int seconds;
    private boolean done = false;

    Clock(Time time){
        hours = time.hours();
        minutes = time.minutes();
        seconds = time.seconds();
        verifyTime();
    }

    @Override
    public String get() {
        return "[ctp_maroon, bold]%02d:%02d:%02d[/]"
                .formatted(hours, minutes, seconds);
    }

    //For tests
    void tick(boolean dontSleep) throws InterruptedException {
        if (--seconds < 0){
            if (--minutes < 0){
                if (--hours < 0){
                    clearTime();
                    done = true;
                    return;
                }
                minutes = 59;
            }
            seconds = 59;
        }

        if (!dontSleep) Thread.sleep(1000);
    }

    void clearTime(){
        hours = 0;
        minutes = 0;
        seconds = 0;
    }

    /*
     * seconds >= 0
     * minutes >= 0
     * hour >= 0
     * when seconds = 0 if minute > 0, second = 60
     * when minute = 0 if hour > 0, minute = 59, second = 60
     * when hour = 0 && minute = 0 && seconds = 0, we're done
     * */
    public void tick() throws InterruptedException {
        tick(false);
    }

    public int hours() {
        return hours;
    }

    public int seconds() {
        return seconds;
    }

    public int minutes() {
        return minutes;
    }

    public boolean isDone(){
        return done;
    }

    void verifyTime(){
         if (minutes > 60){
            Clique.parser().print("[ctp_red, bold] Minutes given cannot be greater than 60");
            clearTime();
        }else if(seconds > 60){
            Clique.parser().print("[ctp_red, bold] Seconds given cannot be greater than 60");
            clearTime();
        }
    }
}
