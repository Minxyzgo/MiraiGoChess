package com.github.minxyzgo.miraiGoChess.go

import java.awt.Color

enum class StoneColor {
    None, Black, White;

    val awt: Color by lazy {
        if (this == Black)
            Color.BLACK
        else
            Color.WHITE
    }
}