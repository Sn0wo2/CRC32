package impl

import `interface`.ICRC32
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

// Thanks https://github.com/esterTion/BiliBili_crc2mid
private const val POLY = 0xEDB88320.toInt()

private val TABLE = IntArray(256).apply {
    for (i in 0..255) {
        var v = i
        repeat(8) { v = if (v and 1 != 0) POLY xor (v ushr 1) else v ushr 1 }
        this[i] = v
    }
}

private fun reverse(
    hash: String,
    min: Int = 0,
    max: Int = 1_000_000_000,
    threads: Int = Runtime.getRuntime().availableProcessors()
): String? {
    var h = hash.toLong(16).inv().toInt()
    val idx = IntArray(4)
    for (i in 3 downTo 0) {
        idx[3 - i] = TABLE.indexOfFirst { it ushr 24 == (h ushr (i * 8) and 0xFF) }
        h = h xor (TABLE[idx[3 - i]] ushr ((3 - i) * 8))
    }
    val pool = Executors.newFixedThreadPool(threads)
    val done = AtomicBoolean()
    val result = arrayOfNulls<String>(1)
    val chunk = ((max.toLong() - min + 1) / threads).coerceAtLeast(1L).toInt()

    fun crc(s: String): Int = s.fold(0xFFFFFFFF.toInt()) { r, ch -> (r ushr 8) xor TABLE[(r xor ch.code) and 0xFF] }

    for (t in 0 until threads) {
        val start = min + t * chunk
        val end = if (t == threads - 1) max else start + chunk - 1
        pool.submit {
            for (i in start..end) {
                if (done.get()) return@submit
                val s = i.toString()
                var r = 0xFFFFFFFF.toInt()
                var last = 0
                for (c in s) {
                    last = (r xor c.code) and 0xFF
                    r = (r ushr 8) xor TABLE[last]
                }
                if (last != idx[3]) continue
                var cur = crc(s)
                val sb = StringBuilder()
                for (j in 2 downTo 0) {
                    val tc = (cur and 0xFF) xor idx[j]
                    if (tc !in 48..57) {
                        cur = -1; break
                    }
                    sb.append(tc - 48)
                    cur = TABLE[idx[j]] xor (cur ushr 8)
                }
                if (cur >= 0 && done.compareAndSet(false, true)) result[0] = s + sb.reverse()
            }
        }
    }
    pool.shutdown()
    pool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS)
    return result[0]
}

class CRC32 : ICRC32 {
    override fun reverseMID(hash: String): String? {
        return reverse(hash)
    }
}