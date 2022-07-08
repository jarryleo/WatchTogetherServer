import udp.OnDataArrivedListener
import udp.UdpFrame
import udp.UdpListener
import java.nio.charset.Charset

object WatchTogetherServer {

    // 服务器监听端口
    private const val port = 51127

    val udp: UdpListener = UdpFrame.getListener()

    /**
     * 开启服务
     */
    fun start(){
        udp.subscribe(port,object :OnDataArrivedListener{
            override fun onDataArrived(data: ByteArray, host: String) {
                val json = String(data, Charset.defaultCharset())

            }
        })
    }

    private fun jsonToBean(){

    }

}