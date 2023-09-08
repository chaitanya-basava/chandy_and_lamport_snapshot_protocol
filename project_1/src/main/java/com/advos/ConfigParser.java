package com.advos;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.advos.models.Config;
import com.advos.models.NodeInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigParser {
    private Config config = null;
    private final boolean verbose;
    private final boolean eVerbose;
    private static final Logger logger = LoggerFactory.getLogger(ConfigParser.class);

    public ConfigParser(boolean verbose, boolean eVerbose) {
        this.verbose = verbose;
        this.eVerbose = eVerbose;
    }

    public void parseConfig(String fileName) {
        String line;
        int lineCount = 0;
        Map<String, Integer> hostPortMap = new HashMap<>();

        try(BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            while((line = br.readLine()) != null) {
                line = line.trim();

                // check for begins with unsigned integer
                Pattern pattern = Pattern.compile("^\\d+");
                Matcher matcher = pattern.matcher(line);
                if (!matcher.find()) continue;

                // remove string after '#' (if any)
                line = line.split("#")[0].trim();

                // split the string delimited by space(s)
                String[] tokens = line.split("\\s+");
                List<String> validTokens = new ArrayList<>();
                for (String token : tokens) {
                    if (!token.isEmpty()) {
                        validTokens.add(token);
                    }
                }

                if (validTokens.isEmpty()) continue;

                if (lineCount == 0) {
                    // first valid line parsing logic (0)
                    try {
                        if(validTokens.size() != 6) {
                            throw new NumberFormatException("not valid first line, has only "
                                    + validTokens.size() + " numbers, need 6 elements");
                        }

                        this.config = new Config(
                                Integer.parseInt(validTokens.get(0)),
                                Integer.parseInt(validTokens.get(1)),
                                Integer.parseInt(validTokens.get(2)),
                                Integer.parseInt(validTokens.get(3)),
                                Integer.parseInt(validTokens.get(4)),
                                Integer.parseInt(validTokens.get(5))
                        );
                    } catch (NumberFormatException e) {
                        if(this.eVerbose) logger.error(e.getMessage());
                        continue;
                    }
                } else if (this.config != null && lineCount <= this.config.getN()) {
                    // next n valid lines parsing logic (1...n)

                    int nodeID;
                    int listenPort;
                    String hostName = validTokens.get(1);
                    try {
                        if(validTokens.size() != 3) {
                            throw new NumberFormatException("not valid line, need 3 elements, has "
                                    + validTokens.size());
                        }

                        nodeID = Integer.parseInt(validTokens.get(0));
                        listenPort = Integer.parseInt(validTokens.get(2));

                        if(this.config.checkNode(nodeID)) throw new Exception("node " + nodeID + " already added");
                        if(
                                hostPortMap.containsKey(hostName) &&
                                        hostPortMap.get(hostName) == listenPort
                        ) throw new Exception("host: " + hostName + " and port: " + listenPort + " already taken");
                    } catch (Exception e) {
                        if(this.eVerbose) logger.error(e.getMessage());
                        continue;
                    }

                    this.config.setNode(nodeID, new NodeInfo(nodeID, hostName, listenPort));
                    hostPortMap.put(hostName, listenPort);

                } else if (this.config != null && lineCount <= 2 * this.config.getN()) {
                    // last n valid lines parsing logic (n+1...2n)
                    int idx = lineCount - (this.config.getN() + 1);

                    List<Integer> validTokensInt = new ArrayList<>();
                    try {
                        for (String validToken: validTokens) {
                            int parsedToken = Integer.parseInt(validToken);
                            if(parsedToken == idx) {
                                throw new NumberFormatException("can't communicate with same process");
                            }
                            validTokensInt.add(parsedToken);
                        }
                    } catch (NumberFormatException e) {
                        if(this.eVerbose) logger.error(e.getMessage());
                        continue;
                    }

                    for (Integer validToken : validTokensInt) {
                        this.config.getNode(idx).addNeighbor(validToken);
                    }
                } else if (this.config != null && lineCount > 2 * this.config.getN()) break;

                lineCount++;
            }

            if(this.verbose) {
                logger.info("Parsed Config file is as follows:" + getConfig().toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Config getConfig() {
        return this.config;
    }

    public static void main(String[] args) {
        ConfigParser configParser = new ConfigParser(true, true);
        configParser.parseConfig(
                Objects.requireNonNull(ConfigParser.class.
                                getClassLoader().
                                getResource("config.txt")).
                        getPath()
        );
    }
}
