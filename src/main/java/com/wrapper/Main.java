package com.wrapper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {

    private static final String CONFIG_FILE = "jvm.config";

    public static void main(String[] args) {
        File configFile = new File(CONFIG_FILE);

        if (!configFile.exists()) {
            System.out.println(CONFIG_FILE + " not found. Creating default...");
            try {
                java.io.FileWriter writer = new java.io.FileWriter(configFile);
                writer.write("java -Xms128M -Xmx7168M -Dterminal.jline=false -Dterminal.ansi=true -jar server.jar");
                writer.close();
                System.out.println("Created " + CONFIG_FILE + " with default command.");
            } catch (IOException e) {
                System.err.println("Failed to create " + CONFIG_FILE + ": " + e.getMessage());
                System.exit(1);
            }
        }

        try {
            String commandLine = readConfigFile(configFile);
            if (commandLine == null || commandLine.trim().isEmpty()) {
                System.err.println("Error: " + CONFIG_FILE + " is empty.");
                System.exit(1);
            }

            List<String> commandArgs = parseCommand(commandLine);
            
            // If the user didn't specify 'java' at the start, we can optionally add it, 
            // but the user request specifically said "Startup Command like java ...", 
            // so we assume the full command is there. 
            // However, to be safe, if the first arg isn't a path to java, we might want to ensure we use the current JVM's java.
            // But let's trust the config file first. 
            // Actually, if they just put "-jar server.jar", we should prepend java.
            
            if (!commandArgs.isEmpty()) {
                String firstArg = commandArgs.get(0);
                if (!firstArg.endsWith("java") && !firstArg.endsWith("java.exe")) {
                     // Check if it looks like a java command or just args
                     // If it starts with -, it's definitely args.
                     if (firstArg.startsWith("-")) {
                         commandArgs.add(0, getCurrentJavaPath());
                     }
                }
            } else {
                 // Empty args?
                 System.err.println("Error: Could not parse command from " + CONFIG_FILE);
                 System.exit(1);
            }

            System.out.println("Launching: " + String.join(" ", commandArgs));

            ProcessBuilder pb = new ProcessBuilder(commandArgs);
            pb.inheritIO();
            Process process = pb.start();
            int exitCode = process.waitFor();
            System.exit(exitCode);

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static String readConfigFile(File file) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            // Read the first non-empty line
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty() && !line.startsWith("#")) {
                    return line;
                }
            }
        }
        return null;
    }

    private static List<String> parseCommand(String command) {
        List<String> tokens = new ArrayList<>();
        StringBuilder currentToken = new StringBuilder();
        boolean inQuotes = false;
        char quoteChar = 0;

        for (int i = 0; i < command.length(); i++) {
            char c = command.charAt(i);

            if (Character.isWhitespace(c)) {
                if (inQuotes) {
                    currentToken.append(c);
                } else {
                    if (currentToken.length() > 0) {
                        tokens.add(currentToken.toString());
                        currentToken.setLength(0);
                    }
                }
            } else if (c == '"' || c == '\'') {
                if (inQuotes && c == quoteChar) {
                    inQuotes = false;
                } else if (!inQuotes) {
                    inQuotes = true;
                    quoteChar = c;
                } else {
                    currentToken.append(c);
                }
            } else {
                currentToken.append(c);
            }
        }

        if (currentToken.length() > 0) {
            tokens.add(currentToken.toString());
        }

        return tokens;
    }

    private static String getCurrentJavaPath() {
        String javaHome = System.getProperty("java.home");
        String bin = javaHome + File.separator + "bin" + File.separator + "java";
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            bin += ".exe";
        }
        return bin;
    }
}
