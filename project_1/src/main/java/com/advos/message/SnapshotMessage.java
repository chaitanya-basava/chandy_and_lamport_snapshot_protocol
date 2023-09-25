package com.advos.message;

import com.advos.state.ChannelState;
import com.advos.state.LocalState;

import java.util.List;

public class SnapshotMessage extends Message {
    private final LocalState localState;
    private final List<ChannelState> channelStates;

    public SnapshotMessage(int sourceNodeId, LocalState localState, List<ChannelState> channelStates) {
        super("", sourceNodeId);
        this.localState = localState;
        this.channelStates = channelStates;
    }

    public LocalState getLocalState() {
        return localState;
    }

    public List<ChannelState> getChannelStates() {
        return channelStates;
    }

    @Override
    public String toString() {
        return super.toString() + " Snapshot from " + this.getSourceNodeId();
    }
}
