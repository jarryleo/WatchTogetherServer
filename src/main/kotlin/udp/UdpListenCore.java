package udp;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 * @author : Jarry Leo
 * @date : 2019/1/25 13:17
 */
class UdpListenCore extends Thread {

    private int port;
    private DatagramSocket receiveSocket;
    private OnDataArrivedListener onDataArrivedListener;
    private PacketProcessor packetProcessor;

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
            byte[] bytes = new byte[packetProcessor.getPacketSize()];
            DatagramPacket packet = new DatagramPacket(bytes, packetProcessor.getPacketSize());
            for (; ; ) {
                receiveSocket.receive(packet);
                String host = packet.getAddress().getHostAddress();
                int length = packet.getLength();
                packetProcessor.mergePacket(bytes, length, host);
                if (packetProcessor.isMergeSuccess(host)) {
                    onDataArrivedListener.onDataArrived(packetProcessor.getMergedData(host), host);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
