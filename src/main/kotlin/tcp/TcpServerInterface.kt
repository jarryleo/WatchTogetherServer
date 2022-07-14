package tcp

import java.nio.channels.SocketChannel

/**
 * tcp订阅端口监听
 */
interface TcpServerInterface {
    /**
     * tcp 订阅端口
     */
    fun subscribe(
        port: Int,
        standbyEvent: StandbyEvent? = null,
        acceptEvent: AcceptEvent? = null,
        dataEvent: DataEvent?= null,
        disconnectEvent: DisconnectEvent?= null,
        errorEvent: ErrorEvent?= null
    )

    /**
     * 关闭tcp订阅
     */
    fun close()
}

/**
 * 新客户端接入
 */
fun interface AcceptEvent {
    fun onAccept(channel: SocketChannel)
}

/**
 * 收到客户端发来的数据
 */
fun interface DataEvent {
    fun onData(data: ByteArray, channel: SocketChannel)
}

/**
 * 客户端断开连接事件
 */
fun interface DisconnectEvent {
    fun onDisconnect(channel: SocketChannel)
}


/**
 * 服务端就绪
 */
fun interface StandbyEvent {
    fun standby()
}

/**
 * 错误事件
 */
fun interface ErrorEvent {
    fun onError(e: Exception)
}
