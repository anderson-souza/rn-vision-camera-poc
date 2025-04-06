package com.rncamera.onnxprocessor

import android.graphics.ImageFormat
import android.media.Image
import java.io.ByteArrayInputStream
import java.io.InputStream


fun imageToByteArray(image: Image): ByteArray {
    val width = image.width
    val height = image.height
    val yBuffer = image.planes[0].buffer // Y
    val uBuffer = image.planes[1].buffer // U
    val vBuffer = image.planes[2].buffer // V

    val ySize = yBuffer.remaining()
    val uSize = uBuffer.remaining()
    val vSize = vBuffer.remaining()

    val nv21 = ByteArray(ySize + uSize + vSize)

    // Copy Y data
    yBuffer.get(nv21, 0, ySize)

    // NV21 format requires V and U data interleaved
    val chromaRowStride = image.planes[1].rowStride
    val chromaPixelStride = image.planes[1].pixelStride

    var offset = ySize
    for (row in 0 until height / 2) {
        for (col in 0 until width / 2) {
            val uIndex = row * chromaRowStride + col * chromaPixelStride
            val vIndex = row * chromaRowStride + col * chromaPixelStride
            nv21[offset++] = vBuffer[vIndex] // V
            nv21[offset++] = uBuffer[uIndex] // U
        }
    }

    return nv21
}
