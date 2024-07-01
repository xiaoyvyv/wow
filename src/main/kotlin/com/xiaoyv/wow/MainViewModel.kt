@file:OptIn(DelicateCoroutinesApi::class)

package com.xiaoyv.wow

import androidx.compose.ui.res.useResource
import com.xiaoyv.wow.kts.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.awt.Robot
import java.io.File
import kotlin.concurrent.thread

class MainViewModel {
    private val _suspendState = MutableStateFlow(false)
    val suspendState = _suspendState.asStateFlow()

    private var _suspendTimeJob: Job? = null
    private val _suspendTime = MutableStateFlow(0L)
    val suspendTime = _suspendTime.asStateFlow()

    private val _logText = MutableStateFlow("输出日志")
    val logText = _logText.asStateFlow()

    private var _heartBeatJob: Job? = null

    /**
     * Robot 类
     */
    private val robot by lazy { Robot() }

    /**
     * 开关暂离状态
     */
    fun toggleSuspendState() {
        _suspendTimeJob?.cancel()
        _suspendTimeJob = null

        _heartBeatJob?.cancel()
        _heartBeatJob = null
        _logText.value = "输出日志"

        _suspendState.value = !_suspendState.value

        // 开启
        if (_suspendState.value) {
            _suspendTime.value = 0
            _suspendTimeJob = GlobalScope.launch {
                withContext(Dispatchers.IO) {
                    while (isActive) {
                        delay(1000)
                        _suspendTime.value += 1000
                    }
                }
            }

            _heartBeatJob = GlobalScope.launch {
                withContext(Dispatchers.IO) {
                    while (isActive) {
                        runCatching { onSuspendHeartBeat() }.onFailure { it.printStackTrace() }
                    }
                }
            }
        }
    }

    /**
     * 暂离挂机心跳执行逻辑
     */
    private suspend fun onSuspendHeartBeat() {
        val desktopCapture = robot.createDesktopCapture()

        // 进入游戏界面
        val enterGameBtnRect = findImage(desktopCapture, getCharacter("enter-game-btn.png"))
        if (enterGameBtnRect != null) {
            println("检测到进入游戏界面，自动点击【进入魔兽世界】")

            robot.click(enterGameBtnRect)
        }

        // 掉线了界面
        val disconnectRect = findImage(desktopCapture, getCharacter("disconnect.jpg"))
        if (disconnectRect != null) {
            println("掉线了界面，自动点击【确定】")

            robot.click(disconnectRect, verBias = 0.6f)
        }

        // 重新连接界面
        val reconnectBtnRect = findImage(desktopCapture, getCharacter("reconnect-btn.png"))
        if (reconnectBtnRect != null) {
            println("重新连接界面，自动点击【重新连接】")

            robot.click(reconnectBtnRect)
        }

        // 重新连接错误界面
        val reconnectErrorRect = findImage(desktopCapture, getCharacter("reconnect-error.png"))
        if (reconnectErrorRect != null) {
            println("重新连接错误界面，自动点击【确定】")

            robot.click(reconnectErrorRect, verBias = 0.6f)
        }

        // 登录状态丢失界面
        val loginForm = findImage(desktopCapture, getCharacter("login-form.png"))
        if (loginForm != null) {
            println("登录状态丢失界面")

            val exitBtn = findImage(desktopCapture, getCharacter("exit-btn.png"))
            if (exitBtn != null) {
                println("登录状态丢失，自动点击【退出】")
                robot.click(exitBtn)

                delay(2000)

                findWindow("战网").maxWindow()
                findWindow("战网").activeWindow()
            }
        }

        // 排队界面
        val queueRect = findImage(desktopCapture, getCharacter("queue.png"))
        if (queueRect != null) {
            println("排队界面...")
        }

        // 服务器列页面
        val targetServer = findImage(desktopCapture, getCharacter("server-target.png"))
        if (targetServer != null) {
            println("自动进入目标服务器")

            robot.click(targetServer)
            robot.click(targetServer)
        }

        // 自动重新启动游戏
        val startBattleGame = findImage(desktopCapture, getCharacter("start-battle-game.png"))
        if (startBattleGame != null) {
            println("自动重新启动游戏")

            findWindow("战网").activeWindow()

            robot.click(startBattleGame)
            robot.click(startBattleGame)

            findWindow("战网").minWindow()
        }
    }

    private fun getCharacter(fileName: String): File {
        val target = File(dataDirOf("character"), fileName)
        if (target.exists()) return target

        return useResource("images/character/$fileName") {
            target.also { file ->
                file.outputStream().use { out ->
                    it.copyTo(out)
                }
            }
        }
    }

    private fun println(string: String) {
        _logText.value = _logText.value + "\n" + string
    }

    fun screenShots() {
        thread { robot.createDesktopCapture() }
    }

    fun matchImage() {
        val file = File("C:\\Users\\why\\IdeaProjects\\WowTool\\data\\screen\\dis.jpg")
        val rect = findImage(
            source = file,
            target = File("C:\\Users\\why\\IdeaProjects\\WowTool\\src\\main\\resources\\images\\disconnect.jpg")
        )
        drawImageRect(file.absolutePath, rect)
    }
}