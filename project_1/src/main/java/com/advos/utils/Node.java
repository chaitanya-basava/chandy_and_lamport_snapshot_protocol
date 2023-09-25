package com.advos.utils;

import com.advos.MAPProtocol;
import com.advos.message.*;
import com.advos.models.Config;
import com.advos.models.NodeInfo;
import com.advos.state.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.ArrayList;

public class Node {
    private static final Logger logger = LoggerFactory.getLogger(Node.class);
    private final Config config;
    private final NodeInfo nodeInfo;
    private final LocalState localState;
    private LocalState localStateSnapshot;
    private final List<ChannelState> channelStates;
    private final GlobalState globalState;
    private final Map<Integer, Channel> inChannels = new HashMap<>();
    private final Map<Integer, Channel> outChannels = new HashMap<>();
    private final Map<Integer, Boolean> receivedMarker = new HashMap<>();
    private int snapshotCount = 1;

    public Node(Config config, NodeInfo nodeInfo, boolean isActive) {
        this.config = config;
        this.nodeInfo = nodeInfo;
        this.localState = new LocalState(isActive, this.config);
        this.channelStates = new ArrayList<>();
        this.globalState = new GlobalState();

        this.localStateSnapshot = null;

        new Thread(this::startServer, "Socket Server Thread").start();
        this.startClient();

        MAPProtocol.sleep(Config.INIT_DELAY);
    }

