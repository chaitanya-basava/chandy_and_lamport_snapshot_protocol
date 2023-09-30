package com.advos.message;


public class MarkerMessage extends Message {
    public MarkerMessage(int sourceNodeId) {
        super("Marker", sourceNodeId);
    }

    @Override
    public String toString() {
        return "[MarkerMessage]----sourceNodeId:" + this.getSourceNodeId() + "----";
    }

    public static MarkerMessage deserialize(String serializedMarkerMessage) {
        return new MarkerMessage(Integer.parseInt(serializedMarkerMessage.split("----")[1].split(":")[1]));
    }
}
