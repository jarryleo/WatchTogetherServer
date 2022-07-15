package server

import ext.jsonToBean
import ext.log
import ext.toJson
import tcp.TcpServer
import tcp.addressText
import java.lang.Math.abs
import java.nio.ByteBuffer
import java.nio.channels.SocketChannel
import java.util.concurrent.ConcurrentHashMap

object WatchTogetherServer {

    //服务器监听端口
    private const val sPort = 51127

    //tcp 相关
    private val tcpServer = TcpServer()

    //房间信息(key 房间id，value 房间信息)
    private val roomMap = ConcurrentHashMap<String, Room>()

    /**
     * 开启服务
     */
    fun start() {
        tcpServer.subscribe(sPort,
            dataEvent = { data, channel ->
                val text = String(data)
                //log("receive ${channel.addressText()}: $json")
                text.split("\n").forEach { json ->
                    if (json.startsWith("{")) {
                        val bean = json.jsonToBean(VideoModel::class.java)
                        if (bean != null) {
                            dispatcher(bean, channel)
                        }
                    }
                }
            },
            standbyEvent = {
                log("Watch Together start success!")
            },
            disconnectEvent = {
                val key = it.addressText()
                log("$key disconnect")
            },
            errorEvent = {
                //it.printStackTrace()
                log("error : ${it.message}")
            }
        )
    }

    /**
     * 发送信息
     */
    private fun send(videoModel: VideoModel, channel: SocketChannel) {
        videoModel.isOwner = channel == videoModel.getRoom()?.ownerChannel
        val json = videoModel.toJson()
        channel.write(ByteBuffer.wrap(json.toByteArray()))
        //log("send to ${channel.addressText()} : $json")
    }

    /**
     * 事件分发
     */
    private fun dispatcher(model: VideoModel, channel: SocketChannel) {
        when (model.action) {
            "join" -> join(model, channel)
            "exit" -> exit(model, channel)
            "url", "play", "pause", "seek" -> changeState(model, channel)
            "sync" -> clientSync(model, channel)
            "heartbeat" -> heartbeat(model, channel)
        }
    }

    //通过视频信息查找房间
    private fun VideoModel.getRoom(): Room? {
        return roomMap[roomId]
    }

    /**
     * 加入或者创建房间
     */
    private fun join(model: VideoModel, channel: SocketChannel) {
        //检查现有房间信息，剔除失效房间
        checkRoom()
        //判断房间号是否合法
        val roomId = model.roomId
        if (roomId.isEmpty()) return
        //判断房间是否存在
        val room = model.getRoom()
        if (room == null) {
            // 不存在创建房间
            val r = Room(roomId, ownerChannel = channel)
            roomMap[roomId] = r
            r.videoModel.roomId = roomId
            r.videoModel.isOwner = true
            r.videoModel.timestamp = System.currentTimeMillis()
            log("${channel.addressText()} create room: $roomId")
            send(r.videoModel, channel)
            r.videoModel.action = "wait"
        } else {
            //房间存在
            if (room.ownerChannel != channel) {
                val current = System.currentTimeMillis()
                //房主
                if (model.isOwner &&
                    kotlin.math.abs(current - room.ownerLastHeartbeat) > 9){
                    room.ownerChannel = channel
                    room.videoModel.action = "join"
                    room.videoModel.isOwner = true
                    log("${channel.addressText()} reconnect room: $roomId")
                }else{
                    //观众
                    room.clientSet.add(channel)
                    room.videoModel.action = "join"
                    room.videoModel.isOwner = false
                    log("${channel.addressText()} join room: $roomId")
                }
                send(room.videoModel, channel)
            }else{
                room.videoModel.isOwner = true
                send(room.videoModel, channel)
            }
        }
    }

    /**
     * 退出房间
     */
    private fun exit(model: VideoModel, channel: SocketChannel) {
        //判断房间号是否合法
        val roomId = model.roomId
        if (roomId.isEmpty()) return
        //判断房间是否存在
        val room = model.getRoom()?:return
        //房主退出(解散房间)
        if (channel == room.ownerChannel){
            room.ownerLastHeartbeat = 0
            checkRoom()
        }else{
            //观众退出
            room.clientSet.remove(channel)
        }
        log("${channel.addressText()} exit room: $roomId")
    }
    /**
     * 房主改变播放状态，同步到所有客户端
     */
    private fun changeState(model: VideoModel, channel: SocketChannel) {
        val room = model.getRoom()
        if (room?.ownerChannel != channel) return
        room.videoModel = model
        room.clientSet.forEach { client ->
            send(model, client)
        }
    }

    /**
     * 客户端同步数据
     */
    private fun clientSync(model: VideoModel, channel: SocketChannel) {
        val room = model.getRoom()
        if (room == null || room.ownerChannel == channel) return
        room.videoModel.action = "sync"
        send(room.videoModel, channel)
    }

    /**
     * 房主心跳，记录时间，客户端心跳不理会
     */
    private fun heartbeat(model: VideoModel, channel: SocketChannel) {
        val room = model.getRoom()
        if (room?.ownerChannel == channel) {
            //url 数据一般很长，节省服务器带宽，心跳包不再携带url
            val url = room.videoModel.url
            if (model.url.isEmpty() && url.isNotEmpty()){
                model.url = url
            }
            room.ownerLastHeartbeat = System.currentTimeMillis()
            room.videoModel = model
        }
    }

    /**
     * 检查现有的房间，是否有房主超过120秒没心跳，有则销毁对应房间
     */
    private fun checkRoom() {
        val current = System.currentTimeMillis()
        val timeMap = mutableMapOf<String, Long>()
        roomMap.forEach { (roomId, room) ->
            timeMap[roomId] = room.ownerLastHeartbeat
        }
        timeMap.forEach { (roomId, time) ->
            if (current - time > 120 * 1000L) {
                try {
                    val room = roomMap.remove(roomId)
                    if (room != null) {
                        //关闭房主的连接
                        room.ownerChannel = null
                        //关闭所有客户端连接
                        room.clientSet.forEach {
                            room.videoModel.action = "exit"
                            send(room.videoModel,it)
                            it.close()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                log("destroy room: $roomId")
            }
        }
    }

}