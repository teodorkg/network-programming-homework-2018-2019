package com.fmi.mpr.hw.chat;

import java.io.IOException;
import java.net.*;

public class MulticastChatReceiver implements Runnable{
    MulticastSocket socketIn;
    DatagramSocket socketOut;

    public MulticastChatReceiver(MulticastSocket socketIn, DatagramSocket socketOut) {
        this.socketIn = socketIn;
        this.socketOut = socketOut;
    }

    public void receiveUDPMessage(String ip, int port) throws IOException {

        byte[] buffer = new byte[1024];


        InetAddress group = InetAddress.getByName(ip);
        socketIn.joinGroup(group);
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        while(!socketIn.isClosed()) {
            socketIn.receive(packet);
            String msg;
            msg = new String(packet.getData(), packet.getOffset(), packet.getLength());
            if (packet.getPort() == socketOut.getLocalPort()) {
                if ("CLOSE_SOCKET".equals(msg)) {
                    socketIn.close();
                    socketOut.close();
                }
                else
                    System.out.println("You: " + msg);
            }
            else {
                if ("CLOSE_SOCKET".equals(msg))
                    continue;
                msg = new String(packet.getData(), packet.getOffset(), packet.getLength());
                System.out.println(packet.getSocketAddress().toString() + ": " + msg);
            }
        }
    }

    @Override
    public void run() {
        try {
            receiveUDPMessage("230.0.0.1", 8888);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
