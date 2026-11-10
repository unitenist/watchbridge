package com.sasa.watchbridge.ancs

import java.io.ByteArrayOutputStream

internal fun Byte.u(): Int = toInt() and 0xFF

internal fun readU16LE(b: ByteArray, o: Int): Int = b[o].u() or (b[o + 1].u() shl 8)

internal fun readU32LE(b: ByteArray, o: Int): Int =
    b[o].u() or (b[o + 1].u() shl 8) or (b[o + 2].u() shl 16) or (b[o + 3].u() shl 24)

internal fun ByteArrayOutputStream.writeU16LE(v: Int) {
    write(v and 0xFF)
    write((v ushr 8) and 0xFF)
}

internal fun ByteArrayOutputStream.writeU32LE(v: Int) {
    write(v and 0xFF)
    write((v ushr 8) and 0xFF)
    write((v ushr 16) and 0xFF)
    write((v ushr 24) and 0xFF)
}

internal fun ByteArray.indexOfByte(value: Int, from: Int): Int {
    for (i in from until size) if (this[i].u() == value) return i
    return -1
}
