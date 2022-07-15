package server

data class VideoModel(
    //执行动作 [] 客户端发送专属信息 {} 服务端发送专属信息 ，其它为播放器通用信息
    // url,play,pause,seek, 播放器指令
    // [heartbeat 只有房主心跳同步播放状态,join 加入房间或者创建,sync 请求同步房主进度],
    // {idle 新房间,wait 等待房主开播}
    // exit 退出房间/解散房间
    var action: String = "idle",
    var isPlaying: Boolean = false,
    var isOwner: Boolean = false, //是否是房主，服务器回复给客户端信息
    var position: Int = 0,
    var roomId: String = "000000", //房间id 6位int值
    var timestamp: Long = 0L,
    var url: String = ""
)