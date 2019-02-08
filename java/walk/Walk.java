package ru.ifmo.rain.kochetkov.walk;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Walk {
    private final Path inputPath;
    private final Path outputPath;

    public Walk(final String input, final String output) throws WalkException {
        try {
            inputPath = Paths.get(input);
            outputPath = Paths.get(output);
        } catch (InvalidPathException e) {
            throw new WalkException("Invalid path input or output file: " + e.getMessage());
        }
    }

    public void walk() throws WalkException {
        try (BufferedReader reader = Files.newBufferedReader(inputPath, StandardCharsets.UTF_8)) {
            try (BufferedWriter writer = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8)) {
                String filePath;
                while ((filePath = reader.readLine()) != null) {
                    int hash;
                    try {
                        hash = FNV.FNVHash(Paths.get(filePath));
                    } catch (InvalidPathException | IOException e) {
                        hash = 0;
                    }
                    PrintWriter printWriter = new PrintWriter(writer);
                    printWriter.printf("%08x %s\n", hash, filePath);
                }
            } catch (IOException e) {
                throw new WalkException("Wrong writing: " + e.getMessage());
            }
        } catch (IOException e) {
            throw new WalkException("Wrong reading: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        try {
            if (args == null || args.length != 2 || args[0] == null || args[1] == null) {
                throw new WalkException("Wrong arguments, expected : \"input file\" \"output file\"");
            }
            Walk walker = new Walk(args[0], args[1]);
            walker.walk();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}
