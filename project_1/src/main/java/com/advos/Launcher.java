package com.advos;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class Launcher {
    private static final Logger logger = LoggerFactory.getLogger(Launcher.class);

    public static void main(String[] args) {
        if (args.length == 0) {
            logger.error("please specify the class to execute and its corresponding arguments");
            System.exit(1);
        }

        String mainClassName = args[0];
        String[] mainArgs = Arrays.copyOfRange(args, 1, args.length);

        if(mainClassName.equals("com.advos.MAPProtocol")) {
            MAPProtocol mapProtocol = new MAPProtocol(mainArgs);
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logger.info("ctrl+c detected. cleaning up...");
                mapProtocol.cleanup();
            }, "Shutdown Listener"));
            mapProtocol.execute();
        } else if(mainClassName.equals("com.advos.ExecuteJar")) {
            new ExecuteJar(mainArgs).execute();
        }
    }
}
