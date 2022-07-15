package server

import java.nio.channels.SocketChannel

/**
 * 房间信息
 */
data class Room(
    val id: String, //房间号，6位数
    var ownerChannel: SocketChannel? = null, //房间所有者
    var ownerLastHeartbeat:Long = System.currentTimeMillis(), //房主的最后心跳时间
    val clientSet: MutableSet<SocketChannel> = mutableSetOf(), //此房间的客户端
    var videoModel: VideoModel = VideoModel() //视频信息
)
