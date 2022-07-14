import ext.log
import server.WatchTogetherServer

fun main(args: Array<String>) {
    log("Watch Together startup...")
    log("Program arguments: ${args.joinToString()}")
    WatchTogetherServer.start()
}