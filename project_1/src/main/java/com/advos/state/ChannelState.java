package com.advos.state;

import java.util.List;

public class ChannelState extends State {
    private final int sourceNodeId;
    private final int  destinationNodeId;

    // vector clock piggybacked in the message
    private final List<Integer> vectorClock;

    public ChannelState(int sourceNodeId, int destinationNodeId, List<Integer> vectorClock) {
        this.sourceNodeId = sourceNodeId;
        this.destinationNodeId = destinationNodeId;
        this.vectorClock = vectorClock;
    }

    public List<Integer> getVectorClock() {
        return vectorClock;
    }

    public int getSourceNodeId() {
        return sourceNodeId;
    }

    public int getDestinationNodeId() {
        return destinationNodeId;
    }
}