    private void startServer() {
        try(ServerSocket server = new ServerSocket()) {
            server.setReuseAddress(true);
            server.bind(new InetSocketAddress(nodeInfo.getPort()));
            while(true) {
                Socket socket = server.accept();
                Channel channel = new Channel(socket, this, -1);
                channel.receiveUrgentMessage();
                this.receivedMarker.put(channel.getNeighbourId(), false);
                this.inChannels.put(channel.getNeighbourId(), channel);

                String socketInfo = channel.getSocket().getInetAddress().getCanonicalHostName() + ":" + channel.getSocket().getPort() + " [" + channel.getNeighbourId() + "]";
                new Thread(channel::receiveMessage, socketInfo + " Message Listener Thread").start();
                logger.info("Received connection from " + socketInfo);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void reinitializeSnapshotProcess() {
        synchronized (this) {
            // set all in-channels to false
            this.receivedMarker.replaceAll((i, v) -> false);
            synchronized(this.channelStates) {
                // clear channel states
                this.channelStates.clear();
            }
            // clear global state
            this.globalState.clear();
            this.localStateSnapshot = null;
        }

        if(this.getNodeInfo().getId() == Config.DEFAULT_SNAPSHOT_NODE_ID) {
            logger.info("initializing '" + snapshotCount++ + "' snapshot process");
            this.localState.setIsBlue(true);
            MAPProtocol.sleep(config.getSnapshotDelay());
            this.receiveMarkerMessage(new MarkerMessage(-1));
        }
    }

    private void recordGlobalState() {
        synchronized(this.globalState) {
            if(this.globalState.getLocalStates().size() == this.config.getN()) {
                // save global state to global states list
                MAPProtocol.addNodeGlobalState(this.globalState);
                new Thread(this::reinitializeSnapshotProcess, "Snapshot Re-Initialization Thread - Global snapshot recorded").start();
            }
        }
    }

    private boolean allMarkersReceived() {
        for(boolean received: this.receivedMarker.values()) {
            if(!received) return false;
        }
        return true;
    }

    private boolean createSocket(String hostname, int port, int neighbourId) {
        try {
            Socket socket = new Socket(hostname, port);
            Channel channel = new Channel(socket, this, neighbourId);
            this.outChannels.put(neighbourId, channel);
            channel.sendMessage(new ApplicationMessage("", this.localState.getVectorClock(), this.nodeInfo.getId()));
            logger.info("Connected to " + socket.getInetAddress().getHostAddress() + ":" + socket.getPort());
        } catch (IOException e) {
            logger.error("Couldn't connect to " + hostname + ":" + port);
            MAPProtocol.sleep(Config.RETRY_CLIENT_CONNECTION_DELAY);
            return false;
        }

        return true;
    }

    private void startClient() {
        List<NodeInfo> neighbours = this.nodeInfo.getNeighborNodesInfo();
        int idx = 0;
        while(idx != neighbours.size()) {
            NodeInfo neighbour = neighbours.get(idx);
            if(createSocket(neighbour.getHost(), neighbour.getPort(), neighbour.getId())) {
                idx++;
            }
        }

        // create a channel with parent node, if a channel doesn't exist [for sending snapshot messages]
        int parentId = this.getNodeInfo().getParentNodeId();
        while(parentId != -1 && !this.outChannels.containsKey(parentId)) {
            if(createSocket(config.getNode(parentId).getHost(), config.getNode(parentId).getPort(), parentId)) break;
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
        if(!this.getLocalState().getIsActive()) return;

        int numMessagesToSend = MAPProtocol.randomInRange(config.getMinPerActive(), config.getMaxPerActive());

        for (int i = 0; i < numMessagesToSend; i++) {
            int destId = this.getRandomNeighbor();
            synchronized(this.localState) {
                if (!this.localState.getIsActive() || this.localState.getMessageCounter() >= this.config.getMaxNumber())
                    break;
                int msgNumber = this.localState.incrementMessageCounter();
                this.localState.incrementVectorClockAti(this.nodeInfo.getId());
                Message message = new ApplicationMessage("Message " + msgNumber +
                        " from Node " + this.nodeInfo.getId() + " to Node " + destId,
                        this.localState.getVectorClock(), this.getNodeInfo().getId());
                this.send(destId, message);
                logger.info("Sent: " + message + " " + config.getMinSendDelay());
            }

            MAPProtocol.sleep(config.getMinSendDelay());
        }
        this.getLocalState().setIsActive(false);
    }

    private void sendSnapshotMessage(int originalId) {
        synchronized(this.channelStates) {
            SnapshotMessage snapshotMessage = new SnapshotMessage(this.getNodeInfo().getId(), originalId, this.localState, this.channelStates);
            // logger.info("Sending: " + snapshotMessage + " to " + this.getNodeInfo().getParentNodeId() + " color: " + this.localState.getIsBlue());
            this.send(this.getNodeInfo().getParentNodeId(), snapshotMessage);
        }
    }

    public void receiveApplicationMessage(ApplicationMessage msg) {
        synchronized(this.localState) {
            for (int i = 0; i < this.localState.getClockSize(); i++) {
                this.localState.setVectorClockAti(i, Math.max(this.localState.getVectorClockAti(i), msg.getPiggybackedClockAti(i)));
            }
            this.localState.incrementVectorClockAti(this.nodeInfo.getId());
            this.localState.incrementMessageReceiveCounter();

            logger.info("Received [Application" +
                    this.localState.getMessageReceiveCounter() + "]: " + msg +
                    " [Vector Clock] " + this.localState.getVectorClock().toString() + "\n");

            // record channel state if marker message already received by this node
            if(!this.localState.getIsBlue() && Boolean.TRUE.equals(!this.receivedMarker.get(msg.getSourceNodeId()))) {
                synchronized(this.channelStates) {
                    this.channelStates.add(new ChannelState(msg.getSourceNodeId(), this.getNodeInfo().getId(), msg));
                }
            }

            if(this.localState.getMessageCounter() < this.config.getMaxNumber() && !this.localState.getIsActive()) {
                this.localState.setIsActive(true);
                new Thread(
                        this::sendApplicationMessages, "Application messaging thread - " + msg.getSourceNodeId()
                ).start();
            }
        }
    }

    public void receiveMarkerMessage(MarkerMessage markerMessage) {
        while(this.receivedMarker.size() < this.nodeInfo.getNeighbors().size()) {
            MAPProtocol.sleep(Config.RETRY_CLIENT_CONNECTION_DELAY);
        }
        logger.info("Received [Marker]: " + markerMessage + " color: " + this.getLocalState().getIsBlue() + "\n");

        synchronized(this.localState) {
            if(this.localState.getIsBlue()) {
                this.localState.setIsBlue(false); // change to red

                if(this.localStateSnapshot == null) {
                    this.localStateSnapshot = new LocalState(
                            this.localState.getIsActive(),
                            this.localState.getIsBlue(),
                            this.localState.getMessageCounter(),
                            this.localState.getMessageReceiveCounter(),
                            this.localState.getVectorClock()
                    );
                }

                for (int neighbourId : this.nodeInfo.getNeighbors()) {
                    this.send(neighbourId, new MarkerMessage(this.getNodeInfo().getId()));
                }

                if(markerMessage.getSourceNodeId() != -1) {
                    this.receivedMarker.put(markerMessage.getSourceNodeId(), true);
                    if(this.allMarkersReceived() && !this.getLocalState().getIsBlue()) {
                        this.getLocalState().setIsBlue(true); // change back to blue
                        this.sendSnapshotMessage(this.getNodeInfo().getId());
                        new Thread(this::reinitializeSnapshotProcess, "Snapshot Cleanup Thread").start();
                    }
                }
            } else {
                this.receivedMarker.put(markerMessage.getSourceNodeId(), true);
                if (this.allMarkersReceived() && !this.getLocalState().getIsBlue()) {
                    this.localState.setIsBlue(true); // change to blue
                    if (this.getNodeInfo().getId() == Config.DEFAULT_SNAPSHOT_NODE_ID) {
                        synchronized (this.globalState) {
                            this.globalState.addLocalState(this.getNodeInfo().getId(), this.localStateSnapshot);
                            this.channelStates.forEach(this.globalState::addChannelState);
                        }
                        this.recordGlobalState();
                    } else {
                        this.sendSnapshotMessage(this.getNodeInfo().getId());
                        new Thread(this::reinitializeSnapshotProcess, "Snapshot Re-Initialization or Cleanup Thread").start();
                    }
                }
            }
        }
    }

    public void receiveSnapshotMessage(SnapshotMessage snapshotMessage) {
        logger.info("Received [Snapshot]: " + snapshotMessage + "\n");
        if(this.getNodeInfo().getId() == Config.DEFAULT_SNAPSHOT_NODE_ID) {
            synchronized(this.globalState) {
                this.globalState.addLocalState(snapshotMessage.getOriginalNodeId(), snapshotMessage.getLocalState());
                snapshotMessage.getChannelStates().forEach(this.globalState::addChannelState);
                this.recordGlobalState();
            }
        } else {
            this.sendSnapshotMessage(snapshotMessage.getOriginalNodeId());
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
