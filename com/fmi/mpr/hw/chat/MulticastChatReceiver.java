package com.fmi.mpr.hw.chat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.*;

public class MulticastChatReceiver implements Runnable{
    MulticastSocket socketIn;
    DatagramSocket socketOut;

    public MulticastChatReceiver(MulticastSocket socketIn, DatagramSocket socketOut) {
        this.socketIn = socketIn;
        this.socketOut = socketOut;
    }

    public void receiveUDP(String ip, int port) throws IOException {

        byte[] buffer = new byte[1024];
        InetAddress group = InetAddress.getByName(ip);
        socketIn.joinGroup(group);
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        while(!socketIn.isClosed()) {
            socketIn.receive(packet);
            String msg;
            msg = new String(packet.getData(), packet.getOffset(), packet.getLength());
            String msgCmdPart;
            msgCmdPart = (msg.indexOf(' ') != -1) ? msg.substring(0, msg.indexOf(' ')) : msg;
            msg = msg.substring(msg.indexOf(' ') + 1);
            if (packet.getPort() == socketOut.getLocalPort()) {
                //message from myself
                if ("_CLOSE_SOCKET".equals(msgCmdPart)) {
                    socketIn.close();
                    socketOut.close();
                }
                else if ("_TEXT".equals(msgCmdPart)) {
                    System.out.println("You: " + msg);
                }
            }
            else {
                //message from other user
                if ("_CLOSE_SOCKET".equals(msgCmdPart))
                    continue;
                else if ("_TEXT".equals(msgCmdPart))
                    System.out.println(packet.getSocketAddress().toString() + ": " + msg);
                else if ("_FILE".equals(msgCmdPart)) {
                    String fileNameAndExt = msg;
                    String fileName = fileNameAndExt.substring(0, fileNameAndExt.lastIndexOf('.'));
                    String extension = fileNameAndExt.substring(fileNameAndExt.lastIndexOf('.'));
                    fileName = socketOut.getLocalPort() + File.separator + fileName;
                    File receivingFile = new File(fileName + extension);
                    int i = 0;
                    while (receivingFile.exists()) {
                        i++;
                        receivingFile = new File(fileName + "(" + i + ")" + extension);
                    }
                    if (i > 0)
                        fileName = fileName + "(" + i + ")";
                    receivingFile.getParentFile().mkdirs();
                    receivingFile.createNewFile();
                    FileOutputStream receiving = new FileOutputStream(receivingFile);
                    receiveUDPFile(receiving);
                    System.out.println("received: " + fileName + extension);
                }
            }
        }
    }

    private void receiveUDPFile(FileOutputStream receiving) throws IOException {
        byte[] buffer = new byte[1024];
        String msg;
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        socketIn.receive(packet);
        msg = new String(packet.getData(), packet.getOffset(), packet.getLength());
        while (!"_END".equals(msg)) {
            receiving.write(packet.getData());
            socketIn.receive(packet);
            msg = new String(packet.getData(), packet.getOffset(), packet.getLength());
            receiving.flush();
        }
        receiving.close();
    }

    @Override
    public void run() {
        try {
            receiveUDP("230.0.0.1", 8888);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
