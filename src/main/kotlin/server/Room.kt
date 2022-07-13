package server

import java.net.InetSocketAddress

/**
 * 房间信息
 */
data class Room(
    val id: String, //房间号，6位数
    val ownerHost: String, //房间所有者ip地址
    var ownerPort: Int, //房间所有者端口
    var ownerLastHeartbeat:Long = System.currentTimeMillis(), //房主的最后心跳时间
    val clientSet: MutableSet<InetSocketAddress> = mutableSetOf(), //客户端地址
    var videoModel: VideoModel = VideoModel() //视频信息
)
