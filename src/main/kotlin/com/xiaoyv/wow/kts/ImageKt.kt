package com.xiaoyv.wow.kts

import com.sun.jna.platform.win32.GDI32Util
import com.sun.jna.platform.win32.User32
import org.bytedeco.javacpp.DoublePointer
import org.bytedeco.opencv.global.opencv_core.minMaxLoc
import org.bytedeco.opencv.global.opencv_imgcodecs.imread
import org.bytedeco.opencv.global.opencv_imgcodecs.imwrite
import org.bytedeco.opencv.global.opencv_imgproc.*
import org.bytedeco.opencv.opencv_core.Mat
import org.bytedeco.opencv.opencv_core.Point
import org.bytedeco.opencv.opencv_core.Rect
import org.bytedeco.opencv.opencv_core.Scalar
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

fun captureScreenByJna() {
    val time = System.currentTimeMillis()
    GDI32Util.getScreenshot(User32.INSTANCE.GetDesktopWindow())
        .save(System.getProperty("user.dir").toString() + "/tmp1.png")
    println("captureScreenByJna spend: ${System.currentTimeMillis() - time} ms")
}

/**
 * 保存图像
 */
fun BufferedImage.save(filePath: String) {
    ImageIO.write(this, "png", File(filePath))
}


/**
 * 图像匹配
 *
 * @author why
 * @since 6/28/24
 */
fun findImage(source: File, target: File, threshold: Double = 0.7): Rect? {
    val sourceImg = imread(source.absolutePath)
    val targetImg = imread(target.absolutePath)

    val result = Mat()

    matchTemplate(sourceImg, targetImg, result, TM_CCOEFF_NORMED)

    // 寻找最佳匹配位置
    val maxVal = DoublePointer(1)
    val maxLoc = Point()
    minMaxLoc(result, null, maxVal, null, maxLoc, null)

    // 设置阈值，根据实际情况调整
    if (maxVal.get() >= threshold) {
        val width = targetImg.cols()
        val height = targetImg.rows()
        return Rect(maxLoc.x(), maxLoc.y(), width, height)
    } else {
        return null
    }
}

/**
 * 给图片绘制矩形框
 */
fun drawImageRect(imagePath: String, rect: Rect?) {
    val data = imread(imagePath)
    rectangle(data, rect ?: return, Scalar(0.0, 0.0, 255.0, 0.0))
    imwrite(File(dataDirOf("tmp"), "match.png").absolutePath, data)
}