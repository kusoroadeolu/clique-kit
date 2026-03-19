package io.github.kusoroadeolu.cliquekit.timer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TimerTests {
    @Test
    void whenClockDone_timeShouldBeZero() throws InterruptedException {
        var clock = new Clock(new Time(0, 0, 1));
        while (!clock.isDone()){
            clock.tick(true);
        }

        assertEquals(0, clock.seconds());
    }

    @Test
    void clock_givenOneHour_onTick_minutesAndSeconds_shouldEquals59() throws InterruptedException {
        var clock = new Clock(new Time(1, 0, 0));
        clock.tick(true);
        assertEquals(59, clock.minutes());
        assertEquals(59, clock.seconds());
    }

    @Test
    void clock_givenOneMinute_onTick_seconds_shouldEquals59() throws InterruptedException {
        var clock = new Clock(new Time(0, 1, 0));
        clock.tick(true);
        assertEquals(59, clock.seconds());
    }

}