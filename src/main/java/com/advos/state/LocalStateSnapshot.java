package com.advos.state;

import com.advos.models.Config;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;

public class LocalStateSnapshot extends LocalState {
    public LocalStateSnapshot(
            boolean isActive,
            boolean isBlue,
            int messageCounter,
            int messageReceiveCounter,
            List<Integer> vectorClock,
            Config config
    ) {
        super(isActive, config);
        this.isBlue = new AtomicBoolean(isBlue);
        this.messageCounter = new AtomicInteger(messageCounter);
        this.messageReceiveCounter = new AtomicInteger(messageReceiveCounter);
        this.vectorClock = new AtomicIntegerArray(
                vectorClock.stream()
                        .mapToInt(Integer::intValue)
                        .toArray()
        );
    }
}
