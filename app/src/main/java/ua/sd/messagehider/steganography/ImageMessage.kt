package ua.sd.messagehider.steganography

import android.graphics.Bitmap

// Based on https://github.com/tigerlyb/Steganography-in-Java/blob/master/Main.java
class ImageMessage(private val container: Bitmap) {

    var secretText: String? = null
        private set

    // Come up with something else because I'm not sure that it is OK to do null-check every time with !! or ?.
    var secretImage: Bitmap? = null
        private set

    private var secretImageWidth = 0
    private var secretImageHeight = 0

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

    fun findSecret(): Boolean {
        if (tryFindImageTag()) {
            secretImage = findImage(secretImageWidth, secretImageHeight)
            return true
        }

        if (tryFindTextTag()) {
            secretText = findText()
            return true
        }

        return false
    }

    // TODO: Make private
    private fun findText(): String {

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

    // TODO: Explain this method. Why 0x7FFFFFFF???
    private fun findImage(sw: Int, sh: Int): Bitmap {
        val lastBitMask = 0x00000001
        var x = MESSAGE_HIDER_START_SECRET
        var y = 0
        var bit = 0
        secretImage = Bitmap.createBitmap(sw, sh, container.config)
        val secretPixelCount = sw * sh
        val pixelArray = IntArray(secretPixelCount)

        for (i in pixelArray.indices) {
            var pixel = 0x00000000

            for (index in 1 .. 32) {
                if (x < container.width) {
                    bit = container.getPixel(x, y) and lastBitMask
                    ++x
                }
                else {
                    x = 0
                    ++y
                    bit = container.getPixel(x, y) and lastBitMask
                }

                if (bit == 1) {
                    pixel = pixel shr 1
                    pixel = pixel or 0x80000000.toInt()
                }
                else {
                    pixel = pixel shr 1
                    pixel = pixel and 0x7FFFFFFF
                }
            }
            pixelArray[i] = pixel
        }

        x = 0
        y = 0

        for (i in pixelArray.indices) {
            if (x < sw) {
                secretImage!!.setPixel(x, y, pixelArray[i])
                ++x
            }
            else {
                x = 0
                ++y
                secretImage!!.setPixel(x, y, pixelArray[i])
            }
        }

        return secretImage!!
    }

    fun hideImage(content: Bitmap): Bitmap {
        val containerPixelsCount = container.width * container.height
        val contentPixelsCount = content.width * content.height

        // TODO: What to return? Null (bed idea). False and set field?
        if (containerPixelsCount / contentPixelsCount < 32) {
            return content
        }

        writeImageTag(content.width, content.height)

        var x = 0
        var y = 0

        // Prefill array of secret image pixels. Then loop through it and forget about width/height checks
        val contentPixels = IntArray(contentPixelsCount)
        for (i in contentPixels.indices) {
            if (x < content.width) {
                contentPixels[i] = content.getPixel(x, y)
                ++x
            }
            else {
                x = 0
                ++y
                contentPixels[i] = content.getPixel(x,y)
            }
        }

        x = MESSAGE_HIDER_START_SECRET
        y = 0

        val lastBitMask = 0x00000001

        for (i in contentPixels.indices) {
            for (bitPos in 1..32) {
                val bit = contentPixels[i] and lastBitMask
                if (bit == 1) {
                    if (x < container.width) {
                        container.setPixel(x, y, container.getPixel(x, y) or 0x00000001)
                        ++x
                    }
                    else {
                        x = 0
                        ++y
                        container.setPixel(x, y, container.getPixel(x, y) or 0x00000001)
                    }
                }
                else {
                    if (x < container.width) {
                        container.setPixel(x, y, container.getPixel(x, y) and 0xFFFFFFFE.toInt())
                        ++x
                    }
                    else {
                        x = 0
                        ++y
                        container.setPixel(x, y, container.getPixel(x, y) and 0xFFFFFFFE.toInt())
                    }
                }

                contentPixels[i] = contentPixels[i] shr 1
            }
        }

        return container
    }

    private fun writeImageTag(sw: Int, sh: Int) {
        hideText(MESSAGE_HIDER_START_IMAGE_TAG.toString())
        val lastBitMask = 0x00000001
        var sw_ = sw
        var sh_ = sh
        var x = MESSAGE_HIDER_START_IMAGE_BITS
        var y = 0

        for (index in 1..MESSAGE_HIDER_WIDTH_HEIGHT_BITS) {
            val lastBit = sw_ and lastBitMask
            if (lastBit == 1) {
                if (x < container.width) {
                    container.setPixel(x, y, container.getPixel(x, y) or 0x00000001)
                    ++x
                }
                else {
                    x = 0
                    ++y
                    container.setPixel(x, y, container.getPixel(x, y) or 0x00000001)
                }
            }
            else {
                if (x < container.width) {
                    container.setPixel(x, y, container.getPixel(x, y) and 0xFFFFFFFE.toInt())
                    ++x
                }
                else {
                    x = 0
                    ++y
                    container.setPixel(x, y, container.getPixel(x, y) and 0xFFFFFFFE.toInt())
                }
            }
            sw_ = sw_ shr 1
        }

        for (index in 1..MESSAGE_HIDER_WIDTH_HEIGHT_BITS) {
            val lastBit = sh_ and lastBitMask
            if (lastBit == 1) {
                if (x < container.width) {
                    container.setPixel(x, y, container.getPixel(x, y) or 0x00000001)
                    ++x
                }
                else {
                    x = 0
                    ++y
                    container.setPixel(x, y, container.getPixel(x, y) or 0x00000001)
                }
            }
            else {
                if (x < container.width) {
                    container.setPixel(x, y, container.getPixel(x, y) and 0xFFFFFFFE.toInt())
                    ++x
                }
                else {
                    x = 0
                    ++y
                    container.setPixel(x, y, container.getPixel(x, y) and 0xFFFFFFFE.toInt())
                }
            }
            sh_ = sh_ shr 1
        }
    }

    private fun tryFindImageTag(): Boolean {
        val lastBitMask = 0x00000001

        var x = 0
        var y = 0

        var characterBit = 0
        var flag = 0

        for (i in 1..MESSAGE_HIDER_START_IMAGE_BITS) {
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
                characterBit = characterBit or 0x80
            }
            else {
                characterBit = characterBit shr 1
            }
        }

        if (characterBit.toChar() == MESSAGE_HIDER_START_IMAGE_TAG) {
            var width = 0
            for (i in 1..MESSAGE_HIDER_WIDTH_HEIGHT_BITS) {
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
                    width = width shr 1
                    width = width or 0x400 // 11 bits (1...0)
                }
                else {
                    width = width shr 1
                }
            }

            secretImageWidth = width

            var height = 0
            for (i in 1..MESSAGE_HIDER_WIDTH_HEIGHT_BITS) {
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
                    height = height shr 1
                    height = height or 0x400 // 11 bits (1...0)
                }
                else {
                    height = height shr 1
                }
            }

            secretImageHeight = height

            return true
        }

        return false
    }

    private fun tryFindTextTag(): Boolean {


        return false
    }

    companion object {
        private val MESSAGE_HIDER_START_IMAGE_TAG = '^'
        private val MESSAGE_HIDER_START_IMAGE_BITS = 8
        private val MESSAGE_HIDER_WIDTH_HEIGHT_BITS = 11
        private val MESSAGE_HIDER_START_SECRET = 30
        private val MESSAGE_HIDER_START_TEXT_TAG = '<'
        private val MESSAGE_HIDER_END_TEXT_TAG = '>'
    }
}
