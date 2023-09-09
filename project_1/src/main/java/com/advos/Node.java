package com.advos;

import com.advos.models.NodeInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Node {
    private static final Logger logger = LoggerFactory.getLogger(Node.class);
    private final int nodeId;
    private final NodeInfo nodeInfo;

    public Node(int nodeId, NodeInfo nodeInfo) throws InterruptedException {
        this.nodeId = nodeId;
        this.nodeInfo = nodeInfo;

        Thread.sleep(5000);
    }

    @Override
    public String toString() {
        return (
                "[Node] " + this.nodeId + "\n" +
                        this.nodeInfo.toString()
        );
    }
}
