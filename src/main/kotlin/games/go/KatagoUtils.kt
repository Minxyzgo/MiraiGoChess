package games.go

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.IOException

object KatagoUtils {
    lateinit var reader: BufferedReader
    lateinit var writer: BufferedWriter
    lateinit var process: Process
    var initialization = false

    /**
     * 初始化katago
     * @param katagoPath katago执行文件的绝对路径
     * @param configPath katago配置文件的绝对路径
     * @param modelPath katago训练文件绝对路径 默认为default_model.bin.gz
     * @param logAll 是否打印所有log
     */
    @JvmOverloads
    fun initKatagoSituationAnalysis(
        katagoPath: String,
        configPath: String,
        modelPath: String? = null,
        logAll: Boolean = true
    ) {
        val build = ProcessBuilder(
            mutableListOf(
                katagoPath,
                "analysis",
                "-config $configPath"
            ).apply {
                if(modelPath != null) add("-model $modelPath")
            }
        )
        build.directory(File("."))
        build.redirectErrorStream(true)
        process = build.start()
        reader = process.inputStream.bufferedReader()

        writer = process.outputStream.bufferedWriter()
        println(process.isAlive)

//        val thread = thread(start = true) {
//            try {
//                sleep(timeOut)
//                throw TimeoutException("katago start failed")
//            } catch(e: InterruptedException) {
//
//            }
//        }

        while(true) {
            if("Started" in reader.readLine().also {
                    if(logAll) println(it)
            }) {
                initialization = true
                //thread.interrupt()
                return
            }
        }
    }

    @Synchronized
    fun analysis(
        schedule: List<IntArray>,
        id: String,
        logAll: Boolean = false
    ): JsonObject? {
        if (!initialization) {
            println("分析失败。你必须先初始化katago")
            return null
        }

        val list = mutableListOf<Array<String>>()

        for (i in schedule.indices) {
            val vec = schedule[i]
            if (i == 0 || (i and 1) != 1) {
                list.add(arrayOf("B", "(${vec[0]},${vec[1]})"))
            } else {
                list.add(arrayOf("W", "(${vec[0]},${vec[1]})"))
            }
        }



        if (logAll) {
            for (str in list) {
                for (st in str) {
                    println(st)
                }
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

        try {
            writer.write((sendJson.toJsonString(false).replace("\n", "") + "\n").also { println("enter: $it") })
            writer.flush()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        var json: JsonObject? = null

        try {
            var read: String?
            while (true) {
                read = this@KatagoUtils.reader.readLine().also { println(it) }
                if (read != null) {
                    json = Parser.default().parse(StringBuilder(read)) as JsonObject
                    break
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

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
