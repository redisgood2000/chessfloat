package com.chessfloat

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.Gravity
import android.view.View
import android.view.WindowManager

/** Transparent, non-interactive full-screen overlay for three candidate arrows. */
class BoardArrowOverlayService : Service() {
    private var windowManager: WindowManager? = null
    private var arrowView: ArrowCanvasView? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_HIDE) {
            removeOverlay()
            stopSelf()
            return START_NOT_STICKY
        }
        val moves = intent?.getStringArrayListExtra(EXTRA_MOVES).orEmpty()
        val left = intent?.getFloatExtra(EXTRA_LEFT, 0f) ?: 0f
        val top = intent?.getFloatExtra(EXTRA_TOP, 0f) ?: 0f
        val size = intent?.getFloatExtra(EXTRA_SIZE, 0f) ?: 0f
        val flipped = intent?.getBooleanExtra(EXTRA_FLIPPED, false) ?: false
        if (moves.isEmpty() || size <= 0f) {
            stopSelf()
            return START_NOT_STICKY
        }
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        removeOverlay()
        val parsed = moves.take(3).mapIndexedNotNull { index, uci ->
            if (!Regex("""^[a-h][1-8][a-h][1-8][qrbn]?$""").matches(uci)) null
            else CandidateMove(index + 1, uci, uci.substring(0,2), uci.substring(2,4),
                uci.getOrNull(4), null, null, 0, listOf(uci))
        }
        if (parsed.isEmpty()) {
            stopSelf()
            return START_NOT_STICKY
        }
        val view = ArrowCanvasView(this).apply {
            configure(parsed, left, top, size, flipped)
        }
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply { gravity = Gravity.TOP or Gravity.START }
        windowManager?.addView(view, params)
        arrowView = view
        return START_NOT_STICKY
    }

    private fun removeOverlay() {
        arrowView?.let { view -> runCatching { windowManager?.removeView(view) } }
        arrowView = null
    }

    override fun onDestroy() {
        removeOverlay()
        super.onDestroy()
    }

    companion object {
        const val ACTION_HIDE = "com.chessfloat.HIDE_ARROWS"
        private const val EXTRA_MOVES = "moves"
        private const val EXTRA_LEFT = "board_left"
        private const val EXTRA_TOP = "board_top"
        private const val EXTRA_SIZE = "board_size"
        private const val EXTRA_FLIPPED = "board_flipped"

        fun show(context: Context, uciMoves: List<String>, left: Float, top: Float,
            size: Float, flipped: Boolean = false) {
            val intent = Intent(context, BoardArrowOverlayService::class.java)
                .putStringArrayListExtra(EXTRA_MOVES, ArrayList(uciMoves.take(3)))
                .putExtra(EXTRA_LEFT, left).putExtra(EXTRA_TOP, top)
                .putExtra(EXTRA_SIZE, size).putExtra(EXTRA_FLIPPED, flipped)
            context.startService(intent)
        }

        fun hide(context: Context) {
            context.stopService(Intent(context, BoardArrowOverlayService::class.java)
                .setAction(ACTION_HIDE))
        }
    }
}

private class ArrowCanvasView(context: Context) : View(context) {
    private var segments: List<ArrowSegment> = emptyList()
    private val colors = intArrayOf(0xFF22C55E.toInt(), 0xFF3B82F6.toInt(), 0xFFF59E0B.toInt())
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        strokeWidth = 11f * resources.displayMetrics.density
    }

    fun configure(moves: List<CandidateMove>, left: Float, top: Float,
        size: Float, flipped: Boolean) {
        segments = BoardArrowGeometry.fromCandidates(moves, left, top, size, flipped)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        segments.forEach { segment ->
            val color = colors.getOrElse(segment.rank - 1) { colors.last() }
            paint.color = color
            paint.alpha = 220
            canvas.drawLine(segment.from.x, segment.from.y, segment.to.x, segment.to.y, paint)
            val angle = kotlin.math.atan2(
                (segment.to.y - segment.from.y).toDouble(),
                (segment.to.x - segment.from.x).toDouble()
            )
            val head = 25f * resources.displayMetrics.density
            val path = Path().apply {
                moveTo(segment.to.x, segment.to.y)
                lineTo(segment.to.x - head * kotlin.math.cos(angle - Math.PI / 6).toFloat(),
                    segment.to.y - head * kotlin.math.sin(angle - Math.PI / 6).toFloat())
                lineTo(segment.to.x - head * kotlin.math.cos(angle + Math.PI / 6).toFloat(),
                    segment.to.y - head * kotlin.math.sin(angle + Math.PI / 6).toFloat())
                close()
            }
            paint.style = Paint.Style.FILL
            canvas.drawPath(path, paint)
            paint.style = Paint.Style.STROKE
        }
    }
}
