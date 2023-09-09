package com.advos.models;

import java.util.ArrayList;
import java.util.List;

public class NodeInfo {
    private final int id;
    private final String host;
    private final int port;
    List<Integer> neighbors = new ArrayList<>();
    List<NodeInfo> neighborNodesInfo = new ArrayList<>();

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

    public List<NodeInfo> getNeighborNodesInfo() {
        return neighborNodesInfo;
    }

    public void addNeighborsInfo(List<NodeInfo> neighborsInfo) {
        this.neighborNodesInfo = neighborsInfo;
    }

    public void addNeighbors(List<Integer> neighbors) {
        this.neighbors = neighbors;
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
