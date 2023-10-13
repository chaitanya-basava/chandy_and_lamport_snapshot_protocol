package com.advos.state;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GlobalState extends State {
    private final Map<Integer, LocalState> localStates;
    private final List<ChannelState> channelStates;

    public GlobalState() {
        this.localStates = new HashMap<>();
        this.channelStates = new ArrayList<>();
    }

    public GlobalState(Map<Integer, LocalState> localStates, List<ChannelState> channelStates) {
        this.localStates = localStates;
        this.channelStates = channelStates;
    }

    public Map<Integer, LocalState> getLocalStates() {
        return this.localStates;
    }

    public List<ChannelState> getChannelStates() {
        return this.channelStates;
    }

    public void addChannelState(ChannelState channelState) {
        this.channelStates.add(channelState);
    }

    public void addLocalState(int nodeId, LocalState localState) {
        this.localStates.put(nodeId, localState);
    }

    public LocalState getLocalStateForNode(int nodeId) {
        return this.localStates.get(nodeId);
    }

    public void clear() {
        synchronized(this) {
            synchronized(this.localStates) {
                this.localStates.clear();
            }
            synchronized(this.channelStates) {
                this.channelStates.clear();
            }
        }
    }
}
