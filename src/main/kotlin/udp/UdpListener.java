package cn.leo.pycar.udp;

/**
 * @author : Jarry Leo
 * @date : 2019/1/25 13:40
 */
public interface UdpListener {

    /**
     * 订阅监听端口
     *
     * @param port                  端口
     * @param onDataArrivedListener 数据回调
     */
    void subscribe(int port, OnDataArrivedListener onDataArrivedListener);

    /**
     * 关闭端口
     *
     * @param port 要关闭的端口号
     */
    void closePort(int port);

    /**
     * 取消端口的订阅监听
     *
     * @param onDataArrivedListener
     */
    void unSubscribe(OnDataArrivedListener onDataArrivedListener);


}
