package com.advos.state;

import com.advos.models.Config;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;

public class LocalState extends State {
    AtomicBoolean isActive;
    AtomicInteger messageCounter;
    AtomicInteger messageReceiveCounter;
    AtomicBoolean isBlue;

    // virtual vector clock of the node
    AtomicIntegerArray vectorClock;

    public LocalState(boolean isActive, Config config) {
        this.isActive = new AtomicBoolean(isActive);
        this.isBlue = new AtomicBoolean(true);
        this.messageCounter = new AtomicInteger(0);
        this.vectorClock = new AtomicIntegerArray(config.getN());
        this.messageReceiveCounter = new AtomicInteger(0);
    }

    public boolean getIsActive() {
        return this.isActive.get();
    }

    public boolean getIsBlue() {
        return this.isBlue.get();
    }

    public int getMessageCounter() {
        return this.messageCounter.get();
    }

    public int getMessageReceiveCounter() {
        return this.messageReceiveCounter.get();
    }

    public int getVectorClockAti(int i) {
        return this.vectorClock.get(i);
    }

    public void incrementVectorClockAti(int i) {
        this.vectorClock.incrementAndGet(i);
    }

    public List<Integer> getVectorClock() {
        List<Integer> clock = new ArrayList<>();
        synchronized(this) {
            for(int i = 0; i < this.vectorClock.length(); i++)
                clock.add(this.vectorClock.get(i));
        }

        return clock;
    }

    public int getClockSize() {
        return this.vectorClock.length();
    }

    public void setVectorClockAti(int i, int value) {
        this.vectorClock.set(i, value);
    }

    public void setIsActive(boolean active) {
        this.isActive.set(active);
    }

    public void setIsBlue(boolean isBlue) {
        this.isBlue.set(isBlue);
    }

    public int incrementMessageCounter() {
        return this.messageCounter.incrementAndGet();
    }

    public void incrementMessageReceiveCounter() {
        this.messageReceiveCounter.incrementAndGet();
    }

    @Override
    public String toString() {
        return this.getVectorClock().toString().replace("[", "")
                .replace("]", "")
                .replace(" ", "")
                .replace(",", " ");
    }
}
