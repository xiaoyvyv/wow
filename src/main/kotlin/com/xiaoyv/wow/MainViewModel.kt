@file:OptIn(DelicateCoroutinesApi::class)

package com.xiaoyv.wow

import com.github.kokorin.jaffree.LogLevel
import com.github.kokorin.jaffree.ffmpeg.FFmpeg
import com.sun.jna.platform.win32.WinDef
import com.xiaoyv.wow.kts.*
import io.github.mymonstercat.Model
import io.github.mymonstercat.ocr.InferenceEngine
import io.github.mymonstercat.ocr.config.ParamConfig
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import tool.Debounce
import java.awt.Robot
import java.awt.event.KeyEvent
import java.util.*
import kotlin.concurrent.thread
import kotlin.math.pow
import kotlin.random.Random


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

    private val _fishState = MutableStateFlow(false)
    val fishState = _fishState.asStateFlow()

    private var _heartBeatJob: Job? = null
    private var _fishJob: Job? = null

    private val debounce = Debounce(2000)
    private val debounce2 = Debounce(2000)

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

            // 重新启动游戏
            val battleWindow = findWindow("战网")
            if (battleWindow != null) {
                log("发现战网窗口，重新启动游戏")
                restartGame(battleWindow)
            }
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

        // 登录信息失效
        if (textBlocks.hasAllTextBlocks("登录", "密码")) {
            robot.click(textBlocks.findTextBlock("退出"))
            delay(3000)
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

    /**
     * 重启游戏
     */
    private suspend fun restartGame(battleWindow: WinDef.HWND) {
        battleWindow.activeWindow()
        battleWindow.setPosition()
        delay(1000)
        val capture = robot.createWindowCapture(battleWindow)

        val engine = InferenceEngine.getInstance(Model.ONNX_PPOCR_V4)
        val result = engine.runOcr(capture.absolutePath, ocrParamConfig)
        val textBlocks = result.textBlocks

        robot.click(textBlocks.findTextBlock("进入游戏"))
        robot.click(textBlocks.findTextBlock("进入游戏"))

        delay(5000)
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

    fun toggleFish() {
        _fishState.value = !_fishState.value

        _fishJob?.cancel()
        _fishJob = null

        if (_fishState.value) {
            _fishJob = GlobalScope.launch(Dispatchers.IO + errorHandler) {
                val wowWindow = findWindow("魔兽世界").also { delay(1000) }
                wowWindow.activeWindow()
                wowWindow.setPosition()

                while (isActive) {
                    startFishing()
                    delay(60000)
                }
            }

            GlobalScope.launch(Dispatchers.Default + errorHandler) {
                autoFishing {
                    robot.keyPress(KeyEvent.VK_NUMPAD0)
                    robot.delay(Random.nextInt(5, 50))
                    robot.keyRelease(KeyEvent.VK_NUMPAD0)
                    robot.delay(Random.nextInt(5, 50))

                    startFishing()
                }
            }
        }
    }

    private fun startFishing() {
        debounce2.debounce {
            robot.keyPress(KeyEvent.VK_EQUALS)
            robot.delay(Random.nextInt(5, 50))
            robot.keyRelease(KeyEvent.VK_EQUALS)
        }

//        log("钓鱼%d次".format())
    }

    private suspend fun autoFishing(onFished: () -> Unit) {

        withContext(Dispatchers.IO) {
            val tmpList = Collections.synchronizedList(arrayListOf<Double>())

            FFmpeg.atPath()
                .addArguments("-f", "dshow")
                .addArguments("-i", "audio=\"virtual-audio-capturer\"")
                .addArguments("-filter_complex", "ebur128=peak=true")
                .addArguments("-f", "null")
                .addArgument("/dev/null")
                .setLogLevel(LogLevel.INFO)
                .setOutputListener {
                    if (it.contains("ebur128")) {
                        val momentary = "M:(.*?)S:".toRegex(RegexOption.IGNORE_CASE)
                            .find(it)?.groups?.get(1)?.value?.trim()?.toDoubleOrNull() ?: 0.0

                        if (tmpList.size >= 5) tmpList.removeAt(0)
                        tmpList.add(momentary)

                        if (tmpList.size == 5) {
                            val average = tmpList.average()
                            if (average > -18) {
                                debounce.debounce {
                                    log("大鱼上钩啦 ${average}!")
                                    onFished()
                                }
                            }
                        }
                    }

                    if (!_fishState.value) throw IllegalStateException("停止钓鱼")
                }
                .execute()
        }


        /**
         * [Parsed_ebur128_0 @ 000002a893fea240] t: 189.900979 TARGET:-23 LUFS    M:  -8.1 S:  -9.1     I: -12.9 LUFS       LRA:  19.8 LU  FTPK:  -3.4  -2.1 dBFS  TPK:  -0.2  -0.1 dBFS
         *
         *
         * Integrated loudness (整合声压)：表示音频文件整体的声压级别。
         * Momentary loudness (瞬时声压)：表示短时间（约400ms）内的声压级别。
         * Short-term loudness (短期声压)：表示中时间（约3秒）内的声压级别。
         * LRA (Loudness Range)：表示音频文件的声压范围。
         * True peak (真实峰值)：表示音频信号的最大峰值。
         * LUFS (Loudness Units relative to Full Scale)是一种测量音频响度的单位，特别用于广播和音频流媒体的响度标准。它由国际电信联盟（ITU）和欧洲广播联盟（EBU）提出，用于衡量感知响度，而不是简单的信号强度。
         * dBFS (decibels relative to Full Scale)是一种测量音频信号电平的单位，表示相对于数字音频系统中的最大可表示电平（满刻度，Full Scale）的分贝值。
         *
         */
        // ffmpeg -f dshow -i audio="virtual-audio-capturer" -filter_complex "showwavespic=s=640x120" -frames:v 1 output.png
        // ffmpeg -v debug -f dshow -i audio="virtual-audio-capturer" -af "volumedetect" -t 2  -f null -
        // ffmpeg -f dshow -i audio="virtual-audio-capturer" -filter_complex ebur128=peak=true -f null /dev/null
    }

    private fun dbToLinear(dbValue: Double): Double {
        return 10.0.pow(dbValue / 20.0)
    }
}











