package com.xiaoyv.wow.kts

import com.sun.jna.platform.win32.User32
import com.sun.jna.platform.win32.WinDef
import com.sun.jna.platform.win32.WinUser

fun findWindow(title: String): WinDef.HWND? {
    return User32.INSTANCE.FindWindow(null, title)
}

fun WinDef.HWND?.maxWindow() {
    this ?: return
    User32.INSTANCE.ShowWindow(this, WinUser.SW_MAXIMIZE)
}

fun WinDef.HWND?.minWindow() {
    this ?: return
    User32.INSTANCE.ShowWindow(this, WinUser.SW_MINIMIZE)
}

fun WinDef.HWND?.activeWindow() {
    User32.INSTANCE.ShowWindow(this, WinUser.SW_RESTORE)
    User32.INSTANCE.SetForegroundWindow(this)
    User32.INSTANCE.SetFocus(this)
    User32.INSTANCE.UpdateWindow(this)
}