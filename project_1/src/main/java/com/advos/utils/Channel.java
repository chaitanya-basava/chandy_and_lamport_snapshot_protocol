package com.advos.utils;

import com.advos.MAPProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;

public class Channel {
    private static final Logger logger = LoggerFactory.getLogger(Channel.class);
    private final Socket socket;
    private final DataInputStream in;
    private final DataOutputStream out;
    private final Node node;

    public Channel(Socket socket, Node node) {
        this.node = node;
        try {
            this.socket = socket;
            this.out = new DataOutputStream(socket.getOutputStream());
            this.in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        } catch (IOException e) {
            logger.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void receiveMessage() {
        while (true) {
            try {
                String msg = this.in.readUTF();
                if(msg.equals("TERMINATE")) break;

                node.receiveApplicationMessage(msg);
            } catch (EOFException ignored) {
                MAPProtocol.sleep(1000);
            }
            catch (IOException e) {
                logger.error(e.getMessage());
            }
        }
    }

    public void sendMessage(String message) {
        try{
            this.out.writeUTF(message);
        } catch (IOException e) {
            logger.error(e.getMessage());
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
