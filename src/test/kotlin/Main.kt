//import org.junit.jupiter.api.Test

import com.github.minxyzgo.miraiGoChess.KotlinMain
import net.mamoe.mirai.alsoLogin
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.plugin.PluginManager.INSTANCE.enable
import net.mamoe.mirai.console.plugin.PluginManager.INSTANCE.load
import net.mamoe.mirai.console.terminal.MiraiConsoleTerminalLoader
import net.mamoe.mirai.console.util.ConsoleExperimentalApi


@OptIn(ConsoleExperimentalApi::class)
suspend fun main() {

    MiraiConsoleTerminalLoader.startAsDaemon()

//    KotlinMain.enableKatago = true
//    KotlinMain.katagoPath = "D:\\mirai-hello-world\\assets\\katago\\katago.exe"
//    KotlinMain.configPath = "D:\\mirai-hello-world\\assets\\katago\\analysis_example.cfg"
//    KotlinMain.masterId = 123456789L
    KotlinMain.load()
    KotlinMain.enable()

    val bot = MiraiConsole.addBot(123456798L, "qwertyuiop") {
        fileBasedDeviceInfo()
    }.alsoLogin()

    MiraiConsole.job.join()


    //testImage()
}


fun testImage() {
    fun getVectorMar(index: Int) = ('A'.toInt() + index).toChar()
    println(getVectorMar(1))
}