package com.rncamera.onnxprocessor

import java.io.InputStream
import java.nio.ByteBuffer
import kotlin.math.min

class ByteBufferBackedInputStream(private val buf: ByteBuffer) : InputStream() {
    override fun read(): Int {
        return if (buf.hasRemaining()) (buf.get().toInt() and 0xFF) else -1
    }

    override fun read(bytes: ByteArray, off: Int, len: Int): Int {
        var len = len
        len = min(len.toDouble(), buf.remaining().toDouble()).toInt()
        buf[bytes, off, len]
        return len
    }
}