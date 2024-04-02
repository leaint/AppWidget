package com.example.appwidget

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityManager
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast

class MyAccessibilityService : AccessibilityService() {

    companion object {
        const val LOCK_SCREEN_ACTION = "LOCK_SCREEN_ACTION"
    }
    private lateinit var manager: AccessibilityManager
    override fun onCreate() {
        super.onCreate()
        manager = getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            if (it.action == LOCK_SCREEN_ACTION) {
                performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN)
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Toast.makeText(this, "MyAccessibilityService stoped", Toast.LENGTH_SHORT).show()
        return super.onUnbind(intent)
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Toast.makeText(this, "MyAccessibilityService started", Toast.LENGTH_SHORT).show()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) {
            return
        }
        windows.forEach {

            try {
                var count = it.childCount
                for (i in 0 until count) {
                    val child = it.getChild(i)
                    child.childCount
                }
                val root = it.root
                count = root?.childCount ?: 0
                for (i in 0 until count) {


                    val child = root.getChild(i)
                    if (child?.packageName?.equals("com.miui.calculator") == true &&
                        child.contentDescription?.equals("更多选项") == true
                    ) {
                        child.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    }
                }
            } catch (e: NullPointerException) {
                // just ignore
            }
        }

    }

    override fun onInterrupt() {
        // do nothing
    }
}