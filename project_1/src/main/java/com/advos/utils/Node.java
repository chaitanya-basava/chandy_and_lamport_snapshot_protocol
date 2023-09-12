package com.advos.utils;

import com.advos.MAPProtocol;
import com.advos.models.Config;
import com.advos.models.NodeInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Node {
    private static final Logger logger = LoggerFactory.getLogger(Node.class);
    private final Config config;
    private final NodeInfo nodeInfo;
    private final Map<Integer, Channel> channels = new HashMap<>();
    private final AtomicBoolean isActive = new AtomicBoolean(false);
    private final AtomicInteger messageCounter = new AtomicInteger(0);

    private final Thread serverThread;

    public Node(Config config, NodeInfo nodeInfo, boolean isActive) {
        this.config = config;
        this.nodeInfo = nodeInfo;
        this.isActive.set(isActive);

        this.serverThread = new Thread(this::startServer, "Socket Server Thread");
        this.serverThread.start();

        this.startClient();
    }

    private void startServer() {
        try(ServerSocket server = new ServerSocket()) {
            server.setReuseAddress(true);
            server.bind(new InetSocketAddress(nodeInfo.getPort()));
            while(true) {
                Socket socket = server.accept();
                Channel channel = new Channel(socket, this);
                String socketInfo = socket.getInetAddress().getCanonicalHostName() + ":" + socket.getPort();

                Thread msgListenerThread = new Thread(
                        channel::receiveMessage,
                        socketInfo + " Message Listener Thread"
                );
                msgListenerThread.start();

                logger.info("Received connection from " + socketInfo);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void startClient() {
        List<NodeInfo> neighbours = this.nodeInfo.getNeighborNodesInfo();
        int idx = 0;
        while(idx !=  neighbours.size()) {
            try {
                Socket socket = new Socket(neighbours.get(idx).getHost(), neighbours.get(idx).getPort());
                Channel channel = new Channel(socket, this);
                this.channels.put(neighbours.get(idx).getId(), channel);
                logger.info("Connected to " + socket.getInetAddress().getHostAddress() + ":" + socket.getPort());
                idx++;
            } catch (IOException e) {
                logger.error("Couldn't connect to " + neighbours.get(idx).getHost() + ":" + neighbours.get(idx).getPort());
                MAPProtocol.sleep(2000);
            }
        }

        logger.info("Connected to " + this.channels.size() + " channel(s)");
    }

    private int getRandomNeighbor() {
        List<NodeInfo> neighbors = this.nodeInfo.getNeighborNodesInfo();
        int idx = new Random().nextInt(neighbors.size());
        return neighbors.get(idx).getId();
    }

    public void sendApplicationMessage() {
        int destId = this.getRandomNeighbor();
        String message = "Message " + messageCounter + " from Node " + this.nodeInfo.getId() + " to Node " + destId;
        channels.get(destId).sendMessage(message);
        this.messageCounter.incrementAndGet();
        logger.info("Sent [Node ID " + destId + "]: " + message);
    }

    public void receiveApplicationMessage(String msg) {
        logger.info("Received: " + msg);
        if(this.messageCounter.get() < this.config.getMaxNumber()) this.setIsActive(true);
    }

    @Override
    public String toString() {
        return (
                "[Node ID] " + this.nodeInfo.getId() + " is " +
                        (this.getIsActive() ? "active" : "inactive") + "\n" + this.nodeInfo
        );
    }

    public boolean getIsActive() {
        return this.isActive.get();
    }

    public void setIsActive(boolean active) {
        this.isActive.set(active);
    }

    public int getMessageCounter() {
        return this.messageCounter.get();
    }

    public void close() {
        this.serverThread.interrupt();
        this.channels.values().forEach(Channel::close);
    }
}
