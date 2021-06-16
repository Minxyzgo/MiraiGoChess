import games.go.KatagoUtils

fun main() {
    KatagoUtils.initKatagoSituationAnalysis(
        "D:\\mirai-hello-world\\assets\\katago\\katago.exe",
        "D:\\mirai-hello-world\\assets\\katago\\analysis_example.cfg"
    )

    val json = KatagoUtils.analysis(
        listOf(intArrayOf(0, 0), intArrayOf(0, 1), intArrayOf(0, 2)),
        "foo",
        true
    )

    val json2 = KatagoUtils.analysis(
        listOf(intArrayOf(0, 0), intArrayOf(0, 1), intArrayOf(0, 2), intArrayOf(7, 9)),
        "foo",
        true
    )

    for(pair in json!!.map) {
        println("key: ${pair.key} value: ${pair.value}")
    }

    for(pair in json2!!.map) {
        println("key: ${pair.key} value: ${pair.value}")
    }

    KatagoUtils.writer.close()
    KatagoUtils.reader.close()
    KatagoUtils.process.destroy()

    println("end")
}