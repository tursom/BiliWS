package cn.tursom.ws

import cn.tursom.core.buffer.ByteBuffer
import cn.tursom.core.buffer.impl.HeapByteBuffer

data class BiliWSPackageHead(
  var totalSize: Int = 0,
  var headSize: Short = 16,
  var version: Short = 0,
  var code: Int = 0,
  var sequence: Int = 1
) {
  fun toByteArray(): ByteArray {
    val buffer = HeapByteBuffer(16)
    writeTo(buffer)
    return buffer.array
  }

  fun writeTo(buffer: java.nio.ByteBuffer): BiliWSPackageHead {
    buffer.putInt(totalSize)
    buffer.putShort(headSize)
    buffer.putShort(version)
    buffer.putInt(code)
    buffer.putInt(sequence)
    return this
  }

  fun writeTo(buffer: ByteBuffer): BiliWSPackageHead {
    buffer.put(totalSize)
    buffer.put(headSize)
    buffer.put(version)
    buffer.put(code)
    buffer.put(sequence)
    return this
  }

  fun readFrom(buffer: java.nio.ByteBuffer): BiliWSPackageHead {
    totalSize = buffer.int
    headSize = buffer.short
    version = buffer.short
    code = buffer.int
    sequence = buffer.int
    return this
  }

  fun readFrom(buffer: ByteBuffer): BiliWSPackageHead {
    totalSize = buffer.getInt()
    headSize = buffer.getShort()
    version = buffer.getShort()
    code = buffer.getInt()
    sequence = buffer.getInt()
    return this
  }
}