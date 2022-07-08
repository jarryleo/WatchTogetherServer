package server

/**
 * 房间信息
 */
data class Room(
    val id: String, //房间号，6位数
    val ownerHost: String, //房间所有者ip地址
    var ownerLastHeartbeat:Long = 0, //房主的最后心跳时间
    val clientSet: MutableSet<String> = mutableSetOf(), //所有进入这个房间的人
    var videoModel: VideoModel = VideoModel() //视频信息
)
