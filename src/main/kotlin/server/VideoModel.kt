data class VideoModel(
    var action: String = "",//执行动作 url,play,pause,seek,heartbeat
    var isPlaying: Boolean = false,
    var position: Int = 0,
    var timestamp: Long = 0L,
    var url: String = ""
)