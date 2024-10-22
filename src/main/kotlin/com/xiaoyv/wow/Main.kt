package com.xiaoyv.wow

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.github.kwhat.jnativehook.GlobalScreen
import com.xiaoyv.wow.kts.dataDirOf
import com.xiaoyv.wow.listener.KeyCombinationListener

fun main() = application {
    val keyListener = remember { KeyCombinationListener() }

    LaunchedEffect(Unit) {
        System.setProperty("java.io.tmpdir", dataDirOf("tmp").absolutePath)

        GlobalScreen.registerNativeHook()
        GlobalScreen.addNativeKeyListener(keyListener)
    }

    val windowState = rememberWindowState()
    windowState.position = WindowPosition(Alignment.Center)

    Window(
        undecorated = false,
        title = "巫妖王挂机工具（QQ: 1223414335）",
        icon = painterResource("images/ic_logo.png"),
        state = windowState,
        onCloseRequest = ::exitApplication,
    ) {
        MainContent(keyListener = keyListener)
    }
}
