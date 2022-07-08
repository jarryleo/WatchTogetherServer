package server

import ext.jsonToBean
import ext.log
import ext.toJson
import udp.OnDataArrivedListener
import udp.UdpFrame
import udp.UdpListener
import udp.UdpSender
import java.util.concurrent.ConcurrentHashMap

object WatchTogetherServer {

    //服务器监听端口
    private const val port = 51127

    //udp 相关
    private val udpListener: UdpListener = UdpFrame.getListener()
    private val sender: UdpSender = UdpFrame.getSender()

    //房间信息(key 房间id，value 房间信息)
    private val roomMap = ConcurrentHashMap<String, Room>()

    /**
     * 开启服务
     */
    fun start() {
        udpListener.subscribe(port, object : OnDataArrivedListener {
            override fun onDataArrived(data: ByteArray, host: String) {
                val json = String(data)
                val bean = json.jsonToBean(VideoModel::class.java)
                if (bean != null) {
                    dispatcher(bean, host)
                }
            }
        })
    }

    /**
     * 发送视频信息
     */
    private fun send(videoModel: VideoModel, host: String) {
        sender.setRemoteHost(host)
        sender.send(videoModel.toJson().toByteArray())
    }

    /**
     * 事件分发
     */
    private fun dispatcher(model: VideoModel, host: String) {
        when (model.action) {
            "join" -> join(model, host)
            "url", "play", "pause", "seek" -> changeState(model, host)
            "sync" -> clientSync(model, host)
            "heartbeat" -> heartbeat(model, host)
        }
    }

    //通过视频信息查找房间
    private fun VideoModel.getRoom(): Room? {
        return roomMap[roomId]
    }

    /**
     * 加入或者创建房间
     */
    private fun join(model: VideoModel, host: String) {
        //检查现有房间信息，剔除失效房间
        checkRoom()
        //判断房间号是否合法
        val roomId = model.roomId
        if (roomId.isEmpty()) return
        //判断房间是否存在
        val room = model.getRoom()
        if (room == null) {
            // 不存在创建房间
            val r = Room(roomId, host)
            roomMap[roomId] = r
            log("$host create room: $roomId")
            send(r.videoModel, host)
            r.videoModel.action = "wait"
        } else {
            //房间存在
            if (room.ownerHost != host) {
                //不是房主则加入客户端，是房主则同步房间信息
                room.clientSet.add(host)
                log("$host join room: $roomId")
            }
            send(room.videoModel, host)
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
            send(model, it)
        }
    }

    /**
     * 客户端同步数据
     */
    private fun clientSync(model: VideoModel, host: String) {
        val room = model.getRoom()
        if (room?.ownerHost != host) return
        send(room.videoModel, host)
    }

    /**
     * 房主心跳，记录时间，客户端心跳不理会
     */
    private fun heartbeat(model: VideoModel, host: String) {
        val room = model.getRoom()
        if (room?.ownerHost == host) {
            room.ownerLastHeartbeat = System.currentTimeMillis()
            room.videoModel = model
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