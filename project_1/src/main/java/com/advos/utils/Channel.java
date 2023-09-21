package com.advos.utils;

import com.advos.MAPProtocol;
import com.advos.message.ApplicationMessage;
import com.advos.message.Message;
import com.advos.message.TerminationMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;

public class Channel {
    private static final Logger logger = LoggerFactory.getLogger(Channel.class);
    private final Socket socket;
    private final ObjectInputStream in;
    private final ObjectOutputStream out;
    private final Node node;
    private final int neighbourId;

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

    public void receiveMessage() {
        while (true) {
            try {
                Message msg = (Message) this.in.readObject();
                if(msg instanceof TerminationMessage) break;
                else if (msg instanceof ApplicationMessage) {
                    ApplicationMessage appMsg = (ApplicationMessage) msg;
                    node.receiveApplicationMessage(appMsg);
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
