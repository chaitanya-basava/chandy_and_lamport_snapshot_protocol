package com.advos.message;

import com.advos.utils.Node;


public class MarkerMessage extends Message {
    public MarkerMessage(Node sourceNode) {
        super("Marker", sourceNode);
    }
}
