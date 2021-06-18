package games.go

import net.mamoe.mirai.contact.Member


class Player(
    val color: StoneColor,
    val qqNumber: Long,

    //总之不太对劲
    val user: Member? = null
) {
    var regretChance = 3
}