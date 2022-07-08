package cn.leo.pycar.udp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author : Jarry Leo
 * @date : 2019/1/25 13:27
 */
public class DefaultPacketProcessor extends PacketProcessor {

    private byte[] bytes;

    public DefaultPacketProcessor() {
        setPacketSize(60 * 1024);
    }

    @Override
    public List<byte[]> subPacket(byte[] data) {
        ArrayList<byte[]> subPacket = new ArrayList<>();
        subPacket.add(data);
        return subPacket;
    }

    @Override
    public void mergePacket(byte[] data, int length, String host) {
        bytes = Arrays.copyOf(data, length);
    }

    @Override
    public boolean isMergeSuccess(String host) {
        return true;
    }

    @Override
    public byte[] getMergedData(String host) {
        return bytes;
    }


}
