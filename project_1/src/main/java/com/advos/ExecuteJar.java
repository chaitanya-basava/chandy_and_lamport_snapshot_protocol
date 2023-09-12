package com.advos;

import com.advos.models.Config;
import com.advos.models.NodeInfo;
import com.advos.utils.ConfigParser;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

public class ExecuteJar {
    private static final Logger logger = LoggerFactory.getLogger(ExecuteJar.class);

    private static String netid;
    private static String jarPath;
    private static String configFile;
    private static String configFileOnDC;
    private static String sshKeyFile;

    private static CommandLine parseArgs(String[] args) {
        Options options = new Options();

        Option configFile = new Option("c", "configFile", true, "config file path");
        configFile.setRequired(true);
        options.addOption(configFile);

        Option remoteConfigFile = new Option("rc", "remoteConfigFile", true, "config file path on dc machine");
        remoteConfigFile.setRequired(true);
        options.addOption(remoteConfigFile);

        Option netid = new Option("id", "netid", true, "netid to login to dc machines");
        netid.setRequired(true);
        options.addOption(netid);

        Option jarPath = new Option("jar", "jarPath", true, "jar file path");
        jarPath.setRequired(true);
        options.addOption(jarPath);

        Option sshKey = new Option("ssh", "sskKey", true, "ssh key path");
        options.addOption(sshKey);

        Option verbose = new Option("v", "verbose", false, "Program verbosity");
        options.addOption(verbose);

        Option runLocal = new Option("lo", "local", false, "Run program on local machine");
        options.addOption(runLocal);

        Option linux = new Option("l", "linux", false, "Run program on linux machine");
        options.addOption(linux);

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

    private static void executeBashCmd(
            String dcHost,
            int nodeId,
            boolean isActive,
            boolean local,
            boolean linux
    ) {
        String jarCommand = "java -jar " + jarPath + " com.advos.MAPProtocol -c " +
                (local ? configFile : configFileOnDC) + " -id " + nodeId;
        if(isActive) jarCommand += " -a";

        String sshCommand = "ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no " +
                "-i " + sshKeyFile + " " + netid + "@" + dcHost + " '" + jarCommand + "'";
        String bashCmd = local ? jarCommand : sshCommand;
        String[] cmd;

        if(linux) {
            cmd = new String[]{ "gnome-terminal", "-e", bashCmd };
        } else {
            String appleScriptCommand = "tell application \"Terminal\"\n" +
                    "    do script \"" + bashCmd + "\"\n" +
                    "end tell";
            cmd = new String[]{ "osascript", "-e", appleScriptCommand };
        }

        logger.info(Arrays.toString(cmd));

        try {
            Process process = Runtime.getRuntime().exec(cmd);

            int exitCode = process.waitFor();

            if (exitCode == 0) {
                logger.info("SSH command executed successfully.");
            } else {
                logger.error("SSH command failed with exit code: " + exitCode);
            }
        } catch (IOException | InterruptedException e) {
            logger.error(e.getMessage());
        }
    }

    public void execute(String[] args) {
        CommandLine cmd = ExecuteJar.parseArgs(args);
        boolean verbose = cmd.hasOption("v");
        boolean local = cmd.hasOption("lo");
        configFile = cmd.getOptionValue("configFile");

        ConfigParser configParser = new ConfigParser(verbose);
        try {
            configParser.parseConfig(configFile);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        Config config = configParser.getConfig();

        int n = config.getN();
        Random rand = new Random();
        int numActive = rand.nextInt(n) + 1;
        List<Integer> activeNodes = new ArrayList<>();

        for(int i = 0; i < numActive; i++) {
            int num = rand.nextInt(n);
            if(activeNodes.contains(num)) continue;
            activeNodes.add(num);
        }

        logger.info(activeNodes.toString());

        netid = cmd.getOptionValue("netid");
        jarPath = cmd.getOptionValue("jar");
        configFileOnDC = cmd.getOptionValue("remoteConfigFile");
        sshKeyFile = cmd.hasOption("ssh") ? cmd.getOptionValue("ssh") : "~/.ssh/id_rsa";

        ExecutorService executorService = Executors.newFixedThreadPool(n);

        for(NodeInfo nodeInfo: config.getNodes().values()) {
            int nodeId = nodeInfo.getId();
            String dcHost = nodeInfo.getHost();
            executorService.submit(() ->
                    executeBashCmd(dcHost, nodeId, activeNodes.contains(nodeId), local, cmd.hasOption("l")));
        }

        executorService.shutdown();
    }
}

