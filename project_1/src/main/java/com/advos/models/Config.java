package com.advos.models;

import java.util.HashMap;
import java.util.Map;

public class Config {
    int n;
    int minPerActive;
    int maxPerActive;
    int minSendDelay;
    int snapshotDelay;
    int maxNumber;
    Map<Integer, NodeInfo> nodes = new HashMap<>();

    public Config(
            int n, int minPerActive,
            int maxPerActive, int minSendDelay,
            int snapshotDelay, int maxNumber
    ) {
        this.n = n;
        this.minPerActive = minPerActive;
        this.maxPerActive = maxPerActive;
        this.minSendDelay = minSendDelay;
        this.snapshotDelay = snapshotDelay;
        this.maxNumber = maxNumber;
    }

    public int getN() {
        return n;
    }

    public int getMinPerActive() {
        return minPerActive;
    }

    public int getMaxPerActive() {
        return maxPerActive;
    }

    public int getMinSendDelay() {
        return minSendDelay;
    }

    public int getSnapshotDelay() {
        return snapshotDelay;
    }

    public int getMaxNumber() {
        return maxNumber;
    }

    public NodeInfo getNode(int idx) {
        return nodes.get(idx);
    }

    public boolean checkNode(int idx) {
        return nodes.containsKey(idx);
    }

    public void setNode(int node, NodeInfo nodeIfo) {
        this.nodes.put(node, nodeIfo);
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();

        str
                .append("\nNodes: ").append(this.getN())
                .append("\nminPerActive: ").append(this.getMinPerActive())
                .append("\nmaxPerActive: ").append(this.getMaxPerActive())
                .append("\nminSendDelay: ").append(this.getMinSendDelay())
                .append("\nsnapshotDelay: ").append(this.getSnapshotDelay())
                .append("\nmaxNumber: ").append(this.getMaxNumber());

        for(Map.Entry<Integer, NodeInfo> entry: this.nodes.entrySet()) {
            str
                    .append("\n---------------Node: ")
                    .append(entry.getKey())
                    .append("---------------\n")
                    .append(entry.getValue());
        }

        return str.toString();
    }
}
