package com.advos;

import com.advos.models.Config;
import com.advos.utils.ConfigParser;
import com.advos.utils.Node;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

public class MAPProtocol {
    private static final Logger logger = LoggerFactory.getLogger(MAPProtocol.class);
    private final Node node;
    private final int nodeId;
    private final Config config;

    private static CommandLine parseArgs(String[] args) {
        Options options = new Options();

        Option nodeIdOption = new Option("id", "nodeId", true, "id of the node to be run");
        nodeIdOption.setRequired(true);
        options.addOption(nodeIdOption);

        Option configFileOption = new Option("c", "configFile", true, "config file path");
        configFileOption.setRequired(true);
        options.addOption(configFileOption);

        Option verboseOption = new Option("v", "verbose", false, "Program verbosity");
        options.addOption(verboseOption);

        Option isActiveOption = new Option("a", "isActive", false, "Node active or not");
        options.addOption(isActiveOption);

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

    MAPProtocol(String[] args) {
        CommandLine cmd = MAPProtocol.parseArgs(args);
        boolean verbose = cmd.hasOption("v");
        boolean isActive = cmd.hasOption("a");
        this.nodeId = Integer.parseInt(cmd.getOptionValue("nodeId"));
        String configFile = cmd.getOptionValue("configFile");

        ConfigParser configParser = new ConfigParser(verbose);
        try {
            configParser.parseConfig(configFile);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        this.config = configParser.getConfig();
        buildSpanningTree();
        this.node = new Node(config, config.getNode(this.nodeId), isActive);
        MAPProtocol.sleep(Config.INIT_DELAY);
    }

    private void buildSpanningTree() {
        boolean[] visited = new boolean[this.config.getN()];
        Queue<Integer> queue = new LinkedList<>();
        queue.add(0);
        visited[0] = true;
        this.config.getNode(0).setParentNodeId(-1);

        while(!queue.isEmpty()) {
            int currentId = queue.poll();
            List<Integer> neighbours = this.config.getNode(currentId).getNeighbors();
            for(Integer neighbourId : neighbours) {
                if(!visited[neighbourId]) {
                    visited[neighbourId] = true;
                    queue.add(neighbourId);
                    this.config.getNode(neighbourId).setParentNodeId(currentId);
                }
            }
        }
    }

    public void execute() {
        logger.info("node info with id: {}\n{}", this.nodeId, node);
        if(this.node.getLocalState().getIsActive()) {
            this.node.sendApplicationMessages();
        }
    }

    public static int randomInRange(int min, int max) {
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
