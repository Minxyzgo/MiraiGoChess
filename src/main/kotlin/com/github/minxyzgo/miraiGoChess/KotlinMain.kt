@file:Suppress("RedundantSuspendModifier")

package com.github.minxyzgo.miraiGoChess

import com.github.minxyzgo.miraiGoChess.go.GoControl
import com.github.minxyzgo.miraiGoChess.go.KatagoUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.mamoe.mirai.console.command.CommandManager
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.console.permission.PermissionService
import net.mamoe.mirai.console.permission.PermissionService.Companion.hasPermission
import net.mamoe.mirai.console.permission.PermitteeId.Companion.permitteeId
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.message.data.PlainText
import java.io.File
import java.io.FileInputStream
import java.math.BigInteger
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import java.util.zip.ZipFile
import kotlin.io.path.Path
import kotlin.io.path.pathString

object KotlinMain : KotlinPlugin(
    JvmPluginDescription(
        id = "minxyzgo.GoChess",
        name = "mirai-go-chess",
        version = "0.1.0"
    )
) {
    private val allGoChess = mutableMapOf<Long, GoControl>()

    internal val enableKataGo get() = PluginConfig.kataGoPath.isNotBlank()
    internal val ACCESS_PERMISSION by lazy {
        PermissionService.INSTANCE.register(permissionId("enable"), "指定可使用围棋的群。若指定的是人，则他可以使用高级功能")
    }

    override fun onEnable() {
        ACCESS_PERMISSION

        PluginConfig.reload()

        CommandManager.registerCommand(KataGoQuickInstallCommand)

        if(enableKataGo) {
            KatagoUtils.initKatagoSituationAnalysis(
                PluginConfig.kataGoPath, PluginConfig.configPath, PluginConfig.modelPath.ifBlank { null })
            logger.info("katago成功启动")
        }

        globalEventChannel()
            .exceptionHandler { logger.error(it) }
            .subscribeAlways<GroupMessageEvent> {
                if(group.permitteeId.hasPermission(ACCESS_PERMISSION)) {
                    message.forEach {
                        if(it is PlainText && it.content.startsWith(".")) {
                            val goControl = allGoChess.getOrPut(group.id, ::GoControl)
                            goControl.main(it.content.removePrefix("."), group, sender)
                        }
                    }
                }
            }
    }
    override fun onDisable() {
        if(enableKataGo) {
            KatagoUtils.shutDown()

            for(chess in allGoChess.values) {
                chess.close()
            }
        }
    }

    object KataGoQuickInstallCommand : SimpleCommand(
        KotlinMain, "installKataGo",
        description = "快速安装kataGo。默认版本：1.4.5， 使用的网络：g170e-b20c256x2-s5303129600-d1228401921 (\"g170e 20 block d1228M\")"
    ) {
        @Handler
        suspend fun CommandSender.handle() {
            if(hasPermission(PermissionService.INSTANCE.rootPermission)) {
                val os = System.getProperty("os.name").lowercase()
                val isLinux = os.contains("linux")
                val isWindows = os.contains("windows")
                if(!isLinux && !isWindows) {
                    sendMessage("下载失败：不支持的系统。(仅支持linux windows) ")
                    return
                }

                val repositoryUrl = "https://github.com/lightvector/KataGo/releases/download/v1.4.5"
                val modelUrl = URL("$repositoryUrl/g170e-b20c256x2-s5303129600-d1228401921.bin.gz")
                val kataGoUrl = URL(
                    if(isLinux)
                        "$repositoryUrl/katago-v1.4.5-opencl-linux-x64.zip"
                    else "$repositoryUrl/katago-v1.4.5-opencl-windows-x64.zip"
                )

                val katago_windows_MD5 = "1f4554bd40abb7c78d2fffd837e6e7c0"
                val katago_linux_MD5 = "51ed11bffb388defc7c7c6bd106999b9"
                val model_MD5 = "d6db9e2d138e5d78d84b1f545aa89081"

                val modelFile = resolveDataFile("default_model.bin.gz")
                val kataGoZipFile = resolveDataFile("katago-v1.4.5-opencl-x64.zip")
                val kataGoDir = resolveDataFile("katago/")
                kataGoDir.mkdirs()

                suspend fun tryDownload(url: URL, name: String, target: File, md5: String) = withContext(Dispatchers.IO){
                    if(!target.exists()) {
                        target.createNewFile()
                    } else {
                        sendMessage("检测到 $name 已经存在")
                        val fileInputStream = FileInputStream(target)
                        val bytes = ByteArray(8192)
                        val digest = MessageDigest.getInstance("MD5")
                        var length: Int
                        while(fileInputStream.read(bytes).also { length = it } != -1) {
                            digest.update(bytes, 0, length)
                        }

                        fileInputStream.close()

                        val _md5 = BigInteger(1, digest.digest()).toString(16)
                        if(md5 == _md5) {
                            return@withContext
                        }
                        sendMessage("错误: 下载$name(MD5)应为$md5 但得到$_md5 开始重新下载")
                    }

                    val conn = url.openConnection() as HttpURLConnection
                    conn.connect()
                    val output = target.outputStream()
                    conn.inputStream.copyTo(output)
                    output.close()
                    conn.disconnect()
                }

                withContext(Dispatchers.IO) {
                    tryDownload(modelUrl, "katago model", modelFile, model_MD5)
                    tryDownload(
                        kataGoUrl,
                        "katago",
                        kataGoZipFile,
                        if(isLinux) katago_linux_MD5 else katago_windows_MD5
                    )

                    val zip = ZipFile(kataGoZipFile)
                    zip.entries().iterator().forEach {
                        val current = File(kataGoDir.absolutePath + "/" + it.name)
                        val input = zip.getInputStream(it)
                        if(!current.isDirectory) {
                            if(!current.exists()) current.createNewFile()
                            val output = current.outputStream()
                            input.copyTo(output)
                            output.close()
                            input.close()
                        }
                    }

                    PluginConfig.configPath = Path(kataGoDir.absolutePath + "/analysis_example.cfg").pathString
                    PluginConfig.kataGoPath = Path(kataGoDir.absolutePath + "/katago" + if(isLinux) "" else ".exe").pathString
                    PluginConfig.modelPath = Path(modelFile.absolutePath).pathString

                    sendMessage("下载成功！")

                    KatagoUtils.initKatagoSituationAnalysis(
                        PluginConfig.kataGoPath, PluginConfig.configPath, PluginConfig.modelPath.ifBlank { null })

                    sendMessage("katago启动成功")

                }
            }
        }
    }
}