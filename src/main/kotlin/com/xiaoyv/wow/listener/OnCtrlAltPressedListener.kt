package com.xiaoyv.wow.listener

import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent

interface OnCtrlAltPressedListener {

    fun onCtrlAltPressed(keyEvent: NativeKeyEvent)
}