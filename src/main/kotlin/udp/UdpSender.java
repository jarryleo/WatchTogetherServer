package cn.leo.pycar.udp;

/**
 * @author : Jarry Leo
 * @date : 2019/1/25 13:48
 */
public interface UdpSender {
    /**
     * 设置发送目标地址
     *
     * @param host 目标地址
     */
    UdpSender setRemoteHost(String host);

    /**
     * 设置发送目标端口
     *
     * @param port 目标端口
     */
    UdpSender setPort(int port);

    /**
     * 设置包处理器
     *
     * @param packetProcessor 对发送数据进行分包处理
     */
    UdpSender setPacketProcessor(PacketProcessor packetProcessor);

    /**
     * 发送数据
     *
     * @param data 二进制数组
     */
    UdpSender send(byte[] data);

    /**
     * 发送广播
     *
     * @param data 对当前网段发送广播
     */
    UdpSender sendBroadcast(byte[] data);
}
