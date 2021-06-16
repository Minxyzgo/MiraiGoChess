import net.mamoe.mirai.console.data.AutoSavePluginData
import net.mamoe.mirai.console.data.value

object BasePluginData: AutoSavePluginData("data") {
    val allGroup by value<MutableSet<Long>>()
}