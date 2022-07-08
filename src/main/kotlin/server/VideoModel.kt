package server

data class VideoModel(
    var action: String = "idle",//执行动作 url,play,pause,seek,heartbeat,join,sync,idle,wait
    var isPlaying: Boolean = false,
    var position: Int = 0,
    var roomId: String = "000000", //房间id 6位int值
    var timestamp: Long = 0L,
    var url: String = ""
)