@file:OptIn(DelicateCoroutinesApi::class)

package com.xiaoyv.wow

import com.xiaoyv.wow.kts.*
import io.github.mymonstercat.Model
import io.github.mymonstercat.ocr.InferenceEngine
import io.github.mymonstercat.ocr.config.ParamConfig
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.awt.Robot
import java.util.logging.Level
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
     * OCR
     */
    private val ocrParamConfig = ParamConfig.getDefaultConfig().apply {
        isDoAngle = true
        isMostAngle = true
    }

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
                        runCatching { onSuspendHeartBeat() }
                            .onFailure { log(it.stackTraceToString()) }
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

        // OCR
        val engine = InferenceEngine.getInstance(Model.ONNX_PPOCR_V4)
        val result = engine.runOcr(desktopCapture.absolutePath, ocrParamConfig)
        val textBlocks = result.textBlocks

        // 进入魔兽世界
        textBlocks.findTextBlock("进入魔兽世界")?.let { textBlock ->
            log("检测到进入游戏界面，自动点击【进入魔兽世界】")
            robot.click(textBlock)
        }

        // 重新连接
        textBlocks.findTextBlock("重新连接")?.let { textBlock ->
            if (textBlocks.findTextBlock("断开连接") != null || textBlocks.findTextBlock("服务器断开") != null) {
                log("断开连接弹窗，自动点击【确定】")
                robot.click(textBlocks.findTextBlock("确定"))
            }

            if (textBlocks.findTextBlock("无法重新连接") != null) {
                log("无法重新连接弹窗，自动点击【确定】")
                robot.click(textBlocks.findTextBlock("确定"))
                return@let
            }

            log("重新连接界面，自动点击【重新连接】")
            robot.click(textBlock)
        }

        // 登录状态丢失界面
        if (textBlocks.findTextBlock("登录") != null || textBlocks.findTextBlock("密码") != null) {
            log("登录状态丢失，自动点击【退出】")
            robot.click(textBlocks.findTextBlock("退出"))

            delay(2000)

            findWindow("战网").maxWindow()
            findWindow("战网").activeWindow()
        }

        // 排队界面
        textBlocks.findTextBlock("队列位置")?.let {
            log("排队中...")
        }

        // 服务器列页面
        textBlocks.findTextBlock("吉安娜")?.let { textBlock ->
            if (textBlocks.findTextBlock("队列位置") == null) {
                log("自动进入目标服务器【吉安娜】")

                robot.click(textBlock)
                robot.click(textBlock)
            }
        }

        // 自动重新启动游戏
        textBlocks.findTextBlock("进入游戏")?.let { textBlock ->
            log("自动从战网客户端重新启动游戏")

            findWindow("战网").activeWindow()

            robot.click(textBlock)
            robot.click(textBlock)

            findWindow("战网").minWindow()
        }
    }

    private fun log(string: String) {
        _logText.value = _logText.value + "\n" + string
    }

    fun screenShots() {
        thread { robot.createDesktopCapture() }
    }
}