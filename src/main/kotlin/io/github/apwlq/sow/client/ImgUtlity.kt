package io.github.apwlq.sow.client

import java.awt.RenderingHints
import java.awt.image.BufferedImage

class ImgUtlity {

    // 이미지 크기를 화면 크기에 맞게 조정하는 함수
    fun resizeImageToScreen(image: BufferedImage, screenWidth: Int, screenHeight: Int): BufferedImage {
        val scaleWidth = screenWidth.toDouble() / image.width
        val scaleHeight = screenHeight.toDouble() / image.height
        val scale = minOf(scaleWidth, scaleHeight)
        val newWidth = (image.width * scale).toInt()
        val newHeight = (image.height * scale).toInt()
        val scaledImage = BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB)
        val graphics = scaledImage.createGraphics()
        graphics.drawImage(image, 0, 0, newWidth, newHeight, null)
        graphics.dispose()
        return scaledImage
    }

    // 비율에 맞게 이미지를 조정하는 함수
    fun scaleImageToAspectRatio(image: BufferedImage, width: Int, height: Int): BufferedImage {
        val aspectRatio = image.width.toDouble() / image.height.toDouble()
        val newWidth: Int
        val newHeight: Int

        if (width.toDouble() / height.toDouble() > aspectRatio) {
            newHeight = height
            newWidth = (height * aspectRatio).toInt()
        } else {
            newWidth = width
            newHeight = (width / aspectRatio).toInt()
        }

        val scaledImage = BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB)
        val graphics2D = scaledImage.createGraphics()
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
        graphics2D.drawImage(image, 0, 0, newWidth, newHeight, null)
        graphics2D.dispose()
        return scaledImage
    }
}