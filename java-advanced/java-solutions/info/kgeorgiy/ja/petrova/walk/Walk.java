package info.kgeorgiy.ja.petrova.walk;

import java.io.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Walk {
    public static void main(String[] args) {
        if (args == null || args.length != 2 || args[0] == null || args[1] == null) {
            System.err.println("Incorrect arguments");
            return;
        }

        if (getPath(args[0]) || getPath(args[1]))
            return;

        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(
                        new FileInputStream(args[0]),
                        StandardCharsets.UTF_8
                ), 1000_000
        )) {
            //:note: don't use random consts
            try (BufferedWriter out = new BufferedWriter(
                    new OutputStreamWriter(
                            new FileOutputStream(args[1]), //омжно проще * invalid path указать кодировку
                            StandardCharsets.UTF_8
                    ), 1000_000
            )) {
                try {
                    MessageDigest md = MessageDigest.getInstance("SHA-256");
                    while (true) {
                        String line = in.readLine();
                        if (line == null) {
                            break;
                        }
                        //:note: ask os
                        if (System.getProperty("os.name").toLowerCase().contains("win") && line.contains("*")) {
                            out.write("0".repeat(64) + " " + line);
                            out.newLine();
                        } else {
                            try {
                                Path path = Path.of(line);
                                String hash = getHash(path.toFile(), md);
                                out.write(hash + " " + path);
                                out.newLine();
                            } catch (InvalidPathException e) {
                                //:note: "0".repeat(64)
                                out.write("0".repeat(64) + " " + line);
                                out.newLine();
                            }
                        }
                    }
                } catch (NoSuchAlgorithmException e) {
                    System.err.println("No such algorithm");
                }
            } catch (SecurityException e) {
                System.err.println("Security exception " + e.getMessage());
            } catch (IOException e) {
                System.err.println("An I/O error occurred: " + e.getMessage());
            }
        } catch (SecurityException e) {
            System.err.println("Security exception " + e.getMessage());
        } catch (IOException e) {
            System.err.println("An I/O error occurred: " + e.getMessage());
        } catch (NullPointerException e) {
            System.err.println("Empty file path");
        }
    }

    private static String getHash(File file, MessageDigest md) {
        try (FileInputStream inputStream = new FileInputStream(file)) {
            byte[] bytes = new byte[64];
            int pos = inputStream.read(bytes);
            while (pos != -1) {
                md.update(bytes, 0, pos);
                pos = inputStream.read(bytes);
            }
            final byte[] hash = md.digest();
            //:note: string.format
            //:note: BigInteger
            return String.format("%0" + (hash.length << 1) + "x", new BigInteger(1, hash));
        } catch (IOException | SecurityException e) {
            return "0".repeat(64);
        }
    }

    private static boolean getPath(String file) {
        try {
            Path path = Path.of(file);
            if (!path.toFile().exists()) {
                try {
                    System.out.println(path);
                    System.out.println(path.getParent());
                    Files.createDirectories(path.getParent());
                } catch (IOException | SecurityException e) {
                    System.err.println("Cannot create directories");
                }
            }
        } catch (InvalidPathException | NullPointerException e) {
            System.err.println("Invalid file");
            return true;
        } catch (SecurityException e) {
            System.err.println("Security exception");
            return true;
        }
        return false;
    }
}
