package com.xiaoyv.wow.kts

import java.io.File

val dataDir by lazy {
    File(System.getProperty("user.dir"), "data").also { it.mkdirs() }
}

fun dataDirOf(child: String): File {
    return File(dataDir, child).also { it.mkdirs() }
}