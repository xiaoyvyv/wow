package com.xiaoyv.wow

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent
import com.xiaoyv.wow.kts.findWindow
import com.xiaoyv.wow.kts.formatHMS
import com.xiaoyv.wow.kts.maxWindow
import com.xiaoyv.wow.listener.KeyCombinationListener
import com.xiaoyv.wow.listener.OnCtrlAltPressedListener
import kotlinx.coroutines.launch
import java.awt.Toolkit

@Preview
@Composable
fun PreviewMainContent() {
    MainContent(keyListener = KeyCombinationListener())
}

@Composable
fun MainContent(keyListener: KeyCombinationListener) {
    val coroutineScope = rememberCoroutineScope()

    val viewModel by remember { mutableStateOf(MainViewModel()) }
    val suspendState by viewModel.suspendState.collectAsState()
    val suspendTime by viewModel.suspendTime.collectAsState()
    val logText by viewModel.logText.collectAsState()

    val logScrollState = rememberScrollState()

    LaunchedEffect(logText) {
        coroutineScope.launch {
            logScrollState.animateScrollTo(logScrollState.maxValue)
        }
    }


    keyListener.addCtrlAltPressedListener(object : OnCtrlAltPressedListener {
        override fun onCtrlAltPressed(keyEvent: NativeKeyEvent) {
            when (keyEvent.keyCode) {
                // 开关暂离功能
                NativeKeyEvent.VC_1 -> {
                    Toolkit.getDefaultToolkit().beep()
                    viewModel.toggleSuspendState()
                }
                // 截屏功能
                NativeKeyEvent.VC_2 -> {
                    Toolkit.getDefaultToolkit().beep()
                    viewModel.screenShots()
                }

                NativeKeyEvent.VC_3 -> {
                    Toolkit.getDefaultToolkit().beep()

//                    findWindow("战网").maxWindow()
                    findWindow("魔兽世界").maxWindow()
                }

                NativeKeyEvent.VC_4 -> System.err.println("Ctrl + Alt + 4 Pressed")
                NativeKeyEvent.VC_5 -> System.err.println("Ctrl + Alt + 5 Pressed")
                NativeKeyEvent.VC_6 -> System.err.println("Ctrl + Alt + 6 Pressed")
                NativeKeyEvent.VC_7 -> System.err.println("Ctrl + Alt + 7 Pressed")
                NativeKeyEvent.VC_8 -> System.err.println("Ctrl + Alt + 8 Pressed")
                NativeKeyEvent.VC_9 -> System.err.println("Ctrl + Alt + 9 Pressed")
                NativeKeyEvent.VC_0 -> System.err.println("Ctrl + Alt + 0 Pressed")
                else -> {}
            }
        }
    })

    MaterialTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = { TopAppBar(title = { Text("巫妖王挂机工具（小玉出品）") }) }
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Button(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = if (suspendState) Color.Red.copy(red = 0.75f) else Color.Green.copy(green = 0.75f),
                        contentColor = Color.White
                    ),
                    onClick = { viewModel.toggleSuspendState() }
                ) {
                    if (suspendState) {
                        Text("关闭暂离挂机（${suspendTime.formatHMS()}）")
                    } else {
                        Text("开启暂离挂机", fontWeight = FontWeight.Bold)
                    }
                }

                BasicTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 12.dp, end = 12.dp, bottom = 12.dp)
                        .background(
                            color = MaterialTheme.colors.onBackground.copy(alpha = 0.9f),
                            shape = MaterialTheme.shapes.medium
                        )
                        .padding(12.dp)
                        .verticalScroll(logScrollState),
                    value = logText,
                    textStyle = TextStyle.Default.copy(
                        color = MaterialTheme.colors.background,
                        lineHeight = 18.sp,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    ),
                    onValueChange = {

                    }
                )
            }
        }
    }
}