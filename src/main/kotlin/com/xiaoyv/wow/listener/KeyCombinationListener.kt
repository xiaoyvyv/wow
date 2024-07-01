package com.xiaoyv.wow.listener

import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener
import java.util.concurrent.CopyOnWriteArrayList

open class KeyCombinationListener : NativeKeyListener {
    private var ctrlPressed = false
    private var altPressed = false

    private val listeners = CopyOnWriteArrayList<OnCtrlAltPressedListener>()

    fun addCtrlAltPressedListener(listener: OnCtrlAltPressedListener) {
        listeners.add(listener)
    }

    override fun nativeKeyPressed(nativeEvent: NativeKeyEvent) {
        if (nativeEvent.keyCode == NativeKeyEvent.VC_CONTROL) {
            ctrlPressed = true
        }
        if (nativeEvent.keyCode == NativeKeyEvent.VC_ALT) {
            altPressed = true
        }

        if (ctrlPressed && altPressed) {
            listeners.forEach { it.onCtrlAltPressed(nativeEvent) }
        }
    }


    override fun nativeKeyReleased(nativeEvent: NativeKeyEvent) {
        if (nativeEvent.keyCode == NativeKeyEvent.VC_CONTROL) {
            ctrlPressed = false
        }
        if (nativeEvent.keyCode == NativeKeyEvent.VC_ALT) {
            altPressed = false
        }
    }
}