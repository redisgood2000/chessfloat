package com.chessfloat

import android.content.Context
import android.graphics.Bitmap
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel

/**
 * TFLite adapter contract for a 13-class piece model:
 * empty, white pawn/knight/bishop/rook/queen/king, black pawn/knight/bishop/rook/queen/king.
 *
 * Supply a trained model at assets/piece_classifier.tflite. The repository intentionally
 * does not ship a pretend/untrained model. Input contract: 64x64 RGB float32 in [0,1],
 * output: 13 float32 logits/probabilities. Validate this contract against the model card.
 */
class BoardPieceRecognizer(context: Context) : AutoCloseable {
    private val interpreter: Interpreter

    init {
        val afd = context.assets.openFd("piece_classifier.tflite")
        val mapped = FileInputStream(afd.fileDescriptor).channel.map(
            FileChannel.MapMode.READ_ONLY, afd.startOffset, afd.declaredLength
        )
        interpreter = Interpreter(mapped)
        afd.close()
        require(interpreter.getInputTensor(0).shape().contentEquals(intArrayOf(1, 64, 64, 3))) {
            "Model input must be [1,64,64,3]"
        }
        require(interpreter.getOutputTensor(0).numElements() == 13) {
            "Model output must contain 13 classes"
        }
    }

    fun classify(square: Bitmap): PiecePrediction {
        val scaled = Bitmap.createScaledBitmap(square, 64, 64, true)
        val input = ByteBuffer.allocateDirect(1 * 64 * 64 * 3 * 4)
            .order(ByteOrder.nativeOrder())
        val pixels = IntArray(64 * 64)
        scaled.getPixels(pixels, 0, 64, 0, 0, 64, 64)
        for (pixel in pixels) {
            input.putFloat(((pixel shr 16 and 0xff) / 255f))
            input.putFloat(((pixel shr 8 and 0xff) / 255f))
            input.putFloat(((pixel and 0xff) / 255f))
        }
        input.rewind()
        val output = Array(1) { FloatArray(13) }
        interpreter.run(input, output)
        scaled.recycle()
        val best = output[0].indices.maxByOrNull { output[0][it] } ?: 0
        return PiecePrediction(best, output[0][best])
    }

    override fun close() = interpreter.close()
}

data class PiecePrediction(val classIndex: Int, val confidence: Float)
