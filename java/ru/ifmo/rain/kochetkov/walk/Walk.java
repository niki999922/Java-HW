package ru.ifmo.rain.kochetkov.walk;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

public class Walk {
    private final Path inputPath;
    private final Path outputPath;

    public Walk(final String input, String output) throws WalkException {
        try {
            inputPath = Paths.get(input);
        } catch (InvalidPathException e) {
            throw new WalkException("Invalid path input file: " + e.getMessage());
        }
        try {
            outputPath = Paths.get(output);
        } catch (InvalidPathException e) {
            throw new WalkException("Invalid path output file: " + e.getMessage());
        }
        try {
            if (Files.notExists(outputPath)) {
                if (outputPath.getParent() != null) {
                    Files.createDirectories(outputPath.getParent());
                }
            }
        } catch (IOException e) {
            throw new WalkException("Can't create directories :" + outputPath.getParent().toString());
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
                    writer.write(String.format("%08x", hash) + " " + filePath + '\n');
                }
            } catch (FileNotFoundException e) {
                throw new WalkException("Wrong file writing, file not found: " + e.getMessage());
            } catch (SecurityException e) {
                throw new WalkException("Security exception, we haven't root: " + e.getMessage());
            } catch (UnsupportedEncodingException e) {
                throw new WalkException("Wrong encoding : " + e.getMessage());
            } catch (IllegalArgumentException e) {
                throw new WalkException("Illegal argument: " + e.getMessage());
            } catch (IOException e) {
                throw new WalkException("Wrong file writing: " + e.getMessage());
            }
        } catch (FileNotFoundException e) {
            throw new WalkException("File doesn't found : " + e.getMessage());
        } catch (SecurityException e) {
            throw new WalkException("Security exception, we haven't root: " + e.getMessage());
        } catch (NullPointerException e) {
            throw new WalkException("loc is null: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new WalkException("Illegal argument: buffer size <=0 or " + e.getMessage());
        } catch (IOException e) {
            throw new WalkException("Wrong file reading: " + e.getMessage());
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
