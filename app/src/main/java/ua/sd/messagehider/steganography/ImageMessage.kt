package ua.sd.messagehider.steganography

import android.graphics.Bitmap

// Based on https://github.com/tigerlyb/Steganography-in-Java/blob/master/Main.java
class ImageMessage(private val container: Bitmap) {

    // Work with ASCII 8-bit characters
    // Embed text in every LSB of Color
    // TODO: Work with UTF-8
    fun hideText(content: String): Bitmap {

//        val test = "я"
//        val length = test.length
//        val bytes = test.encodeToByteArray()
//        val sz = bytes.size
//
//        val intYa = bytes[0].toInt()
//        val uintYa = bytes[0].toUInt()
//        val ubyteYa = bytes[0].toUByte()
//
//        val h:Int = 0xff
//        val ub = bytes[0].toInt() and h
//
//        val decodeToString = bytes.decodeToString()
//
//        val toHexString = Integer.toHexString(bytes[0].toInt())
//
//        val binaryString = Integer.toBinaryString(bytes[0].toInt())

        // TODO: Comment this code, try to manually debug
        // Implement find()


        val content_ = "$content^"

        // ARGB is 32 bits
        val lastBitMask = 0x00000001
        var x = 0
        var y = 0

        for (index in 0..content_.length - 1) {
            var character = content_[index].toInt()
            // Loop through every bit in character byte
            for (j in 1..8) {
                val lastBit = character and lastBitMask

                if (lastBit == 1) {
                    if (x < container.width) {
                        val temp = container.getPixel(x, y)
                        container.setPixel(x, y, container.getPixel(x, y) or 0x00000001)
                        val temp2 = container.getPixel(x, y)
                        ++x
                    } else {
                        x = 0
                        ++y
                        val temp = container.getPixel(x, y)
                        container.setPixel(x, y, container.getPixel(x, y) or 0x00000001)
                        val temp2 = container.getPixel(x, y)
                    }
                }
                else {
                    if (x < container.width) {
                        val temp = container.getPixel(x, y)
                        container.setPixel(x, y, container.getPixel(x, y) and 0xFFFFFFFE.toInt())
                        val temp2 = container.getPixel(x, y)
                        ++x
                    } else {
                        x = 0
                        ++y
                        container.setPixel(x, y, container.getPixel(x, y) and 0xFFFFFFFE.toInt())
                    }
                }

                character = character shr 1
            }
        }

        return container
    }

    fun findText(): String {
        val lastBitMask = 0x00000001

        var resultBytes = byteArrayOf()

        var x = 0
        var y = 0

        // TODO: Work with strings val binary = "01010101", binary.toInt(2)

        var characterBit = 0
        var flag = 0

        while (characterBit.toChar() != '^') {
            for (i in 1..8) {
                if (x < container.width) {
                    val temp = container.getPixel(x, y)
                    flag = container.getPixel(x, y) and lastBitMask
                    ++x
                }
                else {
                    x = 0
                    ++y
                    val temp = container.getPixel(x, y)
                    flag = container.getPixel(x, y) and lastBitMask
                }
                if (flag == 1) {
                    characterBit = characterBit shr 1

                    // 0b1000000
                    characterBit = characterBit or 0x80
                }
                else {
                    characterBit = characterBit shr 1
                }
            }
            resultBytes += (characterBit.toByte())
        }


        val foundMsg = resultBytes.decodeToString()

        // TODO Remove '^' in the end

        return foundMsg
    }
}