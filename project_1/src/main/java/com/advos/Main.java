package com.advos;

import com.advos.models.Config;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

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

    public static void main(String[] args) throws Exception {
        CommandLine cmd = Main.parseArgs(args);
        boolean verbose = cmd.hasOption("v");
        int nodeId = Integer.parseInt(cmd.getOptionValue("nodeId"));
        String configFile = cmd.getOptionValue("configFile");

        ConfigParser configParser = new ConfigParser(verbose);
        configParser.parseConfig(configFile);
        Config config = configParser.getConfig();

        Node node = new Node(nodeId, config.getNode(nodeId));

        logger.info("node info with id: {}\n{}", nodeId, node);
    }
}
