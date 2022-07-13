package udp;

import java.util.ArrayList;
import java.util.List;

/**
 * @author : Jarry Leo
 * @date : 2019/1/25 15:19
 */
class UdpListenEntity implements OnDataArrivedListener {
    private UdpListenCore udpListenCore;
    private List<OnDataArrivedListener> onDataArrivedListenerList = new ArrayList<>();

    public UdpListenEntity(UdpListenCore udpListenCore) {
        this.udpListenCore = udpListenCore;
        udpListenCore.setOnDataArrivedListener(this);
    }

    public void subscribeDataArrivedListener(OnDataArrivedListener onDataArrivedListener) {
        onDataArrivedListenerList.add(onDataArrivedListener);
    }

    public int unsubscribeDataArrivedListener(OnDataArrivedListener onDataArrivedListener) {
        onDataArrivedListenerList.remove(onDataArrivedListener);
        return onDataArrivedListenerList.size();
    }

    @Override
    public void onDataArrived(byte[] data, String host ,int port) {
        for (OnDataArrivedListener onDataArrivedListener : onDataArrivedListenerList) {
            onDataArrivedListener.onDataArrived(data, host, port);
        }
    }

    public UdpListenCore getUdpListenCore() {
        return udpListenCore;
    }

    /**
     * 关闭端口，并清理监听
     */
    public void close(){
        udpListenCore.close();
        onDataArrivedListenerList.clear();
    }
}
