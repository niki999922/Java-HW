package ru.ifmo.rain.kochetkov.net;

import info.kgeorgiy.java.advanced.hello.HelloServer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;

import static java.lang.String.format;

/**
 * @author Kochetkov Nikita M3234
 * Date: 10.05.2019
 */
public class HelloUDPServer implements HelloServer {
    private DatagramSocket socket = null;
    private ExecutorService worker = null;
    private ExecutorService sender;

    @Override
    public void start(int port, int threads) {
        try {
            socket = new DatagramSocket(port);
            worker = Executors.newSingleThreadExecutor();
            sender = Executors.newFixedThreadPool(threads);
            worker.submit(() -> {
                try {
                    while (!socket.isClosed()) {
                        DatagramPacket request = new DatagramPacket(new byte[socket.getReceiveBufferSize()], socket.getReceiveBufferSize());
                        socket.receive(request);
                        sender.submit(() -> {
                            byte[] bytes = generateMessage(request.getData(), request.getOffset(), request.getLength()).getBytes(StandardCharsets.UTF_8);
                            try {
                                socket.send(new DatagramPacket(bytes, bytes.length, request.getSocketAddress()));
                            } catch (IOException e) {
                                System.err.println(e.getMessage());
                            }
                        });
                    }
                } catch (RejectedExecutionException ignored) {
                    System.err.println("Task cannot be accepted for execution.");
                } catch (SocketException ignored) {
                    System.err.println(format("Can't create connect to address %s.", socket.getInetAddress().getHostName()));
                } catch (IOException e) {
                    System.err.println(e.getMessage());
                }
            });
        } catch (SocketException e) {
            System.err.println("Can't create socket bounded to port №" + port);
        }
    }

    private String generateMessage(byte[] data, int offset, int length) {
        return "Hello, " + new String(data, offset, length, StandardCharsets.UTF_8);
    }

    @Override
    public void close() {
        worker.shutdown();
        try {
            worker.awaitTermination(4, TimeUnit.SECONDS);
        } catch (InterruptedException ignored) {}
        sender.shutdown();
        try {
            sender.awaitTermination(4, TimeUnit.SECONDS);
        } catch (InterruptedException ignored) {}
    }

    public static void main(String[] args) {
        if (args == null || args.length != 2 || args[0] == null || args[1] == null) {
            System.err.println("2 non-null arguments expected");
            return;
        }
        try {
            new HelloUDPServer().start(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
        } catch (NumberFormatException e) {
            System.err.println("Integer arguments expected");
        }
    }
}
