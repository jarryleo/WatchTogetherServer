package server

data class VideoModel(
    var action: String = Action.IDLE.cmd,
    var isPlaying: Boolean = false,
    var isOwner: Boolean = false, //是否是房主，服务器回复给客户端信息
    var position: Int = 0,
    var roomId: String = "", //房间id 6位int值字符串
    var timestamp: Long = 0L,
    var url: String = ""
){
    /**
     * 获取指令枚举
     */
    fun action() :Action? {
        return Action.findActionByCmd(action)
    }
}