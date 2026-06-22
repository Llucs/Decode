package com.decode.app.engine.elf

import java.io.File
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder

class ElfEditor {

    data class ElfHeader(
        val isElf: Boolean,
        val is32Bit: Boolean,
        val littleEndian: Boolean,
        val entryPoint: Long,
        val sectionCount: Int,
        val segmentCount: Int
    )

    fun readHeader(elfFile: File): ElfHeader {
        return try {
            RandomAccessFile(elfFile, "r").use { raf ->
                val magic = ByteArray(4)
                raf.readFully(magic)
                val isElf = magic[0].toInt() == 0x7F && magic[1].toInt() == 'E'.toInt() &&
                        magic[2].toInt() == 'L'.toInt() && magic[3].toInt() == 'F'.toInt()

                if (!isElf) {
                    return ElfHeader(false, false, false, 0, 0, 0)
                }

                val is32Bit = raf.readByte().toInt() == 1
                val littleEndian = raf.readByte().toInt() == 1
                val order = if (littleEndian) ByteOrder.LITTLE_ENDIAN else ByteOrder.BIG_ENDIAN

                raf.seek(0)
                val headerSize = if (is32Bit) 52 else 64
                val header = ByteArray(headerSize)
                raf.readFully(header)
                val buf = ByteBuffer.wrap(header).order(order)

                val entryPoint = if (is32Bit) {
                    buf.getInt(24).toLong() and 0xFFFFFFFFL
                } else {
                    buf.getLong(24)
                }

                val sectionCount = if (is32Bit) buf.getShort(48).toInt() and 0xFFFF
                                  else buf.getShort(60).toInt() and 0xFFFF

                val segmentCount = if (is32Bit) buf.getShort(44).toInt() and 0xFFFF
                                  else buf.getShort(56).toInt() and 0xFFFF

                ElfHeader(true, is32Bit, littleEndian, entryPoint, sectionCount, segmentCount)
            }
        } catch (e: Exception) {
            ElfHeader(false, false, false, 0, 0, 0)
        }
    }
}
