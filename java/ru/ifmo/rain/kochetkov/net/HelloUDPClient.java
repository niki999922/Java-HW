package ru.ifmo.rain.kochetkov.net;

import info.kgeorgiy.java.advanced.hello.HelloClient;

import java.io.IOException;
import java.net.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static java.lang.String.format;

/**
 * @author Kochetkov Nikita M3234
 * Date: 10.05.2019
 */
public class HelloUDPClient implements HelloClient {
    public HelloUDPClient() {}

    public static void main(String[] args) throws Exception {
        if (args == null || args.length != 5 || args[0].isEmpty() || args[1].isEmpty() || args[2].isEmpty() || args[3].isEmpty() || args[4].isEmpty()) {
            System.err.println("Invalid arguments input");
            return;
        }
        new HelloUDPClient().run(args[0], Integer.parseInt(args[1]), args[2], Integer.parseInt(args[3]), Integer.parseInt(args[4]));
    }


    @Override
    public void run(String host, int port, String prefix, int threads, int requests) {
        InetSocketAddress address = new InetSocketAddress(host, port);
        ExecutorService worker = Executors.newFixedThreadPool(threads);
        for (int i = 0; i < threads; i++) {
            int finalI = i;
            worker.submit(() -> {
                try (DatagramSocket socket = new DatagramSocket()) {
                    socket.setSoTimeout(1000);
                    for (int j = 0; j < requests; j++) {
                        byte[] bytes = format("%s%d_%d", prefix, finalI, j).getBytes(StandardCharsets.UTF_8);
                        DatagramPacket request = new DatagramPacket(bytes, bytes.length, address);
                        send(socket, request);
                    }
                } catch (SocketException ignored) {
                    System.err.println("Fail of sending or receiving data to address: " + address);
                }
            });
        }
        worker.shutdown();
        try {
            worker.awaitTermination(10L * threads * requests, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            System.err.println(e.getMessage());
        }
    }

    private void send(DatagramSocket socket, DatagramPacket request) {
        while (!socket.isClosed()) {
            try {
                socket.send(request);
                DatagramPacket response = new DatagramPacket(new byte[socket.getReceiveBufferSize()], socket.getReceiveBufferSize());
                socket.receive(response);
                String message = getMessage(response.getData(), response.getOffset(), response.getLength());
                String expected = new String(request.getData());
                System.err.println(Charset.defaultCharset());
                System.err.println(message + " " + expected);
                if (message.contains(expected)) {
                    return;
                }
            } catch (SocketTimeoutException ignored) {
            } catch (SocketException ignored) {
                System.err.println("Can't create datagram socket with address " + socket.getInetAddress().getHostName());
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        }
    }

    private String getMessage(byte[] data, int offset, int length) {
        return new String(data, offset, length, StandardCharsets.UTF_8);
    }

}
