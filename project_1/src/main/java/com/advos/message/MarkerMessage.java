package com.advos.message;


public class MarkerMessage extends Message {
    public MarkerMessage(int sourceNodeId) {
        super("Marker", sourceNodeId);
    }
}
