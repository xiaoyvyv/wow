package com.xiaoyv.wow.kts

import com.benjaminwan.ocrlibrary.TextBlock
import com.sun.jna.platform.win32.User32
import com.sun.jna.platform.win32.WinDef
import java.awt.Rectangle
import java.awt.Robot
import java.awt.Toolkit
import java.awt.event.InputEvent
import java.io.File
import kotlin.math.roundToInt

/**
 * 截屏
 */
fun Robot.createDesktopCapture(): File {
    val captureDir = dataDirOf("capture")
    val captureImage = File(captureDir, "screenshot.png")
    createScreenCapture(Rectangle(Toolkit.getDefaultToolkit().screenSize))
        .save(captureImage.absolutePath)
    return captureImage
}

/**
 * 截取指定窗口
 */
fun Robot.createWindowCapture(window: WinDef.HWND): File {
    // 获取窗口位置和大小
    val rect = WinDef.RECT().apply {
        User32.INSTANCE.GetWindowRect(window, this)
    }

    val x = rect.left
    val y = rect.top
    val width = rect.right - rect.left
    val height = rect.bottom - rect.top

    // 捕获屏幕截图
    val screenRect = Rectangle(x, y, width, height)
    val captureDir = dataDirOf("capture")
    val captureImage = File(captureDir, "screenshot.png")
    createScreenCapture(screenRect).save(captureImage.absolutePath)
    return captureImage
}

/**
 * 点击文字块
 */
fun Robot.click(textBlock: TextBlock?) {
    textBlock ?: return
    click(textBlock.boxPoint.calcRectangle())
}

/**
 * 点击坐标
 */
fun Robot.click(box: Rectangle, horBias: Float = 0.5f, verBias: Float = 0.5f) {
    val centerX = box.x + box.width * horBias
    val centerY = box.y + box.height * verBias

    mouseMove(centerX.roundToInt(), centerY.roundToInt())
    mousePress(InputEvent.BUTTON1_DOWN_MASK)
    delay(40)
    mouseRelease(InputEvent.BUTTON1_DOWN_MASK)

    // 移动开鼠标
    delay(40)
    mouseMove(0, 0)
}