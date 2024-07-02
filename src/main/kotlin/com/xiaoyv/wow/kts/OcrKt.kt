package com.xiaoyv.wow.kts

import com.benjaminwan.ocrlibrary.Point
import com.benjaminwan.ocrlibrary.TextBlock
import java.awt.Rectangle

val TextBlock?.rect: Rectangle?
    get() = this?.boxPoint?.calcRectangle()

/**
 * OcrKt
 *
 * @author why
 * @since 7/1/24
 */
fun List<TextBlock>.findTextBlock(text: String): TextBlock? {
    return find { it.text.orEmpty().contains(text) }
}

/**
 * OcrKt
 *
 * @author why
 * @since 7/1/24
 */
fun List<TextBlock>.hasAllTextBlocks(vararg texts: String): Boolean {
    var result = true
    texts.forEach {
        if (findTextBlock(it) == null) {
            result = false
        }
    }
    return result
}


/**
 * 计算矩形框
 *
 * 根据传入的4个坐标点，得出矩形框的左上角及长宽
 */
fun List<Point>.calcRectangle(): Rectangle {
    var minX = Int.MAX_VALUE
    var minY = Int.MAX_VALUE
    var maxX = Int.MIN_VALUE
    var maxY = Int.MIN_VALUE

    // 找到最小和最大的 x、y 坐标
    for (point in this) {
        val x = point.x
        val y = point.y
        if (x < minX) {
            minX = x
        }
        if (y < minY) {
            minY = y
        }
        if (x > maxX) {
            maxX = x
        }
        if (y > maxY) {
            maxY = y
        }
    }

    // 矩形的左上角坐标即是最小x、y
    val width = maxX - minX
    val height = maxY - minY
    return Rectangle(minX, minY, width, height)
}