package dev.dhruva.jansevakendra

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "JanSevaKendra",
    ) {
        App()
    }
}