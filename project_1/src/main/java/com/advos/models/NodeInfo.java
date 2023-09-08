package com.advos.models;

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

    public int getId() {
        return id;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public List<Integer> getNeighbors() {
        return neighbors;
    }

    public void addNeighbor(int neighbor) {
        this.neighbors.add(neighbor);
    }

    @Override
    public String toString() {
        return (
                "Node: " + this.getId() +
                        "\nHost: " + this.getHost() +
                        "\nPort: " + this.getPort() +
                        "\nNeighbors: " + this.getNeighbors().toString()
        );
    }
}
