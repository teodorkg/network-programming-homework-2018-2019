package com.fmi.mpr.hw.chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.Scanner;

public class MulticastChatClient implements Runnable{
    private DatagramSocket socketOut;
    private MulticastSocket socketIn;
    private Thread receiveThread;
    private MulticastChatReceiver receiver;

    public MulticastChatClient() {
        try {
            socketOut = new DatagramSocket();
            socketIn = new MulticastSocket(8888);
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        receiver = new MulticastChatReceiver(socketIn, socketOut);
    }

    public static void main(String[] args) {
        MulticastChatClient chatClient = new MulticastChatClient();
        chatClient.run();
    }

    @Override
    public void run() {
        receiveThread = new Thread(receiver);
        receiveThread.start();
        try {
            handleSend("230.0.0.1", 8888);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleSend(String ip, int port) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String cmd;
        while ((cmd = reader.readLine()) != null) {
            if ("sendText".equalsIgnoreCase(cmd)) {
                sendUDPText(socketOut, "230.0.0.1", 8888);
            } else if ("sendImage".equalsIgnoreCase(cmd)) {

            } else if ("sendVideo".equalsIgnoreCase(cmd)) {

            } else if ("exit".equalsIgnoreCase(cmd) || socketOut.isClosed()) {
                if ("exit".equalsIgnoreCase(cmd))
                    sendCloseNotificationToChatReceiver("230.0.0.1", socketIn.getLocalPort());
                break;
            } else if (cmd.isEmpty()) {
                continue;
            } else {
                System.out.println("Wrong cmd");
            }
        }
    }

    private void sendCloseNotificationToChatReceiver(String ip, int port) throws IOException {
        byte[] msg = "CLOSE_SOCKET".getBytes();
        InetAddress to = InetAddress.getByName(ip);
        DatagramPacket packet = new DatagramPacket(msg, msg.length, to, port);
        socketOut.send(packet);
        try {
            receiveThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void sendUDPText(DatagramSocket socket, String ip, int port) throws IOException {
        Scanner sc = new Scanner(System.in);
        String line = sc.nextLine();

        InetAddress to = InetAddress.getByName(ip);
        byte[] msg = line.getBytes();

        DatagramPacket packet = new DatagramPacket(msg, msg.length, to, port);

        socket.send(packet);
    }
}
