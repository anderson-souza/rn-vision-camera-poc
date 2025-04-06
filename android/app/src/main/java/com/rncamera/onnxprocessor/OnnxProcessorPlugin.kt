package com.rncamera.onnxprocessor

import android.media.Image
import android.util.Log
import com.mrousavy.camera.frameprocessors.Frame
import com.mrousavy.camera.frameprocessors.FrameProcessorPlugin
import com.mrousavy.camera.frameprocessors.VisionCameraProxy
import com.rncamera.onnxprocessor.ObjectDetector.detect
import java.io.InputStream
import java.io.SequenceInputStream
import java.util.Collections


class OnnxProcessorPlugin(proxy: VisionCameraProxy, options: Map<String, Any>?) :
    FrameProcessorPlugin() {
    override fun callback(frame: Frame, params: Map<String, Any>?): Any? {
        try {
            val image: Image = frame.image

            Log.d(
                "OnnxProcessorPlugin",
                image.width.toString() + " x " + image.height + " Image with format #" + image.format + ". Logging " + params?.size + " parameters:"
            )

//            val bitmap = frame.getImageProxy().toBitmap()
//
//            val size: Int = bitmap.getRowBytes() * bitmap.getHeight()
//            val byteBuffer = ByteBuffer.allocate(size)
//            bitmap.copyPixelsToBuffer(byteBuffer)
//            val byteArray = byteBuffer.array()
//
//            val rawImageBytes = imageToByteArray(image)

            val streams: MutableList<InputStream> = ArrayList()
            for (plane in image.planes) {
                streams.add(ByteBufferBackedInputStream(plane.buffer.duplicate()))
            }
            val combinedStream: InputStream = SequenceInputStream(Collections.enumeration(streams))

            val detections = detect(byteArray)

            Log.d("OnnxProcessorPlugin", "Detected $detections objects")

            return detections
        } catch (e: Exception) {
            Log.e("OnnxProcessorPlugin", "Error processing frame", e)
            return null
        }
    }
}