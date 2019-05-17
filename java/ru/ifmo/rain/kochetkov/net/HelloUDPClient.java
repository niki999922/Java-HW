package ru.ifmo.rain.kochetkov.net;

import info.kgeorgiy.java.advanced.hello.HelloClient;

import java.io.IOException;
import java.net.*;
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
    HelloUDPClient() {
    }

    public static void main(String[] args) throws Exception {
        if (args != null || args.length != 5 || args[0].isEmpty() || args[1].isEmpty() || args[2].isEmpty() || args[3].isEmpty() || args[4].isEmpty()) {
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
                    socket.setSoTimeout(100);
                    for (int j = 0; j < requests; j++) {
                        byte[] bytes = format("%s%d_%d", prefix, finalI, j).getBytes(StandardCharsets.UTF_8);
                        DatagramPacket request = new DatagramPacket(bytes, bytes.length, address);
                        retry(socket, request);
                    }
                } catch (SocketException ignored) {
                    System.err.println("Fail of sending or receiving data to address: " + address);
                }
            });
        }
        worker.shutdown();
        try {
            worker.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            System.err.println(e.getMessage());
        }
    }

    private void retry(DatagramSocket socket, DatagramPacket request) {
        while (!socket.isClosed()) {
            try {
                socket.send(request);
                DatagramPacket response = new DatagramPacket(new byte[socket.getReceiveBufferSize()], socket.getReceiveBufferSize());
                socket.receive(response);
                String message = new String(response.getData(), response.getOffset(), response.getLength(), StandardCharsets.UTF_8);
                String expected = new String(request.getData());
                if (message.contains(expected)) {
                    return;
                }
            } catch (SocketTimeoutException ignored) {
            } catch (SocketException ignored) {
                System.err.println("Unable to create a datagram socket with address " + socket.getInetAddress().getHostName());
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        }
    }

}
