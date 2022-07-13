package udp;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 * @author : Jarry Leo
 * @date : 2019/1/25 13:17
 */
class UdpListenCore extends Thread {

    private final int port;
    private DatagramSocket receiveSocket;
    private OnDataArrivedListener onDataArrivedListener;
    private final PacketProcessor packetProcessor;

    private UdpSender sender;

    public void setOnDataArrivedListener(OnDataArrivedListener onDataArrivedListener) {
        this.onDataArrivedListener = onDataArrivedListener;
    }


    public UdpListenCore(int port, PacketProcessor packetProcessor) {
        this.port = port;
        this.packetProcessor = packetProcessor;
        start();
    }

    @Override
    public void run() {
        listen();
    }

    private void listen() {
        try {
            receiveSocket = new DatagramSocket(port);
            sender = new UdpSenderImpl(receiveSocket);
            byte[] bytes = new byte[packetProcessor.getPacketSize()];
            DatagramPacket packet = new DatagramPacket(bytes, packetProcessor.getPacketSize());
            for (; ; ) {
                receiveSocket.receive(packet);
                String host = packet.getAddress().getHostAddress();
                int port = packet.getPort();
                int length = packet.getLength();
                packetProcessor.mergePacket(bytes, length, host);
                if (packetProcessor.isMergeSuccess(host)) {
                    onDataArrivedListener.onDataArrived(packetProcessor.getMergedData(host), host, port);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取对应socket
     */
    public DatagramSocket getReceiveSocket() {
        return receiveSocket;
    }

    /**
     * 获取同端口发送器，同一个socket
     */
    public UdpSender getSender() {
        return sender;
    }

    /**
     * 关闭socket
     */
    public void close() {
        receiveSocket.close();
    }
}
