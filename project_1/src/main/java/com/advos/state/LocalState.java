package com.advos.state;

import com.advos.models.Config;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicLong;

public class LocalState extends State {
    AtomicBoolean isActive;
    AtomicInteger messageCounter;
    AtomicInteger messageReceiveCounter;
    AtomicBoolean isBlue;
    AtomicLong lastBlueTimestamp;

    // virtual vector clock of the node
    AtomicIntegerArray vectorClock;

    public LocalState(boolean isActive, Config config) {
        this.isActive = new AtomicBoolean(isActive);
        this.isBlue = new AtomicBoolean(true);
        this.messageCounter = new AtomicInteger(0);
        this.vectorClock = new AtomicIntegerArray(config.getN());
        this.messageReceiveCounter = new AtomicInteger(0);
        this.lastBlueTimestamp = new AtomicLong(0);
    }

    public LocalState(boolean isActive, boolean isBlue, int messageCounter, int messageReceiveCounter,
                      long lastBlueTimestamp, int[] vectorClock) {
        this.isActive = new AtomicBoolean(isActive);
        this.isBlue = new AtomicBoolean(isBlue);
        this.messageCounter = new AtomicInteger(messageCounter);
        this.messageReceiveCounter = new AtomicInteger(messageReceiveCounter);
        this.lastBlueTimestamp = new AtomicLong(lastBlueTimestamp);
        this.vectorClock = new AtomicIntegerArray(vectorClock);
    }

    public long getLastBlueTimestamp() {
        return this.lastBlueTimestamp.get();
    }

    public void setLastBlueTimestamp(long timestamp) {
        this.lastBlueTimestamp.set(timestamp);
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

    public String getDelimitedVectorString(String delimiter) {
        return this.getVectorClock().toString().replace("[", "")
                .replace("]", "")
                .replace(" ", "")
                .replace(",", delimiter);
    }

    @Override
    public String toString() {
        return "isActive;" + this.getIsActive() + "####isBlue;" + this.getIsBlue() +
                "####messageCounter;" + this.getMessageCounter() + "####messageReceiveCounter;" +
                this.getMessageReceiveCounter() + "####lastBlueTimestamp;" + this.getLastBlueTimestamp() +
                "####vectorClock;" + this.getVectorClock().toString().replace("[", "").replace("]", "").replace(", ", ",");
    }

    public static LocalState deserialize(String serializedState) {
        String[] serializedStateSplit = serializedState.split("####");

        boolean isActive = Boolean.parseBoolean(serializedStateSplit[0].split(";")[1]);
        boolean isBlue = Boolean.parseBoolean(serializedStateSplit[1].split(";")[1]);
        int messageCounter = Integer.parseInt(serializedStateSplit[2].split(";")[1]);
        int messageReceiveCounter = Integer.parseInt(serializedStateSplit[3].split(";")[1]);
        long lastBlueTimestamp = Long.parseLong(serializedStateSplit[4].split(";")[1]);

        String[] vectorClockSplit = serializedStateSplit[5].split(";")[1].split(",");
        int[] vectorClock = new int[vectorClockSplit.length];
        for (int i = 0; i < vectorClockSplit.length; i++) {
            vectorClock[i] = Integer.parseInt(vectorClockSplit[i]);
        }

        return new LocalState(isActive, isBlue, messageCounter, messageReceiveCounter, lastBlueTimestamp, vectorClock);
    }
}
