package com.advos;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ConfigParser {
    private Config config = null;

    public void parseConfig(String fileName) {
        String line;
        int lineCount = 0;

        try(BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            while((line = br.readLine()) != null) {
                line = line.trim();

                if (line.startsWith("#") || line.isEmpty()) continue;

                line = line.split("#")[0].trim();

                String[] tokens = line.split("\\s+");
                List<String> validTokens = new ArrayList<>();
                for (String token : tokens) {
                    if (!token.isEmpty()) {
                        validTokens.add(token);
                    }
                }

                if (validTokens.isEmpty()) continue;

                if (lineCount == 0) {
                    this.config = new Config(
                            Integer.parseInt(validTokens.get(0)),
                            Integer.parseInt(validTokens.get(1)),
                            Integer.parseInt(validTokens.get(2)),
                            Integer.parseInt(validTokens.get(3)),
                            Integer.parseInt(validTokens.get(4)),
                            Integer.parseInt(validTokens.get(5))
                    );
                } else if (this.config != null && lineCount <= this.config.n) {
                    int nodeID = Integer.parseInt(validTokens.get(0));
                    String hostName = validTokens.get(1);
                    int listenPort = Integer.parseInt(validTokens.get(2));

                    this.config.setNode(nodeID, new NodeInfo(nodeID, hostName, listenPort));
                } else if (this.config != null && lineCount <= 2 * this.config.n) {
                    int idx = lineCount - (this.config.n + 1);

                    for (String validToken : validTokens) {
                        this.config.nodes.get(idx)
                                .addNeighbor(Integer.parseInt(validToken));
                    }
                } else if(this.config != null && lineCount > 2 * this.config.n) break;

                lineCount++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Config getConfig() {
        return config;
    }

    public static void main(String[] args) {
        ConfigParser configParser = new ConfigParser();
        configParser.parseConfig(
                Objects.requireNonNull(ConfigParser.class.
                                getClassLoader().
                                getResource("config.txt")).
                        getPath()
        );

        Config config = configParser.getConfig();
        System.out.println(config);
    }
}
