package com.advos.message;


public class TerminationMessage extends Message {
    public TerminationMessage(int sourceNodeId) {
        super("Termination", sourceNodeId);
    }

    @Override
    public String toString() {
        return "[TerminationMessage]----sourceNodeId:" + this.getSourceNodeId() + "----";
    }

    public static TerminationMessage deserialize(String serializedTerminationMessage) {
        return new TerminationMessage(Integer.parseInt(serializedTerminationMessage.split("----")[1].split(":")[1]));
    }
}
