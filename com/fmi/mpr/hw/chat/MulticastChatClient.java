package com.fmi.mpr.hw.chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Scanner;

import static com.fmi.mpr.hw.chat.Globals.*;

public class MulticastChatClient implements Runnable{
    private DatagramSocket socketOut;
    private MulticastSocket socketIn;
    private Thread receiveThread;
    private MulticastChatReceiver receiver;

    public MulticastChatClient() {
        try {
            socketOut = new DatagramSocket();
            socketIn = new MulticastSocket(PORT);
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
            handleSend();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleSend() throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String cmd;
        while ((cmd = reader.readLine()) != null) {
            if ("sendText".equalsIgnoreCase(cmd)) {
                sendUDPText(socketOut);
            } else if ("sendImage".equalsIgnoreCase(cmd) || "sendVideo".equalsIgnoreCase(cmd) || "sendFile".equalsIgnoreCase(cmd)) {
                sendUDPFile(socketOut);
            } else if ("exit".equalsIgnoreCase(cmd) || socketOut.isClosed()) {
                if ("exit".equalsIgnoreCase(cmd))
                    sendCloseNotificationToChatReceiver();
                break;
            } else if (cmd.isEmpty()) {
                continue;
            } else {
                System.out.println("Wrong cmd");
            }
        }
    }



    private void sendCloseNotificationToChatReceiver() throws IOException {
        byte[] msg = "_CLOSE_SOCKET".getBytes();
        InetAddress to = InetAddress.getByName(IP);
        DatagramPacket packet = new DatagramPacket(msg, msg.length, to, PORT);
        socketOut.send(packet);
        try {
            receiveThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void sendUDPText(DatagramSocket socketOut) throws IOException {
        Scanner sc = new Scanner(System.in);
        String line = sc.nextLine();

        InetAddress to = InetAddress.getByName(IP);
        byte[] msg = ("_TEXT " + line).getBytes();

        DatagramPacket packet = new DatagramPacket(msg, msg.length, to, PORT);

        socketOut.send(packet);
    }

    private void sendUDPFile(DatagramSocket socketOut) throws IOException {
        Scanner sc = new Scanner(System.in);
        String url = sc.nextLine();
        String fileName = url.substring(url.lastIndexOf("\\") + 1);
        InetAddress to = InetAddress.getByName(IP);

        byte[] msg = ("_TEXT sending " + fileName).getBytes();
        DatagramPacket packet = new DatagramPacket(msg, msg.length, to, PORT);
        socketOut.send(packet);

        msg = ("_FILE " + fileName).getBytes();
        packet = new DatagramPacket(msg, msg.length, to, PORT);
        socketOut.send(packet);

        int end;
        byte[] file = fileToBytes(url);
        if (file != null) {
            for (int offset = 0; offset < file.length; offset += PACKET_SIZE) {
                end = (file.length < offset + PACKET_SIZE) ? file.length : offset + PACKET_SIZE;
                byte[] data = Arrays.copyOfRange(file, offset, end);
                packet = new DatagramPacket(data, data.length, to, PORT);
                socketOut.send(packet);

            }
        }
        else {
            System.out.println("Read file problem");
        }

        msg = ("_END").getBytes();
        packet = new DatagramPacket(msg, msg.length, to, PORT);
        socketOut.send(packet);
    }

    private byte[] fileToBytes(String url) {
        byte[] bs;
        try {
            bs = Files.readAllBytes(Path.of(url));
        } catch (IOException e) {
            return  null;
        }
        return bs;
    }
}
