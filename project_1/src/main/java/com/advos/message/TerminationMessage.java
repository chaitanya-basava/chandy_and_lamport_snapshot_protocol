package com.advos.message;


public class TerminationMessage extends Message {
    public TerminationMessage(int sourceNodeId) {
        super("Termination", sourceNodeId);
    }
}
