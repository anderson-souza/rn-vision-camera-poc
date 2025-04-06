package com.rncamera.onnxprocessor

import ai.onnxruntime.OnnxJavaType
import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.nio.ByteBuffer
import java.util.Collections
import ai.onnxruntime.extensions.OrtxPackage
import java.io.InputStream


data class Result(
    var outputBitmap: Bitmap,
    var outputBox: Array<FloatArray>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as Result

        if (outputBitmap != other.outputBitmap) return false
        if (!outputBox.contentDeepEquals(other.outputBox)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + outputBitmap.hashCode()
        result = 31 * result + outputBox.contentDeepHashCode()
        return result
    }

    override fun toString(): String {
        return "Result(outputBitmap=$outputBitmap, outputBox=${outputBox.contentToString()})"
    }


}

object ObjectDetector {

    private val ortEnv: OrtEnvironment by lazy {
        OrtEnvironment.getEnvironment()
    }
    private val ortSession: OrtSession by lazy {
        val sessionOptions: OrtSession.SessionOptions = OrtSession.SessionOptions()
        sessionOptions.registerCustomOpLibrary(OrtxPackage.getLibraryPath())
        ortEnv.createSession(loadModel(), sessionOptions)
    }

    private fun loadModel(): ByteArray {
        val file = "res/raw/yolov8n_with_pre_post_processing.onnx"
        return this::class.java.classLoader.getResourceAsStream(file).readBytes()
    }

    fun detect(rawImageBytes: ByteArray): Result {
        // Step 2: get the shape of the byte array and make ort tensor
        val shape = longArrayOf(rawImageBytes.size.toLong())

        val inputTensor = OnnxTensor.createTensor(
            ortEnv,
            ByteBuffer.wrap(rawImageBytes),
            shape,
            OnnxJavaType.UINT8
        )
        inputTensor.use {
            // Step 3: call ort inferenceSession run
            val output = ortSession.run(
                Collections.singletonMap("image", inputTensor),
                setOf("image_out", "scaled_box_out_next")
            )

            // Step 4: output analysis
            output.use {
                val rawOutput = (output?.get(0)?.value) as ByteArray
                val boxOutput = (output.get(1)?.value) as Array<FloatArray>
                val outputImageBitmap = byteArrayToBitmap(rawOutput)

                // Step 5: set output result
                val result = Result(outputImageBitmap, boxOutput)
                return result
            }
        }
    }

    fun detect(inputStream: InputStream): Result {
        // Step 1: convert image into byte array (raw image bytes)
        val rawImageBytes = inputStream.readBytes()

        // Step 2: get the shape of the byte array and make ort tensor
        val shape = longArrayOf(rawImageBytes.size.toLong())

        val inputTensor = OnnxTensor.createTensor(
            ortEnv,
            ByteBuffer.wrap(rawImageBytes),
            shape,
            OnnxJavaType.UINT8
        )
        inputTensor.use {
            // Step 3: call ort inferenceSession run
            val output = ortSession.run(
                Collections.singletonMap("image", inputTensor),
                setOf("image_out", "scaled_box_out_next")
            )

            // Step 4: output analysis
            output.use {
                val rawOutput = (output?.get(0)?.value) as ByteArray
                val boxOutput = (output.get(1)?.value) as Array<FloatArray>
                val outputImageBitmap = byteArrayToBitmap(rawOutput)

                // Step 5: set output result
                val result = Result(outputImageBitmap, boxOutput)
                return result
            }
        }
    }

    private fun byteArrayToBitmap(data: ByteArray): Bitmap {
        return BitmapFactory.decodeByteArray(data, 0, data.size)
    }
}