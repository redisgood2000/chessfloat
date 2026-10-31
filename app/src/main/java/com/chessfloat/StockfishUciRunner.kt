package com.chessfloat

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.util.concurrent.TimeUnit

/**
 * Runs the bundled Stockfish Android executable extracted by Android from jniLibs.
 * The CI build supplies the official Stockfish 19 arm64-v8a universal binary.
 * UCI scores are reported from the side-to-move perspective.
 */
class StockfishUciRunner(private val executable: File) {
    suspend fun analyze(fen: String, depth: Int = 20, timeoutMs: Long = 30_000): List<CandidateMove> =
        withContext(Dispatchers.IO) {
            require(FenValidator.validate(fen) == null) {
                FenValidator.validate(fen) ?: "Invalid FEN"
            }
            require(depth in 1..40)
            check(executable.isFile && executable.canExecute()) {
                "Stockfish executable missing or not executable: ${executable.absolutePath}"
            }
            withTimeout(timeoutMs) {
                runInterruptible {
                    val process = ProcessBuilder(executable.absolutePath)
                        .redirectErrorStream(true).start()
                    val writer = BufferedWriter(OutputStreamWriter(process.outputStream))
                    val reader = BufferedReader(InputStreamReader(process.inputStream))
                    try {
                        writer.write("uci\n")
                        writer.flush()
                        readUntil(reader, "uciok", 5_000)
                        writer.write("setoption name MultiPV value 3\n")
                        writer.write("isready\n")
                        writer.flush()
                        readUntil(reader, "readyok", 5_000)
                        writer.write("position fen $fen\n")
                        writer.write("go depth $depth\n")
                        writer.flush()

                        val latest = mutableMapOf<Int, CandidateMove>()
                        while (true) {
                            val line = reader.readLine() ?: error("Stockfish exited before bestmove")
                            if (line.startsWith("info ")) {
                                val parsed = UciEngine.parseInfo(line) ?: continue
                                val previous = latest[parsed.rank]
                                if (previous == null || parsed.depth >= previous.depth) {
                                    latest[parsed.rank] = parsed
                                }
                            }
                            if (line.startsWith("bestmove ")) break
                        }
                        latest.values.sortedBy { it.rank }.take(3)
                    } finally {
                        runCatching { writer.write("quit\n"); writer.flush() }
                        runCatching { process.waitFor(500, TimeUnit.MILLISECONDS) }
                        if (process.isAlive) process.destroyForcibly()
                        reader.close()
                        writer.close()
                    }
                }
            }
        }

    private fun readUntil(reader: BufferedReader, expected: String, timeoutMs: Long) {
        val deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(timeoutMs)
        while (System.nanoTime() < deadline) {
            if (!reader.ready()) {
                Thread.sleep(10)
                continue
            }
            if (reader.readLine()?.trim() == expected) return
        }
        error("Timed out waiting for Stockfish $expected")
    }
}
