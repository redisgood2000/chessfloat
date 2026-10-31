package com.chessfloat

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView

class FloatingAssistantService : Service() {
    private lateinit var windowManager: WindowManager
    private var panel: View? = null
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        showPanel()
    }

    private fun showPanel() {
        val density = resources.displayMetrics.density
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding((14*density).toInt(), (10*density).toInt(),
                (14*density).toInt(), (10*density).toInt())
            setBackgroundColor(0xEE101827.toInt())
        }
        root.addView(TextView(this).apply {
            text = "♟ CHESSFLOAT  ·  STUDY"
            textSize = 14f
            setTextColor(0xFFFFFFFF.toInt())
        })
        root.addView(TextView(this).apply {
            text = "1  — waiting for position\n2  — waiting for position\n3  — waiting for position"
            textSize = 13f
            setTextColor(0xFFE2E8F0.toInt())
            setPadding(0, (8*density).toInt(), 0, 0)
        })
        val type = if (Build.VERSION.SDK_INT >= 26)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE
        val lp = WindowManager.LayoutParams((260*density).toInt(),
            WindowManager.LayoutParams.WRAP_CONTENT, type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT).apply {
            gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            y = (90*density).toInt()
        }
        root.setOnTouchListener(object : View.OnTouchListener {
            private var downX = 0f; private var downY = 0f
            private var startX = 0; private var startY = 0
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        downX = event.rawX; downY = event.rawY
                        startX = lp.x; startY = lp.y
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        lp.x = startX + (event.rawX-downX).toInt()
                        lp.y = startY - (event.rawY-downY).toInt()
                        windowManager.updateViewLayout(root, lp)
                        return true
                    }
                }
                return true
            }
        })
        windowManager.addView(root, lp)
        panel = root
    }

    override fun onDestroy() {
        panel?.let { runCatching { windowManager.removeView(it) } }
        panel = null
        super.onDestroy()
    }
}
