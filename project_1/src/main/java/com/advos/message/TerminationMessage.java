package com.advos.message;

import com.advos.utils.Node;


public class TerminationMessage extends Message {
    public TerminationMessage(Node sourceNode) {
        super("Termination", sourceNode);
    }
}
