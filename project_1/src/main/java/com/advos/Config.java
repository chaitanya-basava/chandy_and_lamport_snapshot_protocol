package com.advos;

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

    public void setNode(int node, NodeInfo nodeIfo) {
        this.nodes.put(node, nodeIfo);
    }

    @Override
    public String toString() {
        for(Map.Entry<Integer, NodeInfo> entry: this.nodes.entrySet()) {
            System.out.println("---------------Node: " + entry.getKey() + "---------------");
            System.out.println(entry.getValue());
        }

        System.out.println("------------------------------------------------------------");

        return (
                "Nodes: " + this.n +
                        "\nminPerActive: " + this.minPerActive +
                        "\nmaxPerActive: " + this.maxPerActive +
                        "\nminSendDelay: " + this.minSendDelay +
                        "\nsnapshotDelay: " + this.snapshotDelay +
                        "\nmaxNumber: " + this.maxNumber
        );
    }
}
