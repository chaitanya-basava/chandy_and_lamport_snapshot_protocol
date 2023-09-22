package com.advos.state;

import com.advos.message.ApplicationMessage;

public class ChannelState extends State {
    private final int sourceNodeId;
    private final int  destinationNodeId;

    private final ApplicationMessage msg;

    public ChannelState(int sourceNodeId, int destinationNodeId, ApplicationMessage msg) {
        this.sourceNodeId = sourceNodeId;
        this.destinationNodeId = destinationNodeId;
        this.msg = msg;
    }

    public int getSourceNodeId() {
        return sourceNodeId;
    }

    public int getDestinationNodeId() {
        return destinationNodeId;
    }

    public ApplicationMessage getMsg() {
        return msg;
    }
}
