package udp;

/**
 * @author : Jarry Leo
 * @date : 2019/1/25 13:29
 */
interface UdpConfig {
    int DEFAULT_PACK_SIZE = 1024;
    int DEFAULT_LISTEN_PORT = 37320;
    String HOST_FLAG = "host";
    String PORT_FLAG = "port";
    String DATA_FLAG = "data";
}
