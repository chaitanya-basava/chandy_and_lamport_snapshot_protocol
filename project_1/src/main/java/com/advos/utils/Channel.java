package com.advos.utils;

import com.advos.MAPProtocol;
import com.advos.message.*;
import com.advos.models.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;

public class Channel {
    private static final Logger logger = LoggerFactory.getLogger(Channel.class);

    public Socket getSocket() {
        return socket;
    }

    private final Socket socket;
    private final DataInputStream in;
    private final DataOutputStream out;
    private final Node node;
    private int neighbourId;

    public Channel(Socket socket, Node node, int neighbourId) {
        this.node = node;
        this.neighbourId = neighbourId;
        try {
            this.socket = socket;
            this.out = new DataOutputStream(socket.getOutputStream());
            this.in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        } catch (IOException e) {
            logger.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public Channel(String hostname, int port, Node node, int neighbourId) throws IOException {
        this(new Socket(hostname, port), node, neighbourId);
    }

    public void receiveUrgentMessage() {
        StringBuilder msg = new StringBuilder();
        String line;
        while(true) {
            try {
                line = this.in.readUTF();
                msg.append(line);

                if(msg.toString().endsWith(Config.MESSAGE_DELIMITER) && (msg.toString().contains("[ApplicationMessage]"))) {
                    this.neighbourId = ApplicationMessage.deserialize(msg.toString()).getSourceNodeId();
                    break;
                }
            } catch (EOFException ignored) {
                MAPProtocol.sleep(100);
            }
            catch (IOException e) {
                logger.error(e.getMessage());
            }
        }
    }

    public void receiveMessage() {
        StringBuilder msg = new StringBuilder();
        String line;
        while (true) {
            try {
                line = this.in.readUTF();
                msg.append(line);

                if(msg.toString().endsWith(Config.MESSAGE_DELIMITER)) {
                    if (msg.toString().contains("[ApplicationMessage]")) {
                        ApplicationMessage appMsg = ApplicationMessage.deserialize(msg.toString());
                        if(appMsg.getMsg().isEmpty()) continue;
                        this.node.receiveApplicationMessage(appMsg);
                    } else if (msg.toString().contains("[MarkerMessage]")) {
                        MarkerMessage markerMsg = MarkerMessage.deserialize(msg.toString());
                        this.node.receiveMarkerMessage(markerMsg);
                    } else if (msg.toString().contains("[SnapshotMessage]")) {
                        SnapshotMessage snapshotMsg = SnapshotMessage.deserialize(msg.toString());
                        this.node.receiveSnapshotMessage(snapshotMsg);
                    } else if (msg.toString().contains("[TerminationMessage]")) {
                        this.node.propagateMessage(TerminationMessage.deserialize(msg.toString()));
                    }

                    msg = new StringBuilder();
                }
            } catch (IOException | ClassCastException e) {
                logger.error(e.getMessage());
                MAPProtocol.sleep(500);
            }
        }
    }

    public void sendMessage(Message message) {
        try{
            this.out.writeUTF(message.toString() + Config.MESSAGE_DELIMITER);
            this.out.flush();
        } catch (IOException e) {
            logger.error("Error while sending to " + this.neighbourId + ": " + e.getMessage());
        }
    }

    public int getNeighbourId() {
        return neighbourId;
    }

    public void close() {
        if(!socket.isClosed() && socket.isConnected()) {
            try {
                logger.info("Closing channel with " + socket.getInetAddress().getHostAddress() + ":" + socket.getPort());
                socket.close();
                in.close();
                out.close();
            } catch (IOException e) {
                logger.error(e.getMessage());
            }
        }
    }
}
