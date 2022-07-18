package tcp

import ext.log
import java.io.ByteArrayOutputStream
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.ServerSocketChannel
import java.nio.channels.SocketChannel
import kotlin.properties.Delegates

class TcpServer : Thread(), TcpServerInterface {

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
        try {
            //遍历选择器事件
            select(selector)
        } catch (e: Exception) {
            //e.printStackTrace()
            errorEvent?.onError(e)
            select(selector)
        }
    }

    private fun select(selector: Selector) {
        while (selector.select() > 0) {
            val keys = selector.selectedKeys()
            val it = keys.iterator()
            while (it.hasNext()) {
                val key = it.next()
                it.remove()
                if (!key.isValid) {
                    valid(key)
                    continue
                }
                if (key.isAcceptable) {
                    accept(key)
                }
                if (key.isReadable) {
                    receive(key)
                }
                /*if (key.isWritable) {
                    send(key)
                }*/
                /*if (key.isConnectable) {
                    log("Connectable!")
                }*/
            }
        }
    }

    /**
     * 接受客户端连接请求
     */
    private fun accept(key: SelectionKey) {
        try {
            val serverChannel = (key.channel() as? ServerSocketChannel) ?: return
            val channel = serverChannel.accept()
            val mapKey = channel.addressText()
            log("client accept ：$mapKey")
            channel.configureBlocking(false)
            val buffer = ByteBuffer.allocate(1024)
            channel.register(key.selector(), SelectionKey.OP_READ /*or SelectionKey.OP_WRITE*/, buffer)
            acceptEvent?.onAccept(channel)
        } catch (e: Exception) {
            errorEvent?.onError(e)
        }
    }

    /**
     * 收到客户端发来的数据
     */
    private fun receive(key: SelectionKey) {
        try {
            val buffer = (key.attachment() as? ByteBuffer) ?: ByteBuffer.allocate(1024)
            val channel = key.channel() as? SocketChannel ?: return
            val byteArrayOutputStream = ByteArrayOutputStream()
            var readLength: Int
            while (channel.read(buffer).also { readLength = it } > 0) {
                buffer.flip()
                byteArrayOutputStream.write(buffer.array(), buffer.arrayOffset(), buffer.limit())
                buffer.clear()
                // 最后一包读取特殊处理,不然会一直等待读入
                if (readLength != buffer.capacity()) {
                    break
                }
            }
            if (readLength == -1) {
                channel.close()
                disconnectEvent?.onDisconnect(channel)
                return
            }
            dataEvent?.onData(byteArrayOutputStream.toByteArray(), channel)
            byteArrayOutputStream.close()
        } catch (e: Exception) {
            errorEvent?.onError(e)
        }
    }


    /**
     * 通道可写，这个事件只要通道空闲就是一直可写，不太需要关注
     */
    private fun send(key: SelectionKey) {
        //val buffer = (key.attachment() as? ByteBuffer) ?: ByteBuffer.allocate(1024)
        //val channel = key.channel() as? SocketChannel ?: return
        //log("isWritable : ${channel.addressText()}")
    }

    /**
     * 连接失效，客户端断开连接等
     */
    private fun valid(key: SelectionKey) {
        try {
            val channel = key.channel() as? SocketChannel ?: return
            disconnectEvent?.onDisconnect(channel)
            channel.close()
            log("onDisconnect : ${channel.addressText()}")
        } catch (e: Exception) {
            errorEvent?.onError(e)
        }
    }

    /**
     * 关闭所有连接
     */
    override fun close() {
        try {
            serverChannel?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}