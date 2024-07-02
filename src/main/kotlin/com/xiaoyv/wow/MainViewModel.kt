@file:OptIn(DelicateCoroutinesApi::class)

package com.xiaoyv.wow

import com.xiaoyv.wow.kts.*
import io.github.mymonstercat.Model
import io.github.mymonstercat.ocr.InferenceEngine
import io.github.mymonstercat.ocr.config.ParamConfig
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.awt.Robot
import kotlin.concurrent.thread

class MainViewModel {
    private val _suspendState = MutableStateFlow(false)
    val suspendState = _suspendState.asStateFlow()

    private var _suspendTimeJob: Job? = null
    private val _suspendTime = MutableStateFlow(0L)
    val suspendTime = _suspendTime.asStateFlow()

    private val _logText = MutableStateFlow("输出日志")
    val logText = _logText.asStateFlow()

    private val _targetServer = MutableStateFlow("吉安娜")
    val targetServer = _targetServer.asStateFlow()

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

    private val errorHandler = CoroutineExceptionHandler { _, throwable ->
        if (throwable !is CancellationException) {
            log(throwable.stackTraceToString())
        }
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

        if (findWindow("魔兽世界") == null) {
            _suspendState.value = false
            log("没有找到游戏窗口，请检查是否启动了【魔兽世界】")
            return
        }

        _suspendState.value = !_suspendState.value

        // 开启
        if (_suspendState.value) {
            _suspendTime.value = 0
            _suspendTimeJob = GlobalScope.launch(Dispatchers.Default + errorHandler) {
                withContext(Dispatchers.IO) {
                    while (isActive) {
                        delay(1000)
                        _suspendTime.value += 1000
                    }
                }
            }

            _heartBeatJob = GlobalScope.launch(Dispatchers.Default + errorHandler) {
                withContext(Dispatchers.IO) {
                    while (isActive) {
                        runCatching { onSuspendHeartBeat() }
                            .onFailure { throwable ->
                                if (throwable !is CancellationException) {
                                    log(throwable.stackTraceToString())
                                }
                            }
                    }
                }
            }
        }
    }


    /**
     * 暂离挂机心跳执行逻辑
     */
    private suspend fun onSuspendHeartBeat() {
        val wowWindow = findWindow("魔兽世界")
        if (wowWindow == null) {
            log("未找到【魔兽世界】窗口")
            return
        }

        delay(2000)

        wowWindow.activeWindow()
        wowWindow.setPosition()

        val desktopCapture = robot.createWindowCapture(wowWindow)

        // OCR
        val engine = InferenceEngine.getInstance(Model.ONNX_PPOCR_V4)
        val result = engine.runOcr(desktopCapture.absolutePath, ocrParamConfig)
        val textBlocks = result.textBlocks

        // 进入魔兽世界}
        textBlocks.findTextBlock("进入魔兽世界")?.let { textBlock ->
            log("检测到进入游戏界面，自动点击【进入魔兽世界】")
            robot.click(textBlock)
        }

        // 重新连接
        if (textBlocks.hasAllTextBlocks("魔兽世界", "重新连接")) {
            if (textBlocks.findTextBlock("断开连接") != null || textBlocks.findTextBlock("服务器断开") != null) {
                log("断开连接弹窗，自动点击【确定】")
                robot.click(textBlocks.findTextBlock("确定"))
                return
            }

            if (textBlocks.findTextBlock("无法重新连接") != null) {
                log("无法重新连接弹窗，自动点击【确定】")
                robot.click(textBlocks.findTextBlock("确定"))
                return
            }

            log("重新连接界面，自动点击【重新连接】")
            robot.click(textBlocks.findTextBlock("重新连接"))
        }

        // 排队界面
        if (textBlocks.hasAllTextBlocks("魔兽世界", "队列位置", "预计时间")) {
            log(
                buildString {
                    append(textBlocks.findTextBlock("队列位置")?.text)
                    append("，")
                    append(textBlocks.findTextBlock("预计时间")?.text)
                    append("，排队中...")
                }
            )
        }

        // 服务器列页面
        if (textBlocks.hasAllTextBlocks("服务器名称", targetServer.value)) {
            log("自动进入目标服务器【${targetServer.value}】")

            robot.click(textBlocks.findTextBlock(targetServer.value))
            robot.click(textBlocks.findTextBlock(targetServer.value))
        }
    }

    private fun log(string: String) {
        _logText.value = _logText.value + "\n" + string
    }

    fun screenShots() {
        thread { robot.createDesktopCapture() }
    }

    fun changeServer(option: String) {
        _targetServer.value = option
    }
}