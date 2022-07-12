package udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;

/**
 * @author : Jarry Leo
 * @date : 2019/1/25 13:58
 */
class UdpSendCore {
    private DatagramSocket sendSocket;
    private InetSocketAddress address;
    private DatagramPacket datagramPacket;

    public UdpSendCore() {
        createSendSocket();
    }

    public UdpSendCore(DatagramSocket socket) {
        sendSocket = socket;
    }

    private void createSendSocket() {
        try {
            sendSocket = new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }


    public void sendData(byte[] data, String host, int port) {
        try {
            sendSocket.send(getDatagramPacket(data, host, port));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private DatagramPacket getDatagramPacket(byte[] data, String host, int port) {
        if (address == null) {
            createAddress(host, port);
        } else {
            if (!host.equals(address.getHostString()) || port != address.getPort()) {
                createAddress(host, port);
            }
        }
        if (datagramPacket == null) {
            datagramPacket = new DatagramPacket(data, data.length, address);
        } else {
            datagramPacket.setData(data);
            datagramPacket.setSocketAddress(address);
        }
        return datagramPacket;
    }

    private void createAddress(String host, int port) {
        address = new InetSocketAddress(host, port);
    }

}
