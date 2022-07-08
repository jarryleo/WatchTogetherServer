package cn.leo.pycar.udp;

import java.util.List;

/**
 * 数据包处理器
 *
 * @author : Jarry Leo
 * @date : 2019/1/25 13:27
 */
public abstract class PacketProcessor {
    private int packetSize = UdpConfig.DEFAULT_PACK_SIZE;

    public void setPacketSize(int packetSize) {
        this.packetSize = packetSize;
    }

    public int getPacketSize() {
        return packetSize;
    }

    /**
     * 分包发送
     *
     * @param data 数据包
     * @return 拆分后的数据包集合
     */
    public abstract List<byte[]> subPacket(byte[] data);

    /**
     * 合并数据包
     *
     * @param data 接收到的分包
     * @param host 接受的包的地址
     */
    public abstract void mergePacket(byte[] data,int length, String host);


    /**
     * 数据包是否合成完毕
     *
     * @param host 对应地址的数据包
     * @return true 合成完毕
     */
    public abstract boolean isMergeSuccess(String host);

    /**
     * 获取合成后的数据
     *
     * @param host 对应地址的数据包
     * @return 合成后的数据
     */
    public abstract byte[] getMergedData(String host);

}
