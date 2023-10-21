package com.advos.message;

import java.io.Serializable;

public abstract class Message implements Serializable {
    private final String msg;
    private final int sourceNodeId;

    protected Message(String msg, int sourceNodeId) {
        this.msg = msg;
        this.sourceNodeId = sourceNodeId;
    }

    public String getMsg() {
        return msg;
    }

    public int getSourceNodeId() {
        return sourceNodeId;
    }

    @Override
    public String toString() {
        return "[from: Node-" + sourceNodeId + "] " + this.getMsg();
    }
}
