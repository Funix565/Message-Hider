package ua.sd.messagehider.steganography

import android.graphics.Bitmap

// Based on https://github.com/tigerlyb/Steganography-in-Java/blob/master/Main.java
class ImageMessage(private val container: Bitmap) {

    // Work with ASCII 8-bit characters
    // Embed text in every LSB of Color
    // TODO: Work with UTF-8
    fun hideText(content: String): Bitmap {
        // TODO: Explain this code and bit operations

        // New String template with $ because in Kotlin function parameters are immutable
        val content_ = "$content$MESSAGE_HIDER_END_TEXT_TAG"

        // TODO: Mark first pixels with MESSAGE_HIDER_START_TEXT_TAG

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
                        container.setPixel(x, y, container.getPixel(x, y) or 0x00000001)
                        ++x
                    } else {
                        x = 0
                        ++y
                        container.setPixel(x, y, container.getPixel(x, y) or 0x00000001)
                    }
                }
                else {
                    if (x < container.width) {
                        container.setPixel(x, y, container.getPixel(x, y) and 0xFFFFFFFE.toInt())
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

    /* TODO: Create public method boolean findMessage(). How to return TEXT or Bitmap?
    * Check hidden message type: text, image, none.
    * Call private methods, set result-field (or how to return different fields?) and return true
    * Set result-field null and return false
    * Caller checks if result is true or false
    * If it is true, it checks read-only fields
    *  */


    // TODO: Make private
    fun findText(): String {

        // TODO: Check start tag MESSAGE_HIDER_START_TEXT_TAG '<' at the beginning before finding

        val lastBitMask = 0x00000001

        var resultBytes = byteArrayOf()

        var x = 0
        var y = 0

        // TODO: Maybe work with strings val binary = "01010101", binary.toInt(2)

        var characterBit = 0
        var flag = 0

        // TODO: What if there is no hidden message? -- ADD another tag at the beginning and check before finding
        while (characterBit.toChar() != MESSAGE_HIDER_END_TEXT_TAG) {
            for (i in 1..8) {
                if (x < container.width) {
                    flag = container.getPixel(x, y) and lastBitMask
                    ++x
                }
                else {
                    x = 0
                    ++y
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

        // Remove END_TAG in the end
        return foundMsg.dropLast(1)
    }

    companion object {
        private val MESSAGE_HIDER_START_IMAGE_TAG = '^'
        private val MESSAGE_HIDER_START_TEXT_TAG = '<'
        private val MESSAGE_HIDER_END_TEXT_TAG = '>'
    }
}
