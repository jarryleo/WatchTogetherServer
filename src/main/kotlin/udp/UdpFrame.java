package udp;

import java.util.HashMap;

/**
 * @author : Jarry Leo
 * @date : 2019/1/25 13:38
 */
public class UdpFrame implements UdpListener {
    private final PacketProcessor packetProcessor;
    private static final HashMap<Integer, UdpListenEntity> subscribeMap = new HashMap<>();
    private static final HashMap<OnDataArrivedListener, Integer> portMap = new HashMap<>();


    public UdpFrame(PacketProcessor packetProcessor) {
        this.packetProcessor = packetProcessor;
    }

    public static UdpListener getListener() {
        return new UdpFrame(new DefaultPacketProcessor());
    }

    public static UdpListener getListener(PacketProcessor packetProcessor) {
        return new UdpFrame(packetProcessor);
    }

    @Override
    public void subscribe(int port, OnDataArrivedListener onDataArrivedListener) {
        UdpListenEntity udpListenEntity;
        if (subscribeMap.containsKey(port)) {
            udpListenEntity = subscribeMap.get(port);
        } else {
            UdpListenCore udpListenCore = new UdpListenCore(port, packetProcessor);
            udpListenEntity = new UdpListenEntity(udpListenCore);
            subscribeMap.put(port, udpListenEntity);
        }
        udpListenEntity.subscribeDataArrivedListener(onDataArrivedListener);
        portMap.put(onDataArrivedListener, port);
    }

    @Override
    public void closePort(int port) {
        subscribeMap.get(port).close();
        subscribeMap.remove(port);
    }

    @Override
    public void unSubscribe(OnDataArrivedListener onDataArrivedListener) {
        Integer port = portMap.get(onDataArrivedListener);
        if (port == null) return;
        UdpListenEntity udpListenEntity = subscribeMap.get(port);
        if (udpListenEntity == null) return;
        int size = udpListenEntity.unsubscribeDataArrivedListener(onDataArrivedListener);
        if (size <= 0) {
            closePort(port);
            subscribeMap.remove(port);
            portMap.remove(onDataArrivedListener);
        }
    }

    public static UdpSender getSender() {
        return new UdpSenderImpl();
    }

    public static UdpSender getSender(int port) {
        UdpListenEntity udpListenEntity = subscribeMap.get(port);
        if (udpListenEntity != null) {
            return udpListenEntity.getUdpListenCore().getSender();
        }
        UdpSenderImpl udpSender = new UdpSenderImpl();
        udpSender.setPort(port);
        return udpSender;
    }

    public static UdpSender getSender(String host, int port) {
        return new UdpSenderImpl(host, port);
    }

    public static UdpSender getSender(String host, int port, PacketProcessor packetProcessor) {
        return new UdpSenderImpl(host, port, packetProcessor);
    }

    public static UdpSender getSender(PacketProcessor packetProcessor) {
        UdpSenderImpl udpSender = new UdpSenderImpl();
        udpSender.setPacketProcessor(packetProcessor);
        return udpSender;
    }
}
