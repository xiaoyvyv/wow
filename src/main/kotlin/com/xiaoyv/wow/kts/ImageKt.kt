package com.xiaoyv.wow.kts

import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

/**
 * 保存图像
 */
fun BufferedImage.save(filePath: String) {
    ImageIO.write(this, "png", File(filePath))
}
