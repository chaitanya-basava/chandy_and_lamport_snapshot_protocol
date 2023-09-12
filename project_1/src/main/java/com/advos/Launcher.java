package com.advos;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Launcher {
    private static final Logger logger = LoggerFactory.getLogger(Launcher.class);

    private final MAPProtocol mapProtocol;
    private final ExecuteJar executeJar;

    public Launcher() {
        this.mapProtocol = new MAPProtocol();
        this.executeJar = new ExecuteJar();
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            logger.error("Please provide the fully qualified name of the main class.");
            System.exit(1);
        }

        Launcher launcher = new Launcher();

        String mainClassName = args[0];
        String[] mainArgs = new String[args.length - 1];
        System.arraycopy(args, 1, mainArgs, 0, mainArgs.length);

        if(mainClassName.equals("com.advos.MAPProtocol")) {
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logger.info("ctrl+c detected. cleaning up...");
                launcher.mapProtocol.cleanup();
            }));
            launcher.mapProtocol.execute(mainArgs);
        } else if(mainClassName.equals("com.advos.ExecuteJar")) {
            launcher.executeJar.execute(mainArgs);
        }
    }
}
