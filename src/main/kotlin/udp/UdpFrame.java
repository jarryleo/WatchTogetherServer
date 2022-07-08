package cn.leo.pycar.udp;

import java.util.HashMap;

/**
 * @author : Jarry Leo
 * @date : 2019/1/25 13:38
 */
public class UdpFrame implements UdpListener {
    private PacketProcessor packetProcessor;
    private static UdpSender udpSender = new UdpSenderImpl();
    private static HashMap<Integer, UdpListenEntity> subscribeMap = new HashMap<>();
    private static HashMap<OnDataArrivedListener, Integer> portMap = new HashMap<>();


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
        if (subscribeMap.containsKey(port)) {
            UdpListenEntity udpListenEntity = subscribeMap.get(port);
            udpListenEntity.subscribeDataArrivedListener(onDataArrivedListener);
        } else {
            UdpListenCore udpListenCore = new UdpListenCore(port, packetProcessor);
            UdpListenEntity udpListenEntity = new UdpListenEntity(udpListenCore);
            udpListenEntity.subscribeDataArrivedListener(onDataArrivedListener);
            subscribeMap.put(port, udpListenEntity);
        }
        portMap.put(onDataArrivedListener, port);
    }

    @Override
    public void closePort(int port) {
        udpSender.setPort(port);
        //todo 给自己的监听端口发送关闭协议
    }

    @Override
    public void unSubscribe(OnDataArrivedListener onDataArrivedListener) {
        Integer port = portMap.get(onDataArrivedListener);
        UdpListenEntity udpListenEntity = subscribeMap.get(port);
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
