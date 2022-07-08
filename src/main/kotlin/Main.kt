import ext.log
import server.WatchTogetherServer

fun main(args: Array<String>) {
    log("Watch Together startup...")
    //println("Program arguments: ${args.joinToString()}")
    WatchTogetherServer.start()
    log("Watch Together start success!")
}