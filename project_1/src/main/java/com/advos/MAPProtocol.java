package com.advos;

import com.advos.models.Config;
import com.advos.utils.ConfigParser;
import com.advos.utils.Node;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

public class MAPProtocol {
    private static final Logger logger = LoggerFactory.getLogger(MAPProtocol.class);
    private Node node;

    private static CommandLine parseArgs(String[] args) {
        Options options = new Options();

        Option nodeId = new Option("id", "nodeId", true, "id of the node to be run");
        nodeId.setRequired(true);
        options.addOption(nodeId);

        Option configFile = new Option("c", "configFile", true, "config file path");
        configFile.setRequired(true);
        options.addOption(configFile);

        Option verbose = new Option("v", "verbose", false, "Program verbosity");
        options.addOption(verbose);

        Option isActive = new Option("a", "isActive", false, "Node active or not");
        options.addOption(isActive);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();

        try {
            return parser.parse(options, args);
        } catch (ParseException e) {
            logger.error(e.getMessage());
            formatter.printHelp("utility-name", options);
            System.exit(1);
        }
        return null;
    }

    public void execute(String[] args) {
        CommandLine cmd = MAPProtocol.parseArgs(args);
        boolean verbose = cmd.hasOption("v");
        boolean isActive = cmd.hasOption("a");
        int nodeId = Integer.parseInt(cmd.getOptionValue("nodeId"));
        String configFile = cmd.getOptionValue("configFile");

        ConfigParser configParser = new ConfigParser(verbose);
        try {
            configParser.parseConfig(configFile);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        Config config = configParser.getConfig();

        this.node = new Node(config, config.getNode(nodeId), isActive);

        logger.info("node info with id: {}\n{}", nodeId, node);

        while (true) {
            if(node.getIsActive()) {
                int numMessagesToSend = randomInRange(config.getMinPerActive(), config.getMaxPerActive());

                if(config.getMaxNumber() < node.getMessageCounter() + numMessagesToSend) {
                    numMessagesToSend = config.getMaxNumber() - node.getMessageCounter();
                }

                try {
                    for (int i = 0; i < numMessagesToSend; i++) {
                        node.sendApplicationMessage();
                        sleep(config.getMinSendDelay());
                    }
                    node.setIsActive(false);
                } catch (Exception e) {
                    logger.info(e.getMessage());
                }
            }
        }
    }

    private static int randomInRange(int min, int max) {
        return new Random().nextInt(max - min + 1) + min;
    }

    public static void sleep(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void cleanup() {
        node.close();
    }
}
