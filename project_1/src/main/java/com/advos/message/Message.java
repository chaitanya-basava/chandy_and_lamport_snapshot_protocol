package com.advos.message;

import com.advos.utils.Node;

import java.io.Serializable;

abstract public class Message implements Serializable {
    private final String msg;
    private final Node sourceNode;

    protected Message(String msg, Node sourceNode) {
        this.msg = msg;
        this.sourceNode = sourceNode;
    }

    public String getMsg() {
        return msg;
    }

    public Node getSourceNode() {
        return sourceNode;
    }

    @Override
    public String toString() {
        return "[from: Node-" + this.getSourceNode().getNodeInfo().getId() + "] " + this.getMsg();
    }
}
