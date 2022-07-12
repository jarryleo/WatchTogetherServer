package udp;

import java.net.DatagramSocket;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.List;

/**
 * @author : Jarry Leo
 * @date : 2019/1/25 13:55
 */
class UdpSenderImpl implements UdpSender {
    private String remoteHost = "127.0.0.1";
    private int port = UdpConfig.DEFAULT_LISTEN_PORT;
    private PacketProcessor packetProcessor = new DefaultPacketProcessor();
    private UdpSendCore udpSendCore ;
    private String broadcastHost = "255.255.255.255";

    public UdpSenderImpl() {
        udpSendCore = new UdpSendCore();
    }

    public UdpSenderImpl(DatagramSocket socket) {
        udpSendCore = new UdpSendCore(socket);
    }

    public UdpSenderImpl(String remoteHost, int port) {
        this.remoteHost = remoteHost;
        this.port = port;
    }

    public UdpSenderImpl(String remoteHost, int port, PacketProcessor packetProcessor) {
        this.remoteHost = remoteHost;
        this.port = port;
        this.packetProcessor = packetProcessor;
    }

    @Override
    public UdpSender setRemoteHost(String host) {
        remoteHost = host;
        return this;
    }

    @Override
    public UdpSender setPort(int port) {
        this.port = port;
        return this;
    }

    @Override
    public UdpSender setPacketProcessor(PacketProcessor packetProcessor) {
        this.packetProcessor = packetProcessor;
        return this;
    }

    @Override
    public UdpSender send(byte[] data) {
        List<byte[]> bytes = packetProcessor.subPacket(data);
        for (byte[] aByte : bytes) {
            udpSendCore.sendData(aByte, remoteHost, port);
        }
        return this;
    }

    @Override
    public UdpSender sendBroadcast(byte[] data) {
        if (broadcastHost == null) {
            getBroadcastHost();
        }
        List<byte[]> bytes = packetProcessor.subPacket(data);
        for (byte[] aByte : bytes) {
            udpSendCore.sendData(aByte, broadcastHost, port);
        }
        return this;
    }

    private void getBroadcastHost() {
        try {
            Enumeration nis = NetworkInterface.getNetworkInterfaces();
            InetAddress ia = null;
            while (nis.hasMoreElements()) {
                NetworkInterface ni = (NetworkInterface) nis.nextElement();
                Enumeration<InetAddress> ias = ni.getInetAddresses();
                while (ias.hasMoreElements()) {
                    ia = ias.nextElement();
                    if (ia instanceof Inet6Address) {
                        continue;// skip ipv6
                    }
                    String ip = ia.getHostAddress();
                    if (!"127.0.0.1".equals(ip)) {
                        byte[] bytes = ia.getAddress();
                        bytes[3] = (byte) 255;
                        broadcastHost = InetAddress.getByAddress(bytes).getHostAddress();
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
