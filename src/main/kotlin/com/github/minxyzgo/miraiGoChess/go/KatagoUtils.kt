package com.github.minxyzgo.miraiGoChess.go

import com.github.minxyzgo.miraiGoChess.KotlinMain
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import com.github.minxyzgo.miraiGoChess.PluginConfig
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.util.concurrent.locks.ReentrantLock

object KatagoUtils {
    lateinit var reader: BufferedReader
    lateinit var writer: BufferedWriter
    lateinit var process: Process
    private var init = false
    private val lock = ReentrantLock()

    /**
     * 初始化katago
     * @param katagoPath katago执行文件的绝对路径
     * @param configPath katago配置文件的绝对路径
     * @param modelPath katago训练文件绝对路径 默认为default_model.bin.gz
     */
    @JvmOverloads
    fun initKatagoSituationAnalysis(
        katagoPath: String,
        configPath: String,
        modelPath: String? = null,
    ) {
        lock.lock()

        if(init) {
            reader.close()
            writer.close()
            process.destroy()
        }

        val build = ProcessBuilder(
            mutableListOf(
                katagoPath,
                "analysis",
                "-config $configPath",
                "-analysis-threads ${PluginConfig.analysisThreads}"
            ).apply {
                if(modelPath != null) add("-model $modelPath")
            }
        )
        build.directory(File("."))
        build.redirectErrorStream(true)
        process = build.start()
        reader = process.inputStream.bufferedReader()
        writer = process.outputStream.bufferedWriter()
        while(true) {
            if("Started" in reader.readLine().also {
                    KotlinMain.logger.debug(it)
            }) {
                init = true
                lock.unlock()
                return
            }
        }
    }

    fun analysis(
        schedule: List<IntArray>,
        id: String,
    ): JsonObject? {
        lock.lock()

        if(!init) {
            KotlinMain.logger.error("分析失败。你必须先初始化katago")
            return null
        }

        val list = mutableListOf<Array<String>>()
        for(i in schedule.indices) {
            val vec = schedule[i]
            if(i == 0 || (i and 1) != 1) {
                list.add(arrayOf("B", "(${vec[0]},${vec[1]})"))
            } else {
                list.add(arrayOf("W", "(${vec[0]},${vec[1]})"))
            }
        }

        val sendJson = JsonObject(
            mapOf(
                "id" to id,
                "komi" to 7.5,
                "boardXSize" to 19,
                "boardYSize" to 19,
                "rules" to "chinese",
                "moves" to list
            )
        )

        writer.write((sendJson.toJsonString(false).replace("\n", "") + "\n"))
        writer.flush()

        val json: JsonObject
        var read: String?
        while(true) {
            read = reader.readLine().also { KotlinMain.logger.info(it) }
            if(read != null) {
                json = Parser.default().parse(StringBuilder(read)) as JsonObject
                break
            }
        }

        lock.unlock()
        return json
    }

    /**
     * 释放katago资源
     */
    fun shutDown() {
        reader.close()
        writer.close()
        process.destroy()
    }
}
