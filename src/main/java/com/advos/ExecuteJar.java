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

    private final String netid;
    private final String jarPath;
    private final String configFile;
    private final String configFileOnDC;
    private final String sshKeyFile;
    private final boolean local;
    private final boolean linux;
    private final Config config;

    private CommandLine parseArgs(String[] args) {
        Options options = new Options();

        Option configFileOption = new Option("c", "configFile", true, "config file path");
        configFileOption.setRequired(true);
        options.addOption(configFileOption);

        Option remoteConfigFileOption = new Option("rc", "remoteConfigFile", true, "config file path on dc machine");
        remoteConfigFileOption.setRequired(true);
        options.addOption(remoteConfigFileOption);

        Option netidOption = new Option("id", "netid", true, "netid to login to dc machines");
        netidOption.setRequired(true);
        options.addOption(netidOption);

        Option jarPathOption = new Option("jar", "jarPath", true, "jar file path");
        jarPathOption.setRequired(true);
        options.addOption(jarPathOption);

        Option sshKeyOption = new Option("ssh", "sskKey", true, "ssh key path");
        options.addOption(sshKeyOption);

        Option verboseOption = new Option("v", "verbose", false, "Program verbosity");
        options.addOption(verboseOption);

        Option runLocalOption = new Option("lo", "local", false, "Run program on local machine");
        options.addOption(runLocalOption);

        Option linuxOption = new Option("l", "linux", false, "Run program on linux machine");
        options.addOption(linuxOption);

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

    private void executeBashCmd(
            String dcHost,
            int nodeId,
            boolean isActive,
            boolean local,
            boolean linux
    ) {
        String jarCommand = "java -jar " + this.jarPath + " com.advos.MAPProtocol -c " +
                (local ? this.configFile : this.configFileOnDC) + " -id " + nodeId;
        if(isActive) jarCommand += " -a";

        String sshCommand = "ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no " +
                "-i " + this.sshKeyFile + " " + this.netid + "@" + dcHost + " '" + jarCommand + "'";
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
        } catch (IOException e) {
            logger.error(e.getMessage());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    
    ExecuteJar(String[] args) {
        CommandLine cmd = this.parseArgs(args);
        this.local = cmd.hasOption("lo");
        this.linux = System.getProperty("os.name").equalsIgnoreCase("linux");
        this.configFile = cmd.getOptionValue("configFile");

        ConfigParser configParser = new ConfigParser(cmd.hasOption("v"));
        try {
            configParser.parseConfig(this.configFile);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        this.config = configParser.getConfig();

        this.netid = cmd.getOptionValue("netid");
        this.jarPath = cmd.getOptionValue("jar");
        this.configFileOnDC = cmd.getOptionValue("remoteConfigFile");
        this.sshKeyFile = cmd.hasOption("ssh") ? cmd.getOptionValue("ssh") : "~/.ssh/id_rsa";
    }

    public void execute() {
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

        ExecutorService executorService = Executors.newFixedThreadPool(n);

        for(NodeInfo nodeInfo: this.config.getNodes().values()) {
            int nodeId = nodeInfo.getId();
            String dcHost = nodeInfo.getHost();
            executorService.submit(() ->
                    executeBashCmd(dcHost, nodeId, activeNodes.contains(nodeId), this.local, this.linux));
        }

        executorService.shutdown();
    }
}

