package com.advos.message;

import java.util.List;

public class ApplicationMessage extends Message {
    private final List<Integer> piggybackedClock;

    public ApplicationMessage(String msg, List<Integer> clock, int sourceNodeId) {
        super(msg, sourceNodeId);
        this.piggybackedClock = clock;
    }

    public List<Integer> getPiggybackedClock() {
        return piggybackedClock;
    }

    public int getPiggybackedClockAti(int i) {
        return piggybackedClock.get(i);
    }

    @Override
    public String toString() {
        return super.toString() + " [Piggybacked Clock] " + this.getPiggybackedClock().toString() + "\n";
    }
}
