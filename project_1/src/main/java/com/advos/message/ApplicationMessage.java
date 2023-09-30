package com.advos.message;

import java.util.ArrayList;
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
        return "[ApplicationMessage]----msg:" + this.getMsg() +
                "----sourceNodeId:" + this.getSourceNodeId() +
                "----piggybackedClock:" + this.getPiggybackedClock().toString()
                    .replace("[", "").replace("]", "").replace(", ", ",") + "----";
    }

    public static ApplicationMessage deserialize(String serializedApplicationMessage) {
        String[] applicationMessage = serializedApplicationMessage.split("----");

        String[] strArray = applicationMessage[3].split(":")[1].split(",");

        List<Integer> intArray = new ArrayList<>(strArray.length);
        for(String s : strArray) { intArray.add(Integer.parseInt(s)); }

        return new ApplicationMessage(
                applicationMessage[1].split(":")[1],
                new ArrayList<>(intArray),
                Integer.parseInt(applicationMessage[2].split(":")[1])
        );
    }
}
