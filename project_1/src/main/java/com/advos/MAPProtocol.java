package com.advos;

import com.advos.models.Config;
import com.advos.state.GlobalState;
import com.advos.state.LocalState;
import com.advos.utils.ConfigParser;
import com.advos.utils.Node;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class MAPProtocol {
    private static final Logger logger = LoggerFactory.getLogger(MAPProtocol.class);
    private final Node node;
    private final int nodeId;
    private final Config config;
    private static final List<GlobalState> globalStates = new ArrayList<>();

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
    }

    private void buildSpanningTree() {
        boolean[] visited = new boolean[this.config.getN()];
        Queue<Integer> queue = new ArrayDeque<>();
        queue.add(Config.DEFAULT_SNAPSHOT_NODE_ID);
        visited[Config.DEFAULT_SNAPSHOT_NODE_ID] = true;
        this.config.getNode(Config.DEFAULT_SNAPSHOT_NODE_ID).setParentNodeId(-1);

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

    public static void addNodeGlobalState(GlobalState nodeGlobalState) {
        MAPProtocol.globalStates.add(nodeGlobalState);
    }

    public void execute() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Termination condition met, terminating node " + this.node.getNodeInfo().getId() + "!!!");
            this.cleanup();
            logger.info("\n");
            if(this.nodeId == Config.DEFAULT_SNAPSHOT_NODE_ID) MAPProtocol.validateSnapshots();
        }, "Shutdown Listener"));

        if(this.node.getLocalState().getIsActive()) {
            new Thread(this.node::sendApplicationMessages, "Application Initialization Thread").start();
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

    public synchronized static void validateSnapshots() {
        synchronized(MAPProtocol.class) {
            final List<List<Integer>> inconsistentSnapshots = new ArrayList<>();
            final int[] idx = new int[1];

            MAPProtocol.globalStates.forEach(globalState -> {
                for(int i = 0; i < globalState.getLocalStates().size(); i++) {
                    LocalState ithProcessLocalState = globalState.getLocalStateForNode(i);
                    for(int j = 0; j < globalState.getLocalStates().size(); j++) {
                        if(j == i) continue;
                        LocalState jthProcessLocalState = globalState.getLocalStateForNode(j);
                        if(ithProcessLocalState.getVectorClockAti(i) < jthProcessLocalState.getVectorClockAti(i)) {
                            logger.info("Snapshot [" + idx[0] + "] is not consistent at " + i + " and " + j);
                            inconsistentSnapshots.add(Arrays.asList(idx[0], i, j));
                            break;
                        }
                    }
                }
                idx[0]++;
            });

            if(inconsistentSnapshots.isEmpty()) {
                logger.info("All GlobalState snapshots are consistent");
            }
        }
    }

    public void cleanup() {
        node.close();
    }
}
