package com.advos.state;

public class ChannelState extends State {
    private final int sourceNodeId;
    private final int  destinationNodeId;
    private final String msg;

    public ChannelState(int sourceNodeId, int destinationNodeId, String msg) {
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

    public String getMsg() {
        return msg;
    }

    @Override
    public String toString() {
        return "sourceNodeId;" + this.getSourceNodeId() +
                "####destinationNodeId;" + this.getDestinationNodeId() + "####msg;" + this.getMsg();
    }

    public static ChannelState deserialize(String serializedChannelState) {
        String[] channelState = serializedChannelState.split("####");
        return new ChannelState(
                Integer.parseInt(channelState[0].split(";")[1]),
                Integer.parseInt(channelState[1].split(";")[1]),
                channelState[2].split(";")[1]
        );
    }
}
