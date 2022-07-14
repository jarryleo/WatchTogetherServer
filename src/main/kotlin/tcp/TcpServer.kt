package tcp

import ext.log
import java.io.ByteArrayOutputStream
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.ServerSocketChannel
import java.nio.channels.SocketChannel
import java.util.concurrent.ConcurrentHashMap
import kotlin.properties.Delegates

class TcpServer : Thread(), TcpServerInterface {

    /**
     * 已连接客户端
     */
    private val clientMap = ConcurrentHashMap<String, SocketChannel>()

    /**
     * 服务器监听通道
     */
    private var serverChannel: ServerSocketChannel? = null

    /**
     * 服务器监听端口号
     */
    private var port by Delegates.notNull<Int>()

    /**
     * 事件回调
     */
    private var standbyEvent: StandbyEvent? = null
    private var acceptEvent: AcceptEvent? = null
    private var dataEvent: DataEvent? = null
    private var disconnectEvent: DisconnectEvent? = null
    private var errorEvent: ErrorEvent? = null

    override fun subscribe(
        port: Int,
        standbyEvent: StandbyEvent?,
        acceptEvent: AcceptEvent?,
        dataEvent: DataEvent?,
        disconnectEvent: DisconnectEvent?,
        errorEvent: ErrorEvent?
    ) {
        this.port = port
        this.standbyEvent = standbyEvent
        this.acceptEvent = acceptEvent
        this.dataEvent = dataEvent
        this.disconnectEvent = disconnectEvent
        this.errorEvent = errorEvent
        start()
    }

    override fun run() {
        try {
            createServer(port)
        } catch (e: Exception) {
            e.printStackTrace()
            errorEvent?.onError(e)
        }
    }

    private fun createServer(port: Int) {
        val serverSocketChannel = ServerSocketChannel.open()
        serverSocketChannel.bind(InetSocketAddress(port))
        //非阻塞模式
        serverSocketChannel.configureBlocking(false)
        //注册选择器
        val selector = Selector.open()
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT)
        serverChannel = serverSocketChannel
        standbyEvent?.standby()
        //遍历选择器事件
        while (selector.select() > 0) {
            val keys = selector.selectedKeys()
            val it = keys.iterator()
            while (it.hasNext()) {
                val key = it.next()
                it.remove()
                when {
                    key.isAcceptable -> accept(key)
                    key.isReadable -> receive(key)
                    key.isWritable -> send(key)
                    key.isValid -> {
                        valid(key)
                        continue
                    }
                    key.isConnectable -> {
                        log("Connectable!")
                    }
                }
            }
        }
    }

    /**
     * 接受客户端连接请求
     */
    private fun accept(key: SelectionKey) {
        val channel = (key.channel() as ServerSocketChannel)
        val socketChannel = channel.accept()
        val address = socketChannel.socket().inetAddress
        val port = socketChannel.socket().port
        val mapKey = "$address:$port"
        clientMap[mapKey] = socketChannel
        log("client accept ：$mapKey")
        socketChannel.configureBlocking(false)
        val buffer = ByteBuffer.allocate(1024)
        socketChannel.register(key.selector(), SelectionKey.OP_READ and SelectionKey.OP_WRITE, buffer)
        acceptEvent?.onAccept(socketChannel)
    }

    /**
     * 收到客户端发来的数据
     */
    private fun receive(key: SelectionKey) {
        val buffer = (key.attachment() as? ByteBuffer) ?: ByteBuffer.allocate(1024)
        val channel = key.channel() as? SocketChannel ?: return
        val byteArrayOutputStream = ByteArrayOutputStream()
        var readLength: Int
        while (channel.read(buffer).also { readLength = it } > 0) {
            buffer.flip()
            byteArrayOutputStream.write(buffer.array())
            buffer.clear()
            // 最后一包读取特殊处理,不然会一直等待读入
            if (readLength != buffer.capacity()) {
                break
            }
        }
        dataEvent?.onData(byteArrayOutputStream.toByteArray(),channel)
        byteArrayOutputStream.close()
    }


    private fun send(key: SelectionKey) {
        //val buffer = (key.attachment() as? ByteBuffer) ?: ByteBuffer.allocate(1024)
        val channel = key.channel() as? SocketChannel ?: return
        log("isWritable : ${channel.addressText()}")
        //buffer.flip()
        //channel.write(buffer)
    }

    /**
     * 连接失效，客户端断开连接等
     */
    private fun valid(key: SelectionKey) {
        val channel = key.channel() as? SocketChannel ?: return
        clientMap.remove(channel.addressText())
        disconnectEvent?.onDisconnect(channel)
    }

    /**
     * 关闭所有连接
     */
    override fun close() {
        try {
            serverChannel?.close()
            clientMap.values.forEach {
                it.close()
                disconnectEvent?.onDisconnect(it)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}