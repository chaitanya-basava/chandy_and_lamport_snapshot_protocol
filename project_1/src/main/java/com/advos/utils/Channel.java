package com.advos.utils;

import com.advos.MAPProtocol;
import com.advos.message.*;
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
    private final ObjectInputStream in;
    private final ObjectOutputStream out;
    private final Node node;
    private int neighbourId;

    public Channel(Socket socket, Node node, int neighbourId) {
        this.node = node;
        this.neighbourId = neighbourId;
        try {
            this.socket = socket;
            this.out = new ObjectOutputStream(socket.getOutputStream());
            this.in = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
        } catch (IOException e) {
            logger.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void receiveUrgentMessage() {
        while(true) {
            try {
                Message msg = (Message) this.in.readObject();
                if (msg instanceof ApplicationMessage) {
                    this.neighbourId = msg.getSourceNodeId();
                    break;
                }
            } catch (EOFException ignored) {
                MAPProtocol.sleep(100);
            }
            catch (IOException e) {
                logger.error(e.getMessage());
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void receiveMessage() {
        while (true) {
            try {
                Message msg = (Message) this.in.readObject();
                if(msg instanceof TerminationMessage) break;
                else if (msg instanceof ApplicationMessage) {
                    ApplicationMessage appMsg = (ApplicationMessage) msg;
                    this.node.receiveApplicationMessage(appMsg);
                } else if (msg instanceof MarkerMessage) {
                    MarkerMessage markerMsg = (MarkerMessage) msg;
                    this.node.receiveMarkerMessage(markerMsg);
                } else if (msg instanceof SnapshotMessage) {
                    SnapshotMessage snapshotMsg = (SnapshotMessage) msg;
                    this.node.receiveSnapshotMessage(snapshotMsg);
                }
            } catch (EOFException ignored) {
                MAPProtocol.sleep(500);
            }
            catch (IOException e) {
                logger.error(e.getMessage());
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void sendMessage(Message message) {
        try{
            this.out.writeObject(message);
            this.out.flush();
        } catch (IOException e) {
            logger.error("Error while receiving from " + this.neighbourId + ": " + e.getMessage());
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
