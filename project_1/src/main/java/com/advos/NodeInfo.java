package com.advos;

import java.util.ArrayList;
import java.util.List;

public class NodeInfo {
    int id;
    String host;
    int port;
    List<Integer> neighbors = new ArrayList<>();

    public NodeInfo(int id, String host, int port) {
        this.id = id;
        this.host = host;
        this.port = port;
    }

    public void addNeighbor(int neighbor) {
        this.neighbors.add(neighbor);
    }

    @Override
    public String toString() {
        return (
                "Node: " + this.id +
                        "\nHost: " + this.host +
                        "\nPort: " + this.port +
                        "\nNeighbors: " + this.neighbors.toString()
        );
    }
}
