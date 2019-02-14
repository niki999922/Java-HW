package ru.ifmo.rain.kochetkov.walk;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public final class FNV {
    private static final int FNV_32_INIT = 0x811c9dc5;
    private static final int FNV_32_PRIME = 0x01000193;
    private static final int BUFF_SIZE = 4096;

    private static int FNVHash(final byte[] bytes) {
        int hash = FNV_32_INIT;
        for (final byte b : bytes) {
            hash = (hash * FNV_32_PRIME) ^ (b & 0xff);
        }
        return hash;
    }

    static int FNVHash(Path inputPath) throws IOException {
        int hash = FNV_32_INIT;
        byte[] buff = new byte[BUFF_SIZE];
        InputStream reader = Files.newInputStream(inputPath);
        int c;
        while ((c = reader.read(buff)) > 0) {
            for (int i = 0; i < c; i++) {
                hash = (hash * FNV_32_PRIME) ^ (buff[i] & 0xff);
            }
        }
        return hash;
    }
}