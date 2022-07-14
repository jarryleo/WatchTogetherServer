package tcp

import java.nio.channels.SocketChannel


/**
 * 获取ip和端口，做为存储key
 */
fun SocketChannel.addressText(): String {
    val address = socket().inetAddress.hostName
    val port = socket().port
    return "$address:$port"
}