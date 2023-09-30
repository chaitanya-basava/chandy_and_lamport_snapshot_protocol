package com.advos.message;

import com.advos.models.Config;
import com.advos.state.ChannelState;
import com.advos.state.LocalState;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SnapshotMessage extends Message {
    private final LocalState localState;
    private final List<ChannelState> channelStates;
    private final long expirationTime;

    public SnapshotMessage(int sourceNodeId, LocalState localState, List<ChannelState> channelStates) {
        super("", sourceNodeId);
        this.localState = localState;
        this.channelStates = channelStates;
        this.expirationTime = System.currentTimeMillis() + Config.EXPIRATION_TIME;
    }

    public SnapshotMessage(int sourceNodeId, LocalState localState, List<ChannelState> channelStates, long expirationTime) {
        super("", sourceNodeId);
        this.localState = localState;
        this.channelStates = channelStates;
        this.expirationTime = expirationTime;
    }

    public LocalState getLocalState() {
        return localState;
    }

    public List<ChannelState> getChannelStates() {
        return channelStates;
    }

    public long getExpirationTime() {
        return expirationTime;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() > this.expirationTime;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[SnapshotMessage]----LocalState:" + this.getLocalState().toString() + "----ChannelStates:");
        sb.append(this.getChannelStates().size()).append("=");
        for (ChannelState channelState : this.getChannelStates()) {
            sb.append(",").append(channelState.toString()).append("@@");
        }
        sb.append("----sourceNodeId:").append(this.getSourceNodeId());
        sb.append("----expirationTime:").append(this.getExpirationTime());

        return sb + "----";
    }

    public static SnapshotMessage deserialize(String serializedSnapshotMessage) {
        String[] split = serializedSnapshotMessage.split("----");
        LocalState localState = LocalState.deserialize(split[1].split(":")[1]);
        List<ChannelState> channelStates = new ArrayList<>();

        String[] channelStateStr = split[2].split("=");
        System.out.println(Arrays.toString(channelStateStr));

        if(Integer.parseInt(channelStateStr[0].split(":")[1]) > 0) {
            for (String serializedChannelStates: channelStateStr[1].split("@@")) {
                channelStates.add(ChannelState.deserialize(serializedChannelStates));
            }
        }
        int sourceNodeId = Integer.parseInt(split[3].split(":")[1]);
        long expirationTime = Long.parseLong(split[4].split(":")[1]);

        return new SnapshotMessage(sourceNodeId, localState, channelStates, expirationTime);
    }
}
