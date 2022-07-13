package server

import ext.jsonToBean
import ext.log
import ext.toJson
import udp.OnDataArrivedListener
import udp.UdpFrame
import udp.UdpListener
import udp.UdpSender
import java.net.InetSocketAddress
import java.util.concurrent.ConcurrentHashMap

object WatchTogetherServer {

    //服务器监听端口
    private const val sPort = 51127

    //udp 相关
    private val udpListener: UdpListener = UdpFrame.getListener()
    private var sender: UdpSender? = null

    //房间信息(key 房间id，value 房间信息)
    private val roomMap = ConcurrentHashMap<String, Room>()

    /**
     * 开启服务
     */
    fun start() {
        udpListener.subscribe(sPort, object : OnDataArrivedListener {
            override fun onDataArrived(data: ByteArray, host: String, port: Int) {
                val json = String(data)
                //log("receive $host:$port: $json")
                val bean = json.jsonToBean(VideoModel::class.java)
                if (bean != null) {
                    dispatcher(bean, host, port)
                }
            }
        })
    }

    /**
     * 发送信息
     */
    private fun send(videoModel: VideoModel, host: String, port: Int) {
        if (sender == null) {
            sender = UdpFrame.getSender(port)
        }
        videoModel.isOwner = host == videoModel.getRoom()?.ownerHost
        val json = videoModel.toJson()
        log("send to $host:$port : $json")
        sender?.setRemoteHost(host)
        sender?.setPort(port)
        sender?.send(json.toByteArray())
    }

    /**
     * 事件分发
     */
    private fun dispatcher(model: VideoModel, host: String, port: Int) {
        when (model.action) {
            "join" -> join(model, host, port)
            "url", "play", "pause", "seek" -> changeState(model, host)
            "sync" -> clientSync(model, host, port)
            "heartbeat" -> heartbeat(model, host, port)
        }
    }

    //通过视频信息查找房间
    private fun VideoModel.getRoom(): Room? {
        return roomMap[roomId]
    }

    /**
     * 加入或者创建房间
     */
    private fun join(model: VideoModel, host: String, port: Int) {
        //检查现有房间信息，剔除失效房间
        checkRoom()
        //判断房间号是否合法
        val roomId = model.roomId
        if (roomId.isEmpty()) return
        //判断房间是否存在
        val room = model.getRoom()
        if (room == null) {
            // 不存在创建房间
            val r = Room(roomId, host, port)
            roomMap[roomId] = r
            r.videoModel.roomId = roomId
            r.videoModel.timestamp = System.currentTimeMillis()
            log("$host:$port create room: $roomId")
            send(r.videoModel, host, port)
            r.videoModel.action = "wait"
        } else {
            //房间存在
            if (room.ownerHost != host) {
                //不是房主则加入客户端，是房主则同步房间信息
                val address = InetSocketAddress(host, port)
                room.clientSet.add(address)
                log("$host:$port join room: $roomId")
                room.videoModel.action = "join"
            }
            send(room.videoModel, host, port)
        }
    }

    /**
     * 房主改变播放状态，同步到所有客户端
     */
    private fun changeState(model: VideoModel, host: String) {
        val room = model.getRoom()
        if (room?.ownerHost != host) return
        room.videoModel = model
        room.clientSet.forEach {
            send(model, it.hostString, it.port)
        }
    }

    /**
     * 客户端同步数据
     */
    private fun clientSync(model: VideoModel, host: String, port: Int) {
        val room = model.getRoom()
        if (room == null || room.ownerHost == host) return
        room.videoModel.action = "sync"
        send(room.videoModel, host, port)
    }

    /**
     * 房主心跳，记录时间，客户端心跳不理会
     */
    private fun heartbeat(model: VideoModel, host: String, port :Int) {
        val room = model.getRoom()
        if (room?.ownerHost == host) {
            room.ownerLastHeartbeat = System.currentTimeMillis()
            room.videoModel = model
            room.ownerPort = port
        }else{
            val client = room?.clientSet?.find { it.hostString == host }
            if (client != null && client.port != port){
                room.clientSet.remove(client)
                room.clientSet.add(InetSocketAddress(host,port))
            }
        }
    }

    /**
     * 检查现有的房间，是否有房主超过30秒没心跳，有则销毁对应房间
     */
    private fun checkRoom() {
        val current = System.currentTimeMillis()
        val timeMap = mutableMapOf<String, Long>()
        roomMap.forEach { (roomId, room) ->
            timeMap[roomId] = room.ownerLastHeartbeat
        }
        timeMap.forEach { (roomId, time) ->
            if (current - time > 30 * 1000L) {
                roomMap.remove(roomId)
                log("destroy room: $roomId")
            }
        }
    }

}