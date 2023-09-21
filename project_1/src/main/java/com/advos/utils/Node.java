package com.advos.utils;

import com.advos.MAPProtocol;
import com.advos.message.ApplicationMessage;
import com.advos.message.Message;
import com.advos.models.Config;
import com.advos.models.NodeInfo;
import com.advos.state.LocalState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Node implements Serializable {
    private static final Logger logger = LoggerFactory.getLogger(Node.class);
    private transient final Config config;

    private final NodeInfo nodeInfo;
    private final LocalState localState;
    private transient final Map<String, Channel> inChannels = new HashMap<>();
    private transient final Map<Integer, Channel> outChannels = new HashMap<>();

    public Node(Config config, NodeInfo nodeInfo, boolean isActive) {
        this.config = config;
        this.nodeInfo = nodeInfo;
        this.localState = new LocalState(isActive, this.config);

        new Thread(this::startServer, "Socket Server Thread").start();

        this.startClient();
    }

    private void startServer() {
        try(ServerSocket server = new ServerSocket()) {
            server.setReuseAddress(true);
            server.bind(new InetSocketAddress(nodeInfo.getPort()));
            while(true) {
                Socket socket = server.accept();
                Channel channel = new Channel(socket, this, -1);
                String socketInfo = socket.getInetAddress().getCanonicalHostName() + ":" + socket.getPort();
                this.inChannels.put(socketInfo, channel);

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
                Channel channel = new Channel(socket, this, neighbours.get(idx).getId());
                this.outChannels.put(neighbours.get(idx).getId(), channel);
                logger.info("Connected to " + socket.getInetAddress().getHostAddress() + ":" + socket.getPort());
                idx++;
            } catch (IOException e) {
                logger.error("Couldn't connect to " + neighbours.get(idx).getHost() + ":" + neighbours.get(idx).getPort());
                MAPProtocol.sleep(500);
            }
        }

        logger.info("Connected to " + this.outChannels.size() + " channel(s)");
    }

    private int getRandomNeighbor() {
        List<NodeInfo> neighbors = this.nodeInfo.getNeighborNodesInfo();
        int idx = new Random().nextInt(neighbors.size());
        return neighbors.get(idx).getId();
    }

    private void send(int destId, Message message) {
        this.outChannels.get(destId).sendMessage(message);
    }

    public void sendApplicationMessages() {
        int numMessagesToSend = MAPProtocol.randomInRange(config.getMinPerActive(), config.getMaxPerActive());

        if(config.getMaxNumber() < this.getLocalState().getMessageCounter() + numMessagesToSend) {
            numMessagesToSend = config.getMaxNumber() - this.getLocalState().getMessageCounter();
        }

        try {
            for (int i = 0; i < numMessagesToSend; i++) {
                if(!this.localState.getIsActive() || this.localState.getMessageCounter() >= this.config.getMaxNumber()) break;

                int msgNumber = this.localState.incrementMessageCounter();
                this.localState.incrementVectorClockAti(this.nodeInfo.getId());
                int destId = this.getRandomNeighbor();
                Message message = new ApplicationMessage("Message " + msgNumber +
                        " from Node " + this.nodeInfo.getId() + " to Node " + destId,
                        this.localState.getVectorClock(), this.getNodeInfo().getId());
                this.send(destId, message);
                logger.info("Sent: " + message);

                MAPProtocol.sleep(config.getMinSendDelay());
            }
            this.getLocalState().setIsActive(false);
        } catch (Exception e) {
            logger.info(e.getMessage());
        }
    }

    public void receiveApplicationMessage(ApplicationMessage msg) {
        synchronized (this.localState) {
            for (int i = 0; i < this.localState.getClockSize(); i++) {
                this.localState.setVectorClockAti(i, Math.max(this.localState.getVectorClockAti(i), msg.getPiggybackedClockAti(i)));
            }
            this.localState.incrementVectorClockAti(this.nodeInfo.getId());

            logger.info("Received: " + msg + " [Vector Clock] " + this.localState.getVectorClock().toString() + "\n");

            if(this.localState.getMessageCounter() < this.config.getMaxNumber()) {
                this.localState.setIsActive(true);
                this.sendApplicationMessages();
            }
        }
    }

    @Override
    public String toString() {
        return (
                "[Node ID] " + this.nodeInfo.getId() + " is " +
                        (this.localState.getIsActive() ? "active" : "inactive") + "\n" + this.nodeInfo
        );
    }

    public NodeInfo getNodeInfo() {
        return this.nodeInfo;
    }

    public LocalState getLocalState() {
        return localState;
    }

    public void close() {
        this.outChannels.values().forEach(Channel::close);
        this.inChannels.values().forEach(Channel::close);
    }
}
